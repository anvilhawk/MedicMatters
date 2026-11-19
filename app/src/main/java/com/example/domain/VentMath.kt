package com.example.domain

import kotlin.math.roundToInt

enum class VentSubMode(val label: String) {
    SETUP("Protocol Setup"),
    TROUBLESHOOT("Troubleshoot")
}

enum class VentStrategy(
    val shortLabel: String,
    val title: String,
    val pcgSection: String,
    val vtLowMlPerKg: Double,
    val vtHighMlPerKg: Double,
    val defaultVtMlPerKg: Double,
    val targetVeMlPerKgMin: Double,
    val defaultPeep: String,
    val defaultIe: String,
    val modeGuidance: String,
    val etco2Target: String,
    val spo2Target: String
) {
    NORMAL(
        shortLabel = "Normal",
        title = "Normal Lung Ventilation",
        pcgSection = "PCG 7.33.5.1",
        vtLowMlPerKg = 6.0,
        vtHighMlPerKg = 8.0,
        defaultVtMlPerKg = 7.0,
        targetVeMlPerKgMin = 100.0,
        defaultPeep = "3–5 cmH2O",
        defaultIe = "1:2",
        modeGuidance = "APVcmv or PC (Start 20/5: Pcontrol 15 + PEEP 5)",
        etco2Target = "35–45 mmHg",
        spo2Target = "94–98% (FiO2 1.0 → wean)"
    ),
    METABOLIC_ACIDOSIS(
        shortLabel = "Met. Acidosis",
        title = "Metabolic Acidosis (pH < 7.25 / AG > 16)",
        pcgSection = "PCG 7.33.5.2",
        vtLowMlPerKg = 6.0,
        vtHighMlPerKg = 8.0,
        defaultVtMlPerKg = 7.0,
        targetVeMlPerKgMin = 120.0,
        defaultPeep = "5 cmH2O",
        defaultIe = "1:2",
        modeGuidance = "PCV+ / PC (Avoid APV & PRVC — high drive causes pressure drops)",
        etco2Target = "25–30 mmHg (NEVER < 25)",
        spo2Target = "94–98% (Titrate FiO2)"
    ),
    ARDS(
        shortLabel = "ARDS / Stiff",
        title = "ARDS / Low Compliance (Lung Protective)",
        pcgSection = "PCG 7.33.5.4 / 7.34.6.4",
        vtLowMlPerKg = 4.0,
        vtHighMlPerKg = 6.0,
        defaultVtMlPerKg = 6.0,
        targetVeMlPerKgMin = 100.0,
        defaultPeep = "5–15+ cmH2O (PEEP over FiO2)",
        defaultIe = "1:2",
        modeGuidance = "APVcmv or PC — MUST switch to PC if Pplat > 30 cmH2O",
        etco2Target = "Permissive hypercapnia (pH ≥ 7.15)",
        spo2Target = "88–95% (PaO2 55–80)"
    ),
    OBSTRUCTIVE(
        shortLabel = "Asthma / COPD",
        title = "Obstructive / Bronchospasm",
        pcgSection = "PCG 7.34.6.2 / Table 7.16",
        vtLowMlPerKg = 6.0,
        vtHighMlPerKg = 8.0,
        defaultVtMlPerKg = 7.0,
        targetVeMlPerKgMin = 85.0,
        defaultPeep = "3–5 cmH2O (Watch AutoPEEP)",
        defaultIe = "1:4 – 1:6 (Short TI, Long TE)",
        modeGuidance = "PC / VCV (Avoid PRVC — variable airway resistance drops VT)",
        etco2Target = "Permissive hypercapnia (pH ≥ 7.20)",
        spo2Target = "88–92% (COPD) / >93% (Asthma)"
    ),
    PERFUSION_LIMITED(
        shortLabel = "Shock (SBP<80)",
        title = "Perfusion Limited Ventilation (PLV)",
        pcgSection = "PCG 7.33.5.3",
        vtLowMlPerKg = 10.0,
        vtHighMlPerKg = 12.0,
        defaultVtMlPerKg = 10.0,
        targetVeMlPerKgMin = 100.0,
        defaultPeep = "5 cmH2O (Avoid high mean pressure)",
        defaultIe = "1:2 (Long expiratory pause)",
        modeGuidance = "Volume / APVcmv (Fixed RR = 10 bpm; ↑ VT to 12 if ETCO2 > 45)",
        etco2Target = "35–45 mmHg (Restores venous return)",
        spo2Target = "80–89% tolerable in profound shock; restore normal settings when SBP ≥ 90"
    ),
    TBI_NEURO(
        shortLabel = "TBI / Neuro",
        title = "Traumatic Brain Injury / ICP Protection",
        pcgSection = "PCG 8.13.4",
        vtLowMlPerKg = 6.0,
        vtHighMlPerKg = 8.0,
        defaultVtMlPerKg = 7.0,
        targetVeMlPerKgMin = 100.0,
        defaultPeep = "5 cmH2O (Avoid > 5 unless hypoxic)",
        defaultIe = "1:2",
        modeGuidance = "APVcmv or PC — keep Pplat < 30 & head elevated 30°",
        etco2Target = "35–40 mmHg (30–35 ONLY if impending herniation)",
        spo2Target = "≥ 95% (Strictly avoid hypoxia & hypotension)"
    ),
    PEDIATRIC(
        shortLabel = "Pediatric",
        title = "Pediatric / Infant Ventilation",
        pcgSection = "PCG 7.34.6 / Form 5805-CS",
        vtLowMlPerKg = 6.0,
        vtHighMlPerKg = 8.0,
        defaultVtMlPerKg = 7.0,
        targetVeMlPerKgMin = 150.0,
        defaultPeep = "5 cmH2O",
        defaultIe = "1:2 (TI 0.3–1.0 s by age)",
        modeGuidance = "PC / PSIMV MANDATORY < 10 kg (Start 20/5, P-ramp 25 ms, Neo circuit < 10 kg)",
        etco2Target = "35–45 mmHg (Avoid < 35 unless herniating)",
        spo2Target = "94–98% (Wean FiO2 as tolerated)"
    )
}

