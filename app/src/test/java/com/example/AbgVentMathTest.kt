package com.example

import com.example.domain.AbgMath
import com.example.domain.AbgSampleType
import com.example.domain.AbgSeverity
import com.example.domain.VentAlertSeverity
import com.example.domain.VentMath
import com.example.domain.VentStrategy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AbgVentMathTest {

    @Test
    fun `abg identifies compensated DKA high-AG metabolic acidosis and triggers vent alert`() {
        val result = AbgMath.analyze(
            sampleType = AbgSampleType.ARTERIAL,
            ph = 7.14,
            paco2 = 22.0,
            hco3 = 8.0,
            pao2 = 105.0,
            fio2Percent = 21.0,
            na = 134.0,
            cl = 98.0
        )!!
        assertEquals(AbgSeverity.CRITICAL, result.severity)
        assertTrue(result.primaryDisorder.contains("High-AG Metabolic Acidosis"))
        assertTrue(result.compensationStatus.contains("Winter's"))
        assertEquals(28.0, result.anionGap!!, 0.1)
        assertTrue(result.metAcidosisVentAlert)
        assertTrue(result.likelyCauses.any { it.contains("DKA") || it.contains("Ketoacidosis") })
    }

    @Test
    fun `abg detects mixed metabolic and respiratory acidosis when PaCO2 exceeds Winter upper bound`() {
        // HCO3 = 16 -> Winter's = 1.5*16 + 8 = 32 +/- 2 -> 30..34. PaCO2 55 > 34 -> concurrent resp acidosis
        val result = AbgMath.analyze(
            sampleType = AbgSampleType.ARTERIAL,
            ph = 7.08,
            paco2 = 55.0,
            hco3 = 16.0,
            pao2 = 54.0,
            fio2Percent = 60.0
        )!!
        assertEquals(AbgSeverity.CRITICAL, result.severity)
        assertTrue(result.primaryDisorder.contains("Mixed Respiratory & Metabolic Acidosis"))
        assertEquals(90.0, result.pfRatio!!, 0.5)
        assertTrue(result.oxygenationText.contains("ARDS"))
    }

    @Test
    fun `abg detects acute respiratory acidosis vs chronic respiratory acidosis`() {
        val acute = AbgMath.analyze(
            sampleType = AbgSampleType.ARTERIAL,
            ph = 7.24,
            paco2 = 60.0,
            hco3 = 25.0
        )!!
        assertTrue(acute.primaryDisorder.contains("Acute Respiratory Acidosis"))

        val chronic = AbgMath.analyze(
            sampleType = AbgSampleType.ARTERIAL,
            ph = 7.33,
            paco2 = 70.0,
            hco3 = 35.0
        )!!
        assertTrue(chronic.primaryDisorder.contains("Chronic"))
    }

    @Test
    fun `abg detects respiratory alkalosis and metabolic alkalosis`() {
        val respAlk = AbgMath.analyze(
            sampleType = AbgSampleType.ARTERIAL,
            ph = 7.52,
            paco2 = 26.0,
            hco3 = 21.0
        )!!
        assertTrue(respAlk.primaryDisorder.contains("Respiratory Alkalosis"))

        val metAlk = AbgMath.analyze(
            sampleType = AbgSampleType.ARTERIAL,
            ph = 7.51,
            paco2 = 48.0,
            hco3 = 38.0
        )!!
        assertTrue(metAlk.primaryDisorder.contains("Metabolic Alkalosis"))
    }

    @Test
    fun `vent setup calculates normal, metabolic acidosis, PLV, and pediatric formulas per PCG`() {
        // 70 kg Normal: 7 mL/kg = 490 mL, VE = 100 mL/kg = 7000 mL/min -> RR = 7000 / 490 = 14
        val normal = VentMath.calculateSetup(VentStrategy.NORMAL, 70.0, "IBW", 7.0)!!
        assertEquals(490, normal.targetVtMl)
        assertEquals(7.0, normal.targetVeLMin, 0.05)
        assertEquals(14, normal.startingRr)

        // 70 kg Met Acidosis: VE = 120 mL/kg = 8400 mL/min -> RR = 8400 / 490 = 17
        val acid = VentMath.calculateSetup(VentStrategy.METABOLIC_ACIDOSIS, 70.0, "IBW", 7.0)!!
        assertEquals(8.4, acid.targetVeLMin, 0.05)
        assertEquals(17, acid.startingRr)

        // 70 kg Perfusion Limited: VT 10 mL/kg = 700 mL, RR fixed 10
        val plv = VentMath.calculateSetup(VentStrategy.PERFUSION_LIMITED, 70.0, "IBW", 10.0)!!
        assertEquals(700, plv.targetVtMl)
        assertEquals(10, plv.startingRr)

        // 8 kg Pediatric (< 10 kg): Min VE = 200 mL/kg/min = 1.6 L/min, VT 7 mL/kg = 56 mL -> RR = 29
        val peds = VentMath.calculateSetup(VentStrategy.PEDIATRIC, 8.0, "Direct", 7.0)!!
        assertEquals(56, peds.targetVtMl)
        assertEquals(1.6, peds.targetVeLMin, 0.05)
        assertEquals(29, peds.startingRr)
    }

    @Test
    fun `vent mechanics differentiates resistance vs compliance and calculates 3xRCexp`() {
        // High Rinsp (Ppeak 42 - Pplat 19 = 23 > 5) + RCexp 1.4 -> Min TE = 4.2s; RR 18 TI 1.0 -> Actual TE = 2.33s < 4.2s
        val highRinsp = VentMath.analyzeMechanics(
            cstat = 52.0,
            rinsp = 28.0,
            rcexp = 1.4,
            ppeak = 42.0,
            pplat = 19.0,
            peep = 5.0,
            vte = 450.0,
            autoPeep = 4.0,
            vLeakPercent = 3.0,
            rr = 18.0,
            tiSec = 1.0,
            p01 = -3.5
        )
        assertEquals(VentAlertSeverity.CRITICAL, highRinsp.overallSeverity)
        assertEquals(23.0, highRinsp.pipMinusPplat!!, 0.1)
        assertEquals(4.2, highRinsp.minTeThreeRcExp!!, 0.05)
        assertTrue(highRinsp.findings.any { it.headline.contains("Resistance") })
        assertTrue(highRinsp.findings.any { it.headline.contains("Expiratory Time") || it.headline.contains("Breath Stacking") })

        // Low Cstat (ARDS): Ppeak 36, Pplat 33 (gradient 3 <= 5), Cstat = 420 / (33 - 10) = 18.3
        val lowCstat = VentMath.analyzeMechanics(
            cstat = 26.0,
            rinsp = 11.0,
            rcexp = 0.4,
            ppeak = 36.0,
            pplat = 33.0,
            peep = 10.0,
            vte = 420.0,
            autoPeep = 0.0,
            vLeakPercent = 2.0,
            rr = 22.0,
            tiSec = 0.9,
            p01 = -2.5
        )
        assertEquals(VentAlertSeverity.CRITICAL, lowCstat.overallSeverity)
        assertNotNull(lowCstat.computedCstat)
        assertTrue(lowCstat.findings.any { it.headline.contains("Compliance") })
    }
}
