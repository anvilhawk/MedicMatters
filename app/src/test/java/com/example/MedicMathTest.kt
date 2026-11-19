package com.example

import com.example.domain.DoseRateUnit
import com.example.domain.DoseRangeStatus
import com.example.domain.DripMode
import com.example.domain.GcsEye
import com.example.domain.GcsMotor
import com.example.domain.GcsSeverity
import com.example.domain.GcsVerbal
import com.example.domain.HeightUnit
import com.example.domain.MedicMath
import com.example.domain.MedicationUnit
import com.example.domain.OxygenCylinder
import com.example.domain.OxygenStatus
import com.example.domain.PatientSex
import com.example.domain.TimeUnit
import com.example.domain.WeightUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicMathTest {

    @Test
    fun `volume drip calculation matches standard EMS formula`() {
        // 500 mL over 4 hours with 15 gtt/mL tubing -> (500 * 15) / 240 = 31.25 gtt/min
        val result = MedicMath.calculateVolumeDrip(
            volumeMl = 500.0,
            timeValue = 4.0,
            timeUnit = TimeUnit.HOURS,
            dropFactorGttPerMl = 15
        )
        assertNotNull(result)
        assertEquals(31.25, result!!.gttPerMinute, 0.01)
        assertEquals(31, result.roundedGttPerMinute)
        assertEquals(125.0, result.mlPerHour, 0.01)
        assertEquals(7.8125, result.gttPer15Seconds, 0.01)
    }

    @Test
    fun `dopamine weight-based infusion calculates exact microdrip rate`() {
        // 80 kg patient, 5 mcg/kg/min, 400 mg in 250 mL (1600 mcg/mL), 60 gtt/mL
        // Desired = 400 mcg/min -> 400 / 1600 = 0.25 mL/min = 15 mL/hr = 15 gtt/min
        val result = MedicMath.calculateDoseDrip(
            weight = 80.0,
            weightUnit = WeightUnit.KG,
            desiredDose = 5.0,
            doseRateUnit = DoseRateUnit.MCG_KG_MIN,
            drugAmount = 400.0,
            drugUnit = MedicationUnit.MG,
            bagVolumeMl = 250.0,
            dropFactorGttPerMl = 60
        )
        assertNotNull(result)
        assertEquals(15.0, result!!.gttPerMinute, 0.01)
        assertEquals(15.0, result.mlPerHour, 0.01)
        assertNull(result.warning)
    }

    @Test
    fun `lidocaine grams bag converts properly for mg per min infusion`() {
        // 2 mg/min from 2 g in 500 mL (4 mg/mL) with 60 gtt/mL -> 0.5 mL/min = 30 mL/hr = 30 gtt/min
        val result = MedicMath.calculateDoseDrip(
            weight = null,
            weightUnit = WeightUnit.KG,
            desiredDose = 2.0,
            doseRateUnit = DoseRateUnit.MG_MIN,
            drugAmount = 2.0,
            drugUnit = MedicationUnit.G,
            bagVolumeMl = 500.0,
            dropFactorGttPerMl = 60
        )
        assertNotNull(result)
        assertEquals(30.0, result!!.gttPerMinute, 0.01)
        assertEquals(30.0, result.mlPerHour, 0.01)
    }

    @Test
    fun `oxygen D cylinder duration with 200 PSI residual matches NREMT math`() {
        // (1500 - 200) * 0.16 / 15 = 1300 * 0.16 / 15 = 208 / 15 = 13.8667 min
        val result = MedicMath.calculateOxygenDuration(
            cylinder = OxygenCylinder.D,
            currentPsi = 1500.0,
            flowRateLpm = 15.0,
            safeResidualPsi = 200.0
        )
        assertNotNull(result)
        assertEquals(13.867, result!!.usableMinutes, 0.01)
        assertEquals(16.0, result.totalMinutesToZero, 0.01)
        assertEquals(OxygenStatus.CAUTION, result.status)
    }

    @Test
    fun `oxygen M cylinder at 2000 PSI on 10 Lpm has safe duration`() {
        // (2000 - 200) * 1.56 / 10 = 1800 * 1.56 / 10 = 280.8 min
        val result = MedicMath.calculateOxygenDuration(
            cylinder = OxygenCylinder.M,
            currentPsi = 2000.0,
            flowRateLpm = 10.0,
            safeResidualPsi = 200.0
        )
        assertNotNull(result)
        assertEquals(280.8, result!!.usableMinutes, 0.01)
        assertEquals(OxygenStatus.SAFE, result.status)
    }

    @Test
    fun `syringe dose converts mcg desired from mg vial accurately`() {
        // Desired 300 mcg from 1 mg / 1 mL vial -> 0.3 mL in a 1 mL syringe
        val result = MedicMath.calculateSyringeDose(
            desiredDose = 300.0,
            desiredUnit = MedicationUnit.MCG,
            isWeightBased = false,
            weight = null,
            weightUnit = WeightUnit.KG,
            drugOnHand = 1.0,
            onHandUnit = MedicationUnit.MG,
            volumeOnHandMl = 1.0
        )
        assertNotNull(result)
        assertNull(result!!.error)
        assertEquals(0.3, result.volumeToDrawMl, 0.001)
        assertEquals(1, result.recommendedSyringeMl)
        assertEquals(1, result.vialsNeeded)
    }

    @Test
    fun `syringe weight-based dose calculates total dose and vials`() {
        // 1 mcg/kg for 80 kg = 80 mcg; from 100 mcg / 2 mL (50 mcg/mL) -> 1.6 mL in 3 mL syringe
        val result = MedicMath.calculateSyringeDose(
            desiredDose = 1.0,
            desiredUnit = MedicationUnit.MCG,
            isWeightBased = true,
            weight = 80.0,
            weightUnit = WeightUnit.KG,
            drugOnHand = 100.0,
            onHandUnit = MedicationUnit.MCG,
            volumeOnHandMl = 2.0
        )
        assertNotNull(result)
        assertEquals(1.6, result!!.volumeToDrawMl, 0.001)
        assertEquals(3, result.recommendedSyringeMl)
        assertEquals(1, result.vialsNeeded)
    }

    @Test
    fun `syringe incompatible units returns clear error`() {
        val result = MedicMath.calculateSyringeDose(
            desiredDose = 10.0,
            desiredUnit = MedicationUnit.UNITS,
            isWeightBased = false,
            weight = null,
            weightUnit = WeightUnit.KG,
            drugOnHand = 10.0,
            onHandUnit = MedicationUnit.MG,
            volumeOnHandMl = 2.0
        )
        assertNotNull(result)
        assertNotNull(result!!.error)
    }

    @Test
    fun `devine ideal body weight matches male and female formulas`() {
        // Male 5 ft 10 in (70 in): 50 + 2.3 * 10 = 73.0 kg
        val male = MedicMath.calculateIbw(
            sex = PatientSex.MALE,
            heightUnit = HeightUnit.FT_IN,
            feet = 5.0,
            inches = 10.0,
            cm = null
        )
        assertNotNull(male)
        assertEquals(73.0, male!!.ibwKg, 0.01)
        assertEquals(438, male.tidalVolumeLowMl)
        assertEquals(584, male.tidalVolumeHighMl)

        // Female 5 ft 4 in (64 in): 45.5 + 2.3 * 4 = 54.7 kg
        val female = MedicMath.calculateIbw(
            sex = PatientSex.FEMALE,
            heightUnit = HeightUnit.FT_IN,
            feet = 5.0,
            inches = 4.0,
            cm = null
        )
        assertNotNull(female)
        assertEquals(54.7, female!!.ibwKg, 0.01)
        assertTrue(!female.isUnderFiveFeet)
    }

    @Test
    fun `glasgow coma scale computes totals and severity bands accurately`() {
        val mild = MedicMath.calculateGcs(
            eye = GcsEye.SPONTANEOUS,
            verbal = GcsVerbal.CONFUSED,
            motor = GcsMotor.OBEYS_COMMANDS
        )
        assertNotNull(mild)
        assertEquals(14, mild!!.totalScore)
        assertEquals(GcsSeverity.MILD, mild.severity)
        assertTrue(!mild.airwayAlert)

        val moderate = MedicMath.calculateGcs(
            eye = GcsEye.VERBAL,
            verbal = GcsVerbal.INAPPROPRIATE_WORDS,
            motor = GcsMotor.WITHDRAWS_PAIN
        )
        assertNotNull(moderate)
        assertEquals(10, moderate!!.totalScore)
        assertEquals(GcsSeverity.MODERATE, moderate.severity)
        assertTrue(!moderate.airwayAlert)

        val severe = MedicMath.calculateGcs(
            eye = GcsEye.PAIN,
            verbal = GcsVerbal.INCOMPREHENSIBLE,
            motor = GcsMotor.WITHDRAWS_PAIN
        )
        assertNotNull(severe)
        assertEquals(8, severe!!.totalScore)
        assertEquals(GcsSeverity.SEVERE, severe.severity)
        assertTrue(severe.airwayAlert)

        val incomplete = MedicMath.calculateGcs(
            eye = GcsEye.SPONTANEOUS,
            verbal = null,
            motor = GcsMotor.OBEYS_COMMANDS
        )
        assertNull(incomplete)
    }

    @Test
    fun `air methods pcg drip protocol validation flags underdose and overdose`() {
        val dopamine = MedicMath.findDripPreset("Dopamine")!!
        val within = MedicMath.validateDripDose(
            preset = dopamine,
            desiredDose = 5.0,
            doseRateUnit = DoseRateUnit.MCG_KG_MIN,
            mode = DripMode.MED_INFUSION,
            bagVolumeMl = 250.0,
            timeValue = null,
            timeUnit = TimeUnit.MINUTES
        )
        assertEquals(DoseRangeStatus.WITHIN_RANGE, within.status)

        val tooLow = MedicMath.validateDripDose(
            preset = dopamine,
            desiredDose = 1.0,
            doseRateUnit = DoseRateUnit.MCG_KG_MIN,
            mode = DripMode.MED_INFUSION,
            bagVolumeMl = 250.0,
            timeValue = null,
            timeUnit = TimeUnit.MINUTES
        )
        assertEquals(DoseRangeStatus.BELOW_MIN, tooLow.status)

        val tooHigh = MedicMath.validateDripDose(
            preset = dopamine,
            desiredDose = 25.0,
            doseRateUnit = DoseRateUnit.MCG_KG_MIN,
            mode = DripMode.MED_INFUSION,
            bagVolumeMl = 250.0,
            timeValue = null,
            timeUnit = TimeUnit.MINUTES
        )
        assertEquals(DoseRangeStatus.ABOVE_MAX, tooHigh.status)

        // Norepinephrine supports both mcg/min (2-30) and mcg/kg/min (0.05-2)
        val norepi = MedicMath.findDripPreset("Norepi")!!
        assertEquals("8", norepi.drugAmount)
        val norepiKg = MedicMath.validateDripDose(
            preset = norepi,
            desiredDose = 0.5,
            doseRateUnit = DoseRateUnit.MCG_KG_MIN,
            mode = DripMode.MED_INFUSION,
            bagVolumeMl = 250.0,
            timeValue = null,
            timeUnit = TimeUnit.MINUTES
        )
        assertEquals(DoseRangeStatus.WITHIN_RANGE, norepiKg.status)

        // Vasopressin shock range 0.01 - 0.04 units/min
        val vasopressin = MedicMath.findDripPreset("Vasopressin")!!
        val vasoOk = MedicMath.validateDripDose(
            preset = vasopressin,
            desiredDose = 0.03,
            doseRateUnit = DoseRateUnit.UNITS_MIN,
            mode = DripMode.MED_INFUSION,
            bagVolumeMl = 100.0,
            timeValue = null,
            timeUnit = TimeUnit.MINUTES
        )
        assertEquals(DoseRangeStatus.WITHIN_RANGE, vasoOk.status)
        val vasoHigh = MedicMath.validateDripDose(
            preset = vasopressin,
            desiredDose = 0.1,
            doseRateUnit = DoseRateUnit.UNITS_MIN,
            mode = DripMode.MED_INFUSION,
            bagVolumeMl = 100.0,
            timeValue = null,
            timeUnit = TimeUnit.MINUTES
        )
        assertEquals(DoseRangeStatus.ABOVE_MAX, vasoHigh.status)
    }

    @Test
    fun `air methods pcg syringe protocol validation checks range and max single dose`() {
        val fentanyl = MedicMath.findSyringePreset("Fentanyl")!!
        // 1 mcg/kg at 80 kg = 80 mcg <= 100 mcg max single dose
        val fentanylOk = MedicMath.validateSyringeDose(
            preset = fentanyl,
            desiredDose = 1.0,
            desiredUnit = MedicationUnit.MCG,
            isWeightBased = true,
            weight = 80.0,
            weightUnit = WeightUnit.KG
        )
        assertEquals(DoseRangeStatus.WITHIN_RANGE, fentanylOk.status)

        // 1 mcg/kg at 120 kg = 120 mcg > 100 mcg max single dose
        val fentanylMaxExceeded = MedicMath.validateSyringeDose(
            preset = fentanyl,
            desiredDose = 1.0,
            desiredUnit = MedicationUnit.MCG,
            isWeightBased = true,
            weight = 120.0,
            weightUnit = WeightUnit.KG
        )
        assertEquals(DoseRangeStatus.MAX_SINGLE_EXCEEDED, fentanylMaxExceeded.status)

        // 0.2 mcg/kg below 0.5 min
        val fentanylLow = MedicMath.validateSyringeDose(
            preset = fentanyl,
            desiredDose = 0.2,
            desiredUnit = MedicationUnit.MCG,
            isWeightBased = true,
            weight = 70.0,
            weightUnit = WeightUnit.KG
        )
        assertEquals(DoseRangeStatus.BELOW_MIN, fentanylLow.status)

        // Ondansetron fixed 4 mg adult & 0.1 mg/kg peds
        val ondansetron = MedicMath.findSyringePreset("Ondansetron")!!
        assertEquals(
            DoseRangeStatus.WITHIN_RANGE,
            MedicMath.validateSyringeDose(ondansetron, 4.0, MedicationUnit.MG, false, null, WeightUnit.KG).status
        )
        assertEquals(
            DoseRangeStatus.ABOVE_MAX,
            MedicMath.validateSyringeDose(ondansetron, 8.0, MedicationUnit.MG, false, null, WeightUnit.KG).status
        )
        assertEquals(
            DoseRangeStatus.WITHIN_RANGE,
            MedicMath.validateSyringeDose(ondansetron, 0.1, MedicationUnit.MG, true, 20.0, WeightUnit.KG).status
        )
    }

    @Test
    fun `generic drug is first preset in drip and syringe lists with custom info status`() {
        val firstDrip = MedicMath.DRIP_PRESETS.first()
        assertEquals("Generic", firstDrip.name)
        val dripValidation = MedicMath.validateDripDose(
            preset = firstDrip,
            desiredDose = 12.0,
            doseRateUnit = DoseRateUnit.MCG_KG_MIN,
            mode = DripMode.MED_INFUSION,
            bagVolumeMl = 250.0,
            timeValue = null,
            timeUnit = TimeUnit.MINUTES
        )
        assertEquals(DoseRangeStatus.INFO_ONLY, dripValidation.status)

        val firstSyringe = MedicMath.SYRINGE_PRESETS.first()
        assertEquals("Generic", firstSyringe.name)
        val syringeValidation = MedicMath.validateSyringeDose(
            preset = firstSyringe,
            desiredDose = 75.0,
            desiredUnit = MedicationUnit.MCG,
            isWeightBased = false,
            weight = null,
            weightUnit = WeightUnit.KG
        )
        assertEquals(DoseRangeStatus.INFO_ONLY, syringeValidation.status)
    }
}