data class VentSetupResult(
    val strategy: VentStrategy,
    val weightKg: Double,
    val weightSourceLabel: String,
    val vtMlPerKg: Double,
    val targetVtMl: Int,
    val vtRangeLowMl: Int,
    val vtRangeHighMl: Int,
    val targetVeLMin: Double,
    val minVeLabel: String,
    val startingRr: Int,
    val highPressureAlarmGuidance: String,
    val cuffPressureGuidance: String,
    val isVtOutsideStrategyRange: Boolean,
    val protocolWarnings: List<String>
)

data class PedsVentCardRow(
    val age: String,
    val weightKg: String,
    val rateBpm: String,
    val iTimeSec: String,
    val vtMl: String
)

enum class VentAlertSeverity {
    NORMAL,
    CAUTION,
    CRITICAL
}

data class VentMechanicsScenario(
    val name: String,
    val subtitle: String,
    val cstat: String,
    val rinsp: String,
    val rcexp: String,
    val ppeak: String,
    val pplat: String,
    val peep: String,
    val vte: String,
    val autoPeep: String,
    val vLeakPercent: String,
    val rr: String,
    val tiSec: String,
    val p01: String
)

data class VentFinding(
    val severity: VentAlertSeverity,
    val parameter: String,
    val headline: String,
    val causes: String,
    val action: String
)

data class VentMechanicsResult(
    val overallSeverity: VentAlertSeverity,
    val headlineDiagnosis: String,
    val pipMinusPplat: Double?,
    val computedCstat: Double?,
    val predictedPplat: Double?,
    val minTeThreeRcExp: Double?,
    val actualTeSec: Double?,
    val maxSafeRr: Int?,
    val computedPmean: Double?,
    val findings: List<VentFinding>
)

data class VentGuideCard(
    val title: String,
    val badge: String,
    val bullets: List<String>
)

object VentMath {

    val PEDS_VENT_CARD_ROWS = listOf(
        PedsVentCardRow("Preemie", "2 kg", "40–70", "0.2–0.3 s", "8–12 mL"),
        PedsVentCardRow("Newborn", "4 kg", "30–60", "0.4–0.6 s", "24–32 mL"),
        PedsVentCardRow("4 mo", "6 kg", "30–60", "0.4–0.6 s", "36–48 mL"),
        PedsVentCardRow("6 mo", "8 kg", "24–38", "0.4–0.6 s", "48–64 mL"),
        PedsVentCardRow("1 yr", "10 kg", "22–30", "0.6–0.7 s", "60–80 mL"),
        PedsVentCardRow("2 yr", "12 kg", "22–30", "0.7 s", "72–96 mL"),
        PedsVentCardRow("3 yr", "15 kg", "22–30", "0.7 s", "90–120 mL"),
        PedsVentCardRow("4 yr", "17 kg", "22–26", "0.7 s", "102–136 mL"),
        PedsVentCardRow("5 yr", "20 kg", "20–24", "0.7–0.9 s", "120–160 mL"),
        PedsVentCardRow("6 yr", "22 kg", "20–24", "0.7–0.9 s", "132–176 mL"),
        PedsVentCardRow("7–8 yr", "25–27 kg", "16–22", "0.7–0.9 s", "150–216 mL"),
        PedsVentCardRow("9–11 yr", "30–40 kg", "16–22", "0.7–1.0 s", "180–320 mL"),
        PedsVentCardRow("12–13 yr", "50–60 kg", "16–22", "0.9–1.0 s", "300–480 mL")
    )

    val MECHANICS_SCENARIOS = listOf(
        VentMechanicsScenario(
            name = "Normal",
            subtitle = "Healthy Mechanics",
            cstat = "55",
            rinsp = "10",
            rcexp = "0.7",
            ppeak = "22",
            pplat = "18",
            peep = "5",
            vte = "480",
            autoPeep = "0",
            vLeakPercent = "2",
            rr = "14",
            tiSec = "1.0",
            p01 = "-1.5"
        ),
        VentMechanicsScenario(
            name = "High Rinsp",
            subtitle = "Bronchospasm / Kink",
            cstat = "52",
            rinsp = "28",
            rcexp = "1.4",
            ppeak = "42",
            pplat = "19",
            peep = "5",
            vte = "450",
            autoPeep = "4",
            vLeakPercent = "3",
            rr = "18",
            tiSec = "1.0",
            p01 = "-3.5"
        ),
        VentMechanicsScenario(
            name = "Low Cstat",
            subtitle = "ARDS / PTX / Stiff",
            cstat = "26",
            rinsp = "11",
            rcexp = "0.4",
            ppeak = "36",
            pplat = "33",
            peep = "10",
            vte = "420",
            autoPeep = "0",
            vLeakPercent = "2",
            rr = "22",
            tiSec = "0.9",
            p01 = "-2.5"
        ),
        VentMechanicsScenario(
            name = "Air Trapping",
            subtitle = "High RCexp / AutoPEEP",
            cstat = "78",
            rinsp = "22",
            rcexp = "1.6",
            ppeak = "31",
            pplat = "20",
            peep = "5",
            vte = "460",
            autoPeep = "7",
            vLeakPercent = "2",
            rr = "20",
            tiSec = "1.2",
            p01 = "-4.0"
        ),
        VentMechanicsScenario(
            name = "Cuff Leak",
            subtitle = "High VLeak%",
            cstat = "54",
            rinsp = "9",
            rcexp = "0.7",
            ppeak = "18",
            pplat = "15",
            peep = "5",
            vte = "310",
            autoPeep = "0",
            vLeakPercent = "38",
            rr = "16",
            tiSec = "1.0",
            p01 = "-2.0"
        )
    )

    val TROUBLESHOOTING_GUIDES = listOf(
        VentGuideCard(
            title = "DOPES — Acute Desat / High Pressure Alarm",
            badge = "PRIORITY",
            bullets = listOf(
                "D — Dislodgement / Right Mainstem: Check ETCO2 waveform, bilateral breath sounds, ETT depth at teeth/lips.",
                "O — Obstruction: Suction ETT, check for kinks, biting, wet/occluded HME filter, or small ETT raising Rinsp.",
                "P — Pneumothorax: Hypoxia + high Ppeak/Pplat + ↓ Cstat + ↓ breath sounds/hypotension → needle/finger thoracostomy.",
                "E — Equipment Failure: Check O2 source/PSI, circuit disconnects, flow sensor, disconnect & bag with PEEP valve to isolate.",
                "S — Stacked Breaths (AutoPEEP) / Sedation: Disconnect circuit briefly to allow full exhalation, deepen sedation/analgesia."
            )
        ),
        VentGuideCard(
            title = "Cstat (Static Compliance) — Ref: 50–60 mL/cmH2O",
            badge = "Cstat = VTE ÷ (Pplat − PEEP)",
            bullets = listOf(
                "Low Cstat (< 50, stiff < 40): ARDS, pulmonary edema, pneumonia, pneumothorax, right mainstem, abdominal distension, chest wall eschar/burn.",
                "Low Cstat Action: Drop VT to 4–6 mL/kg IBW, keep Pplat < 30 cmH2O, switch to PC if Pplat > 30, optimize PEEP.",
                "High Cstat (> 70, floppy > 80): Emphysema / COPD (high compliance + high RCexp). Use obstructive settings (I:E 1:4–1:6) & watch AutoPEEP.",
                "Caveat: Unreliable during active spontaneous breathing effort and INVALID on NIV."
            )
        ),
        VentGuideCard(
            title = "Rinsp (Inspiratory Resistance) — Ref: 5–15 cmH2O/(L/s)",
            badge = "PIP − Pplat > 5",
            bullets = listOf(
                "5–7 = 'Happy pipes' (minimal airway resistance). 10–15 = Normal intubated.",
                "Elevated (> 15, COPD 15–30): Bronchospasm, secretions/mucus plug, kinked/small ETT, wet HME, water in circuit.",
                "Rinsp Action: Run DOPES, inline suction, give bronchodilators, warm/replace clogged HME, slow P-ramp (25 ms peds) if coughing/bucking.",
                "Note: Small ETT sizes naturally elevate Rinsp; INVALID on NIV."
            )
        ),
        VentGuideCard(
            title = "RCexp (Expiratory Time Constant) & AutoPEEP",
            badge = "Min TE = 3 × RCexp",
            bullets = listOf(
                "Normal RCexp: 0.5–0.9 s. Low (< 0.5 s): ARDS/restrictive (empties fast). High (> 0.9–1.2 s): COPD/Asthma (empties slowly).",
                "Emptying Math: 1×RCexp = 63% | 2× = 86.5% | 3× = 95% | 4× = 98%. Always keep actual TE ≥ 3 × RCexp.",
                "If AutoPEEP > 0 or TE < 3 × RCexp: Lower RR, shorten TI (target I:E 1:4–1:6), treat bronchospasm, deepen sedation."
            )
        ),
        VentGuideCard(
            title = "Low VTE, High fTotal, P0.1 & Regulation Pressure Limited",
            badge = "HAMILTON ALARMS",
            bullets = listOf(
                "Low VTE (>10% below target): 1) Rule out AutoPEEP (0), 2) Check VLeak% / ETT cuff (20–30 adult, 20–25 peds) & circuit, 3) Check dead space (HME 9 mL peds / 80 mL adult). Increase VT or PC while keeping Ppeak < 35.",
                "High fTotal: Check rotor/aircraft vibration causing auto-triggering (raise flow trigger sensitivity), pain/under-sedation, or metabolic acidosis.",
                "P0.1 (Airway Occlusion 100 ms): 0 to -3 normal; -3 to -4 strong effort; < -5 excessive work of breathing → ↑ PS/pinsp & shorten P-ramp.",
                "'Regulation Pressure Limited' (APV): Vent stops breath 5–10 cmH2O below Pmax alarm → risk of hypoventilation; run DOPES & consider PC."
            )
        ),
        VentGuideCard(
            title = "NIV (CPAP / BiPAP) Troubleshooting",
            badge = "PCG 7.16 / 7.20",
            bullets = listOf(
                "VLeak > 25%: Reseat mask, tighten/adjust headgear straps without excess bridge pressure, check chin/dentures/beard seal.",
                "Do NOT use Cstat or Rinsp on NIV — readings are invalid in non-invasive mode.",
                "Monitor synchrony, rising PCO2, or worsening mental status/fatigue → prepare early for endotracheal intubation."
            )
        )
    )

    fun calculateSetup(
        strategy: VentStrategy,
        weightKg: Double?,
        weightSourceLabel: String,
        customVtMlPerKg: Double? = null
    ): VentSetupResult? {
        if (weightKg == null || weightKg <= 0.0) return null

        val isPediatric = strategy == VentStrategy.PEDIATRIC || weightKg < 30.0
        val effectiveVtMlPerKg = customVtMlPerKg?.takeIf { it in 3.0..14.0 } ?: strategy.defaultVtMlPerKg
        val targetVtMl = (effectiveVtMlPerKg * weightKg).roundToInt().coerceAtLeast(5)
        val vtLowMl = (strategy.vtLowMlPerKg * weightKg).roundToInt().coerceAtLeast(4)
        val vtHighMl = (strategy.vtHighMlPerKg * weightKg).roundToInt().coerceAtLeast(6)

        val effectiveVePerKg = when {
            strategy == VentStrategy.PEDIATRIC && weightKg < 10.0 -> 200.0
            strategy == VentStrategy.PEDIATRIC -> 150.0
            strategy == VentStrategy.PERFUSION_LIMITED -> (effectiveVtMlPerKg * 10.0) // RR 10 fixed
            else -> strategy.targetVeMlPerKgMin
        }

        val targetVeMlMin = effectiveVePerKg * weightKg
        val targetVeLMin = targetVeMlMin / 1000.0

        val startingRr = if (strategy == VentStrategy.PERFUSION_LIMITED) {
            10
        } else {
            (targetVeMlMin / targetVtMl.toDouble()).roundToInt().coerceIn(8, 60)
        }

        val minVeLabel = when {
            strategy == VentStrategy.PEDIATRIC && weightKg < 10.0 -> "200 mL/kg/min (<10 kg)"
            strategy == VentStrategy.PEDIATRIC -> "150 mL/kg/min (≥10 kg)"
            strategy == VentStrategy.PERFUSION_LIMITED -> "Fixed RR 10 × VT (~${MedicMath.formatNumber(effectiveVePerKg, 0)} mL/kg/min)"
            else -> "${MedicMath.formatNumber(effectiveVePerKg, 0)} mL/kg/min"
        }

        val isOutside = effectiveVtMlPerKg < strategy.vtLowMlPerKg - 0.01 ||
            effectiveVtMlPerKg > strategy.vtHighMlPerKg + 0.01

        val warnings = mutableListOf<String>()
        if (isOutside) {
            warnings.add(
                "Selected VT (${MedicMath.formatNumber(effectiveVtMlPerKg, 1)} mL/kg) is outside ${strategy.shortLabel} range (${MedicMath.formatNumber(strategy.vtLowMlPerKg, 0)}–${MedicMath.formatNumber(strategy.vtHighMlPerKg, 0)} mL/kg)."
            )
        }
        when (strategy) {
            VentStrategy.METABOLIC_ACIDOSIS -> {
                warnings.add("Avoid APV / PRVC in severe metabolic acidosis; maintain patient's compensatory minute ventilation & never lower ETCO2 < 25 mmHg.")
            }
            VentStrategy.OBSTRUCTIVE -> {
                warnings.add("Avoid PRVC; allow prolonged exhalation (I:E 1:4–1:6) and monitor AutoPEEP + VTE closely.")
            }
            VentStrategy.ARDS -> {
                warnings.add("Keep Pplat < 30 cmH2O (switch to Pressure Control if Pplat > 30). Prioritize PEEP titration over high FiO2.")
            }
            VentStrategy.PERFUSION_LIMITED -> {
                warnings.add("Reserve PLV for refractory shock (SBP < 80 mmHg). Immediately restore normal lung-protective vent settings once SBP ≥ 90 mmHg.")
            }
            VentStrategy.TBI_NEURO -> {
                warnings.add("Avoid prophylactic hyperventilation; keep ETCO2 35–40 mmHg (30–35 mmHg ONLY with signs of impending herniation).")
            }
            VentStrategy.PEDIATRIC -> {
                if (weightKg < 10.0) {
                    warnings.add("< 10 kg: PC / PSIMV mode & neonatal flow sensor/circuit mandatory initially (start PIP 20 / PEEP 5, P-ramp 25 ms, check HME dead space 9 mL).")
                } else {
                    warnings.add("Consult pediatric vent card for age/weight rate & TI; Med Control required if PEEP > 15 cmH2O.")
                }
            }
            VentStrategy.NORMAL -> {
                warnings.add("Monitor Ppeak < 35 & Pplat < 30 cmH2O; re-check ETT cuff pressure at baseline, altitude, and after landing.")
            }
        }

        val cuffGuidance = if (isPediatric) {
            "20–25 cmH2O (Check baseline, altitude & landing)"
        } else {
            "20–30 cmH2O (Check baseline, altitude & landing)"
        }

        return VentSetupResult(
            strategy = strategy,
            weightKg = weightKg,
            weightSourceLabel = weightSourceLabel,
            vtMlPerKg = effectiveVtMlPerKg,
            targetVtMl = targetVtMl,
            vtRangeLowMl = vtLowMl,
            vtRangeHighMl = vtHighMl,
            targetVeLMin = targetVeLMin,
            minVeLabel = minVeLabel,
            startingRr = startingRr,
            highPressureAlarmGuidance = "10 cmH2O above Pplat (Keep Ppeak < 35, Pplat < 30)",
            cuffPressureGuidance = cuffGuidance,
            isVtOutsideStrategyRange = isOutside,
            protocolWarnings = warnings
        )
    }

    fun analyzeMechanics(
        cstat: Double?,
        rinsp: Double?,
        rcexp: Double?,
        ppeak: Double?,
        pplat: Double?,
        peep: Double?,
        vte: Double?,
        autoPeep: Double?,
        vLeakPercent: Double?,
        rr: Double?,
        tiSec: Double?,
        p01: Double?
    ): VentMechanicsResult {
        val pipMinusPplat = if (ppeak != null && pplat != null && ppeak >= pplat) {
            ppeak - pplat
        } else null

        val computedCstat = if (vte != null && pplat != null && peep != null && pplat > peep && vte > 0.0) {
            vte / (pplat - peep)
        } else null

        val effectiveCstat = cstat ?: computedCstat

        val predictedPplat = if (vte != null && effectiveCstat != null && effectiveCstat > 0.0 && peep != null) {
            (vte / effectiveCstat) + peep
        } else null

        val minTeThreeRcExp = rcexp?.takeIf { it > 0.0 }?.let { it * 3.0 }

        val actualTeSec = if (rr != null && rr > 0.0 && tiSec != null && tiSec > 0.0) {
            val cycleSec = 60.0 / rr
            (cycleSec - tiSec).coerceAtLeast(0.0)
        } else null

        val maxSafeRr = if (rcexp != null && rcexp > 0.0 && tiSec != null && tiSec > 0.0) {
            val minCycle = tiSec + (3.0 * rcexp)
            if (minCycle > 0.0) (60.0 / minCycle).toInt().coerceAtLeast(1) else null
        } else null

        val computedPmean = if (ppeak != null && peep != null && tiSec != null && actualTeSec != null && (tiSec + actualTeSec) > 0.0) {
            ((tiSec * ppeak) + (actualTeSec * peep)) / (tiSec + actualTeSec)
        } else null

        val findings = mutableListOf<VentFinding>()

        // 1. PIP - Pplat Gradient (Resistance vs Compliance differentiator)
        if (pipMinusPplat != null) {
            if (pipMinusPplat > 5.0) {
                findings.add(
                    VentFinding(
                        severity = if (pipMinusPplat > 15.0) VentAlertSeverity.CRITICAL else VentAlertSeverity.CAUTION,
                        parameter = "PIP − Pplat = ${MedicMath.formatNumber(pipMinusPplat, 0)} cmH2O (> 5)",
                        headline = "Airway Resistance Problem (Normal Alveolar Pplat)",
                        causes = "Bronchospasm, secretions/mucus plug, kinked/bitten ETT, small ETT, water in circuit, clogged/wet HME.",
                        action = "Run DOPES: inline suction, check ETT & HME, give bronchodilators, consider bite block/sedation."
                    )
                )
            } else if (pplat != null && pplat > 30.0) {
                findings.add(
                    VentFinding(
                        severity = VentAlertSeverity.CRITICAL,
                        parameter = "Pplat = ${MedicMath.formatNumber(pplat, 0)} cmH2O (PIP−Pplat ≤ 5)",
                        headline = "Lung / Chest Wall Compliance Problem (Barotrauma Risk)",
                        causes = "ARDS, pulmonary edema, pneumonia, pneumothorax, right mainstem, abdominal compartment, chest eschar, or excessive VT.",
                        action = "PCG 7.33.5.4: Reduce VT to 4–6 mL/kg IBW, keep Pplat < 30 cmH2O, switch to Pressure Control, R/O pneumothorax/mainstem."
                    )
                )
            }
        } else if (pplat != null && pplat > 30.0) {
            findings.add(
                VentFinding(
                    severity = VentAlertSeverity.CRITICAL,
                    parameter = "Pplat = ${MedicMath.formatNumber(pplat, 0)} cmH2O (> 30)",
                    headline = "Elevated Plateau Pressure",
                    causes = "Stiff lungs (ARDS/edema), pneumothorax, mainstem intubation, overdistension.",
                    action = "Drop VT (4–6 mL/kg) and switch from APV to Pressure Control if Pplat remains > 30."
                )
            )
        }

        // 2. Peak Pressure
        if (ppeak != null && ppeak >= 35.0) {
            findings.add(
                VentFinding(
                    severity = if (ppeak >= 40.0) VentAlertSeverity.CRITICAL else VentAlertSeverity.CAUTION,
                    parameter = "Ppeak = ${MedicMath.formatNumber(ppeak, 0)} cmH2O (Ideal < 35)",
                    headline = "High Peak Inspiratory Pressure",
                    causes = "If PIP−Pplat > 5: airway obstruction/bronchospasm. If PIP−Pplat ≤ 5: low compliance or AutoPEEP.",
                    action = "Check High Pressure alarm (Pplat + 10); if 'Regulation Pressure Limited' in APV, run DOPES & switch to PC if needed."
                )
            )
        }

        // 3. Cstat analysis
        if (effectiveCstat != null) {
            when {
                effectiveCstat < 40.0 -> findings.add(
                    VentFinding(
                        severity = VentAlertSeverity.CRITICAL,
                        parameter = "Cstat = ${MedicMath.formatNumber(effectiveCstat, 0)} mL/cmH2O (< 40)",
                        headline = "Severely Reduced Compliance (Stiff Lung / PTX)",
                        causes = "ARDS, severe pulmonary edema, pneumothorax, right mainstem, thoracic burn eschar, overdistension.",
                        action = "Lung-protective VT 4–6 mL/kg, keep Pplat < 30, switch to PC, optimize PEEP, rule out PTX."
                    )
                )
                effectiveCstat < 50.0 -> findings.add(
                    VentFinding(
                        severity = VentAlertSeverity.CAUTION,
                        parameter = "Cstat = ${MedicMath.formatNumber(effectiveCstat, 0)} mL/cmH2O (Ref 50–60)",
                        headline = "Mildly Reduced Static Compliance",
                        causes = "Early pulmonary edema, atelectasis, mild ARDS, obesity, ascites.",
                        action = "Ensure VT ≤ 6–8 mL/kg IBW, keep Pplat < 30, reassess PEEP titration."
                    )
                )
                effectiveCstat > 70.0 -> findings.add(
                    VentFinding(
                        severity = VentAlertSeverity.CAUTION,
                        parameter = "Cstat = ${MedicMath.formatNumber(effectiveCstat, 0)} mL/cmH2O (> 70)",
                        headline = "High Compliance (Floppy Lung — COPD / Emphysema)",
                        causes = "Alveolar wall destruction (emphysema/COPD); high risk of air trapping & AutoPEEP.",
                        action = "Use obstructive strategy (I:E 1:4–1:6), lower RR, monitor RCexp & AutoPEEP."
                    )
                )
            }
        }

        // 4. Rinsp analysis
        if (rinsp != null && rinsp > 15.0) {
            findings.add(
                VentFinding(
                    severity = if (rinsp > 22.0) VentAlertSeverity.CRITICAL else VentAlertSeverity.CAUTION,
                    parameter = "Rinsp = ${MedicMath.formatNumber(rinsp, 0)} cmH2O/(L/s) (Ref 5–15)",
                    headline = "Elevated Inspiratory Resistance",
                    causes = "Bronchospasm (COPD/asthma 15–30), secretions, kinked/small ETT, clogged HME.",
                    action = "Check ETT size & patency, inline suction, bronchodilators, replace wet HME, slow P-ramp if coughing."
                )
            )
        }

        // 5. RCexp & Expiratory Time check
        if (rcexp != null && rcexp > 0.0) {
            if (rcexp > 0.9) {
                val teText = if (actualTeSec != null && minTeThreeRcExp != null) {
                    "Actual TE ${MedicMath.formatNumber(actualTeSec, 1)} s vs Min TE ${MedicMath.formatNumber(minTeThreeRcExp, 1)} s (3×RCexp)"
                } else {
                    "Min TE needed = ${MedicMath.formatNumber(rcexp * 3.0, 1)} s (3×RCexp)"
                }
                val insufficientTe = actualTeSec != null && minTeThreeRcExp != null && actualTeSec < minTeThreeRcExp
                findings.add(
                    VentFinding(
                        severity = if (insufficientTe) VentAlertSeverity.CRITICAL else VentAlertSeverity.CAUTION,
                        parameter = "RCexp = ${MedicMath.formatNumber(rcexp, 2)} s · $teText",
                        headline = if (insufficientTe) "Insufficient Expiratory Time — Breath Stacking!" else "Prolonged Expiratory Time Constant (Obstructive)",
                        causes = "COPD, status asthmaticus, high airway resistance, or elevated compliance slowing passive exhalation.",
                        action = "Keep RR ≤ ${maxSafeRr ?: "low"} bpm (extend TE > ${MedicMath.formatNumber(rcexp * 3.0, 1)} s, I:E 1:4–1:6), treat bronchospasm."
                    )
                )
            } else if (actualTeSec != null && minTeThreeRcExp != null && actualTeSec < minTeThreeRcExp) {
                findings.add(
                    VentFinding(
                        severity = VentAlertSeverity.CAUTION,
                        parameter = "TE ${MedicMath.formatNumber(actualTeSec, 1)} s < 3×RCexp (${MedicMath.formatNumber(minTeThreeRcExp, 1)} s)",
                        headline = "Rate / TI Too Fast for Full Emptying (< 95%)",
                        causes = "High respiratory rate or long inspiratory time cutting expiratory phase short.",
                        action = "Lower RR to ≤ ${maxSafeRr ?: "target"} bpm or shorten TI so TE ≥ 3 × RCexp."
                    )
                )
            }
        }

        // 6. AutoPEEP
        if (autoPeep != null && autoPeep > 0.5) {
            findings.add(
                VentFinding(
                    severity = if (autoPeep >= 5.0) VentAlertSeverity.CRITICAL else VentAlertSeverity.CAUTION,
                    parameter = "AutoPEEP = ${MedicMath.formatNumber(autoPeep, 1)} cmH2O (Ideal 0)",
                    headline = "Air Trapping / Intrinsic PEEP",
                    causes = "Incomplete exhalation (RR too high, TI too long, severe bronchospasm), causing hypotension & wasted trigger effort.",
                    action = "If hypotensive: disconnect circuit briefly to decompress chest. Lower RR, lengthen E-time (1:4–1:6), bronchodilators."
                )
            )
        }

        // 7. VLeak%
        if (vLeakPercent != null && vLeakPercent > 10.0) {
            findings.add(
                VentFinding(
                    severity = if (vLeakPercent > 25.0) VentAlertSeverity.CRITICAL else VentAlertSeverity.CAUTION,
                    parameter = "VLeak = ${MedicMath.formatNumber(vLeakPercent, 0)}%",
                    headline = if (vLeakPercent > 25.0) "Significant Airway / Circuit Leak (> 25%)" else "Moderate Airway / Circuit Leak",
                    causes = "Deflated/ruptured ETT cuff, pilot line leak, loose ventilator tubing/HME, or mask leak on NIV.",
                    action = "Check ETT cuff pressure (20–30 adult / 20–25 peds), pilot balloon, circuit connections. On NIV (>25%), refit mask straps."
                )
            )
        }

        // 8. P0.1 (Work of breathing / drive)
        if (p01 != null && p01 < -3.0) {
            findings.add(
                VentFinding(
                    severity = if (p01 <= -5.0) VentAlertSeverity.CRITICAL else VentAlertSeverity.CAUTION,
                    parameter = "P0.1 = ${MedicMath.formatNumber(p01, 1)} cmH2O (Ref 0 to -3)",
                    headline = if (p01 <= -5.0) "Excessive Work of Breathing / Air Hunger" else "Strong Inspiratory Effort",
                    causes = "Inadequate pressure/volume support, slow P-ramp, high metabolic demand/acidosis, or pain/anxiety.",
                    action = "Increase pressure support / Pinsp, shorten P-ramp, verify metabolic acidosis compensation, assess analgesia/sedation."
                )
            )
        }

        val overallSeverity = when {
            findings.any { it.severity == VentAlertSeverity.CRITICAL } -> VentAlertSeverity.CRITICAL
            findings.any { it.severity == VentAlertSeverity.CAUTION } -> VentAlertSeverity.CAUTION
            else -> VentAlertSeverity.NORMAL
        }

        val headlineDiagnosis = when {
            findings.isEmpty() -> "Normal Ventilator Mechanics"
            findings.size == 1 -> findings.first().headline
            else -> "${findings.first().headline} (+${findings.size - 1} more)"
        }

        if (findings.isEmpty()) {
            findings.add(
                VentFinding(
                    severity = VentAlertSeverity.NORMAL,
                    parameter = "Mechanics Within Target",
                    headline = "No Acute Resistance, Compliance, or AutoPEEP Alerts",
                    causes = "Cstat (50–60), Rinsp (≤15), Pplat (<30), AutoPEEP (0), and VLeak (<10%) are acceptable.",
                    action = "Continue monitoring ETCO2 (35–45), SpO2 (94–98%), and ETT cuff pressure at altitude."
                )
            )
        }

        return VentMechanicsResult(
            overallSeverity = overallSeverity,
            headlineDiagnosis = headlineDiagnosis,
            pipMinusPplat = pipMinusPplat,
            computedCstat = computedCstat,
            predictedPplat = predictedPplat,
            minTeThreeRcExp = minTeThreeRcExp,
            actualTeSec = actualTeSec,
            maxSafeRr = maxSafeRr,
            computedPmean = computedPmean,
            findings = findings
        )
    }
}
