package com.example.domain

import kotlin.math.abs

enum class AbgSampleType(val label: String) {
    ARTERIAL("Arterial (ABG)"),
    VENOUS("Venous (VBG)")
}

enum class AbgSeverity {
    NORMAL,
    CAUTION,
    CRITICAL
}

data class AbgPreset(
    val name: String,
    val detail: String,
    val sampleType: AbgSampleType,
    val ph: String,
    val paco2: String,
    val hco3: String,
    val pao2: String,
    val fio2Percent: String,
    val na: String,
    val cl: String
)

data class AbgResult(
    val sampleType: AbgSampleType,
    val severity: AbgSeverity,
    val primaryDisorder: String,
    val compensationStatus: String,
    val expectedCompensationDetail: String,
    val phStatusText: String,
    val paco2StatusText: String,
    val hco3StatusText: String,
    val anionGap: Double?,
    val anionGapText: String?,
    val deltaDeltaText: String?,
    val pfRatio: Double?,
    val oxygenationText: String,
    val metAcidosisVentAlert: Boolean,
    val likelyCauses: List<String>,
    val fieldActions: List<String>,
    val pcgReference: String = "Air Methods PCG 4.3.21 (Table 4.20) & 7.33.5.2"
)

object AbgMath {

    val PRESETS = listOf(
        AbgPreset(
            name = "Normal",
            detail = "7.40 / 40 / 24",
            sampleType = AbgSampleType.ARTERIAL,
            ph = "7.40",
            paco2 = "40",
            hco3 = "24",
            pao2 = "95",
            fio2Percent = "21",
            na = "140",
            cl = "104"
        ),
        AbgPreset(
            name = "DKA",
            detail = "HAGMA",
            sampleType = AbgSampleType.ARTERIAL,
            ph = "7.14",
            paco2 = "22",
            hco3 = "8",
            pao2 = "105",
            fio2Percent = "21",
            na = "134",
            cl = "98"
        ),
        AbgPreset(
            name = "COPD",
            detail = "Resp Acid",
            sampleType = AbgSampleType.ARTERIAL,
            ph = "7.26",
            paco2 = "68",
            hco3 = "30",
            pao2 = "58",
            fio2Percent = "28",
            na = "139",
            cl = "100"
        ),
        AbgPreset(
            name = "PE / Hypervent",
            detail = "Resp Alk",
            sampleType = AbgSampleType.ARTERIAL,
            ph = "7.52",
            paco2 = "26",
            hco3 = "21",
            pao2 = "62",
            fio2Percent = "40",
            na = "140",
            cl = "105"
        ),
        AbgPreset(
            name = "Vomiting",
            detail = "Met Alk",
            sampleType = AbgSampleType.ARTERIAL,
            ph = "7.51",
            paco2 = "48",
            hco3 = "38",
            pao2 = "90",
            fio2Percent = "21",
            na = "138",
            cl = "88"
        ),
        AbgPreset(
            name = "Mixed Shock",
            detail = "pH 7.08",
            sampleType = AbgSampleType.ARTERIAL,
            ph = "7.08",
            paco2 = "55",
            hco3 = "16",
            pao2 = "54",
            fio2Percent = "60",
            na = "138",
            cl = "100"
        )
    )

    fun analyze(
        sampleType: AbgSampleType,
        ph: Double?,
        paco2: Double?,
        hco3: Double?,
        pao2: Double? = null,
        fio2Percent: Double? = null,
        na: Double? = null,
        cl: Double? = null
    ): AbgResult? {
        if (ph == null || paco2 == null || hco3 == null) return null
        if (ph < 6.5 || ph > 8.0 || paco2 <= 0.0 || hco3 <= 0.0) return null

        // Reference bounds per Air Methods PCG Table 4.20 (p. 4-27) + clinical midpoints
        val phLow = if (sampleType == AbgSampleType.ARTERIAL) 7.35 else 7.33
        val phHigh = if (sampleType == AbgSampleType.ARTERIAL) 7.45 else 7.40
        val phMid = if (sampleType == AbgSampleType.ARTERIAL) 7.40 else 7.365

        val co2Low = if (sampleType == AbgSampleType.ARTERIAL) 35.0 else 40.0
        val co2High = if (sampleType == AbgSampleType.ARTERIAL) 45.0 else 52.0
        val co2Baseline = if (sampleType == AbgSampleType.ARTERIAL) 40.0 else 46.0

        val hco3Low = if (sampleType == AbgSampleType.ARTERIAL) 22.0 else 23.0
        val hco3High = if (sampleType == AbgSampleType.ARTERIAL) 26.0 else 30.0
        val hco3Baseline = if (sampleType == AbgSampleType.ARTERIAL) 24.0 else 26.0

        val phStatusText = when {
            ph < phLow -> "Acidemia (Low)"
            ph > phHigh -> "Alkalemia (High)"
            else -> "Normal (${MedicMath.formatNumber(phLow, 2)}–${MedicMath.formatNumber(phHigh, 2)})"
        }
        val paco2StatusText = when {
            paco2 > co2High -> "Hypercapnia (High)"
            paco2 < co2Low -> "Hypocapnia (Low)"
            else -> "Normal (${MedicMath.formatNumber(co2Low, 0)}–${MedicMath.formatNumber(co2High, 0)})"
        }
        val hco3StatusText = when {
            hco3 < hco3Low -> "Low HCO3⁻"
            hco3 > hco3High -> "Elevated HCO3⁻"
            else -> "Normal (${MedicMath.formatNumber(hco3Low, 0)}–${MedicMath.formatNumber(hco3High, 0)})"
        }

        // Anion Gap = Na - Cl - HCO3 (PCG normal < 14, high-AG trigger > 16)
        val anionGap = if (na != null && cl != null && na > 0.0 && cl > 0.0) {
            na - cl - hco3
        } else null

        val isHighAnionGap = anionGap != null && anionGap > 14.0
        val anionGapText = anionGap?.let { ag ->
            when {
                ag > 16.0 -> "AG ${MedicMath.formatNumber(ag, 1)} mEq/L (High — PCG >16 trigger)"
                ag > 14.0 -> "AG ${MedicMath.formatNumber(ag, 1)} mEq/L (Elevated >14)"
                ag < 6.0 -> "AG ${MedicMath.formatNumber(ag, 1)} mEq/L (Low)"
                else -> "AG ${MedicMath.formatNumber(ag, 1)} mEq/L (Normal <14)"
            }
        }

        val deltaDeltaText = if (anionGap != null && anionGap > 14.0 && hco3 < 23.5) {
            val deltaAg = anionGap - 12.0
            val deltaHco3 = 24.0 - hco3
            if (deltaHco3 > 0.5) {
                val ratio = deltaAg / deltaHco3
                val ratioStr = MedicMath.formatNumber(ratio, 2)
                when {
                    ratio < 0.4 -> "Δ/Δ = $ratioStr (<0.4: Pure Non-AG Acidosis)"
                    ratio < 0.8 -> "Δ/Δ = $ratioStr (0.4–0.8: Mixed High-AG + Non-AG Acidosis)"
                    ratio <= 2.0 -> "Δ/Δ = $ratioStr (0.8–2.0: Pure High-AG Metabolic Acidosis)"
                    else -> "Δ/Δ = $ratioStr (>2.0: High-AG Acidosis + Concurrent Met. Alkalosis)"
                }
            } else null
        } else null

        val isAcidemic = ph < phLow
        val isAlkalemic = ph > phHigh
        val isPhNormal = !isAcidemic && !isAlkalemic

        val primaryDisorder: String
        val compensationStatus: String
        val expectedCompensationDetail: String
        val causes = linkedSetOf<String>()
        val actions = linkedSetOf<String>()

        when {
            // Normal pH and normal PCO2/HCO3
            isPhNormal && paco2 in co2Low..co2High && hco3 in hco3Low..hco3High -> {
                if (isHighAnionGap) {
                    primaryDisorder = "High Anion Gap Metabolic Acidosis (Normal pH)"
                    compensationStatus = "Mixed / Balanced Acid-Base State"
                    expectedCompensationDetail =
                        "Elevated AG (${MedicMath.formatNumber(anionGap, 1)}) with normal pH suggests HAGMA masked by concurrent metabolic alkalosis or respiratory alkalosis."
                    addHagmaCauses(causes)
                } else {
                    primaryDisorder = "Normal Acid-Base Balance"
                    compensationStatus = "No Primary Disorder Detected"
                    expectedCompensationDetail =
                        "pH, PCO2, and HCO3⁻ are within ${sampleType.label} reference ranges."
                    causes.add("Balanced ventilation and metabolic acid-base status")
                }
            }

            // Normal pH with abnormal CO2 & HCO3 -> fully compensated or mixed opposing disorders
            isPhNormal -> {
                val co2HighFlag = paco2 > co2High
                val co2LowFlag = paco2 < co2Low
                val hco3HighFlag = hco3 > hco3High
                val hco3LowFlag = hco3 < hco3Low

                when {
                    co2HighFlag && hco3LowFlag -> {
                        primaryDisorder = "Mixed Respiratory & Metabolic Acidosis"
                        compensationStatus = "Uncompensated / Combined Derangement"
                        expectedCompensationDetail =
                            "Elevated CO2 and low HCO3⁻ both drive acidosis; verify measurements."
                        addRespAcidosisCauses(causes)
                        addMetAcidosisCauses(causes, isHighAnionGap)
                    }
                    co2LowFlag && hco3HighFlag -> {
                        primaryDisorder = "Mixed Respiratory & Metabolic Alkalosis"
                        compensationStatus = "Combined Alkalosis"
                        expectedCompensationDetail =
                            "Low CO2 and high HCO3⁻ both drive alkalosis."
                        addRespAlkalosisCauses(causes)
                        addMetAlkalosisCauses(causes)
                    }
                    // Opposing shifts with pH < midpoint -> primary acidosis with full compensation
                    ph < phMid && co2HighFlag && hco3HighFlag -> {
                        primaryDisorder = "Respiratory Acidosis (Fully Compensated)"
                        compensationStatus = "Full Renal Compensation"
                        val expChron = 24.0 + 0.35 * (paco2 - co2Baseline)
                        expectedCompensationDetail =
                            "Chronic Resp Acidosis expected HCO3⁻ ≈ ${MedicMath.formatNumber(expChron, 1)} mEq/L."
                        addRespAcidosisCauses(causes)
                    }
                    ph < phMid && hco3LowFlag && co2LowFlag -> {
                        primaryDisorder =
                            if (isHighAnionGap) "High-AG Metabolic Acidosis (Fully Compensated)"
                            else "Metabolic Acidosis (Fully Compensated)"
                        compensationStatus = "Full Respiratory Compensation (Winter's)"
                        val expCo2 = 1.5 * hco3 + 8.0
                        expectedCompensationDetail =
                            "Winter's expected PaCO2 = ${MedicMath.formatNumber(expCo2 - 2.0, 0)}–${MedicMath.formatNumber(expCo2 + 2.0, 0)} mmHg."
                        addMetAcidosisCauses(causes, isHighAnionGap)
                    }
                    ph >= phMid && hco3HighFlag && co2HighFlag -> {
                        primaryDisorder = "Metabolic Alkalosis (Fully Compensated)"
                        compensationStatus = "Full Respiratory Compensation"
                        val expCo2 = 40.0 + 0.7 * (hco3 - 24.0)
                        expectedCompensationDetail =
                            "Expected PaCO2 ≈ ${MedicMath.formatNumber(expCo2, 1)} ± 3 mmHg."
                        addMetAlkalosisCauses(causes)
                    }
                    ph >= phMid && co2LowFlag && hco3LowFlag -> {
                        primaryDisorder = "Respiratory Alkalosis (Fully Compensated)"
                        compensationStatus = "Full Renal Compensation"
                        val expChron = 24.0 - 0.5 * (co2Baseline - paco2)
                        expectedCompensationDetail =
                            "Chronic Resp Alkalosis expected HCO3⁻ ≈ ${MedicMath.formatNumber(expChron, 1)} mEq/L."
                        addRespAlkalosisCauses(causes)
                    }
                    else -> {
                        primaryDisorder = "Borderline / Compensated Acid-Base Shift"
                        compensationStatus = "Near-Normal pH"
                        expectedCompensationDetail =
                            "Mild parameter deviation without overt acidemia or alkalemia."
                        if (co2HighFlag) addRespAcidosisCauses(causes)
                        if (co2LowFlag) addRespAlkalosisCauses(causes)
                        if (hco3LowFlag) addMetAcidosisCauses(causes, isHighAnionGap)
                        if (hco3HighFlag) addMetAlkalosisCauses(causes)
                    }
                }
            }

            // Acidemia (pH < phLow)
            isAcidemic -> {
                val respAcid = paco2 > co2High
                val metAcid = hco3 < hco3Low

                when {
                    respAcid && metAcid -> {
                        primaryDisorder = "Mixed Respiratory & Metabolic Acidosis"
                        compensationStatus = "No Compensation — Additive Acidosis"
                        val winters = 1.5 * hco3 + 8.0
                        expectedCompensationDetail =
                            "Winter's expected PaCO2 = ${MedicMath.formatNumber(winters - 2.0, 0)}–${MedicMath.formatNumber(winters + 2.0, 0)} mmHg; actual PaCO2 (${MedicMath.formatNumber(paco2, 0)}) is higher → dual respiratory failure + metabolic acid load."
                        addRespAcidosisCauses(causes)
                        addMetAcidosisCauses(causes, isHighAnionGap)
                    }
                    respAcid -> {
                        val deltaCo2 = paco2 - co2Baseline
                        val expAcuteHco3 = 24.0 + 0.10 * deltaCo2
                        val expChronHco3 = 24.0 + 0.35 * deltaCo2
                        val isChronic = hco3 > expAcuteHco3 + 2.0
                        val hasMetAcidToo = hco3 < expAcuteHco3 - 2.0
                        primaryDisorder = when {
                            hasMetAcidToo -> "Mixed Respiratory & Metabolic Acidosis"
                            isChronic -> "Chronic (or Acute-on-Chronic) Respiratory Acidosis"
                            else -> "Acute Respiratory Acidosis"
                        }
                        compensationStatus = when {
                            hasMetAcidToo -> "Concurrent Metabolic Acidosis (HCO3⁻ lower than expected)"
                            abs(hco3 - expChronHco3) <= 2.5 && isChronic -> "Partial Chronic Renal Compensation"
                            hco3 <= hco3High -> "Uncompensated Acute"
                            else -> "Partially Compensated"
                        }
                        expectedCompensationDetail =
                            "Acute exp. HCO3⁻ ≈ ${MedicMath.formatNumber(expAcuteHco3, 1)} | Chronic exp. HCO3⁻ ≈ ${MedicMath.formatNumber(expChronHco3, 1)} mEq/L."
                        addRespAcidosisCauses(causes)
                        if (hasMetAcidToo) addMetAcidosisCauses(causes, isHighAnionGap)
                    }
                    metAcid || (paco2 <= co2High && hco3 < hco3Baseline) -> {
                        val wintersMid = 1.5 * hco3 + 8.0
                        val wintersLow = (wintersMid - 2.0).coerceAtLeast(8.0)
                        val wintersHigh = wintersMid + 2.0
                        val concurrentRespAcid = paco2 > wintersHigh
                        val concurrentRespAlk = paco2 < wintersLow

                        val gapPrefix = when {
                            anionGap == null -> ""
                            isHighAnionGap -> "High-AG "
                            else -> "Non-AG "
                        }
                        primaryDisorder = when {
                            concurrentRespAcid -> "${gapPrefix}Metabolic Acidosis + Respiratory Acidosis"
                            concurrentRespAlk -> "${gapPrefix}Metabolic Acidosis + Respiratory Alkalosis"
                            else -> "${gapPrefix}Metabolic Acidosis"
                        }
                        compensationStatus = when {
                            concurrentRespAcid -> "Inadequate Respiratory Compensation (Hypoventilating)"
                            concurrentRespAlk -> "Excessive Hyperventilation (Concurrent Resp. Alkalosis)"
                            paco2 < co2Low -> "Partially Compensated (Winter's Match)"
                            else -> "Uncompensated"
                        }
                        expectedCompensationDetail =
                            "Winter's Formula: Expected PaCO2 = ${MedicMath.formatNumber(wintersLow, 0)}–${MedicMath.formatNumber(wintersHigh, 0)} mmHg (Measured: ${MedicMath.formatNumber(paco2, 0)})."
                        addMetAcidosisCauses(causes, isHighAnionGap)
                        if (concurrentRespAcid) addRespAcidosisCauses(causes)
                        if (concurrentRespAlk) addRespAlkalosisCauses(causes)
                    }
                    else -> {
                        primaryDisorder = "Acidemia (Atypical / Mixed)"
                        compensationStatus = "Verify Sample & Electrolytes"
                        expectedCompensationDetail =
                            "Low pH without classic PCO2 elevation or HCO3⁻ drop; check Anion Gap and clinical history."
                        addMetAcidosisCauses(causes, isHighAnionGap)
                    }
                }
            }

            // Alkalemia (pH > phHigh)
            else -> {
                val respAlk = paco2 < co2Low
                val metAlk = hco3 > hco3High

                when {
                    respAlk && metAlk -> {
                        primaryDisorder = "Mixed Respiratory & Metabolic Alkalosis"
                        compensationStatus = "No Compensation — Combined Alkalosis"
                        expectedCompensationDetail =
                            "Both low PCO2 and elevated HCO3⁻ contribute to severe alkalemia."
                        addRespAlkalosisCauses(causes)
                        addMetAlkalosisCauses(causes)
                    }
                    respAlk -> {
                        val deltaCo2 = co2Baseline - paco2
                        val expAcuteHco3 = 24.0 - 0.20 * deltaCo2
                        val expChronHco3 = 24.0 - 0.50 * deltaCo2
                        val isChronic = hco3 < expAcuteHco3 - 2.0
                        val hasMetAlkToo = hco3 > expAcuteHco3 + 2.5

                        primaryDisorder = when {
                            hasMetAlkToo -> "Mixed Respiratory & Metabolic Alkalosis"
                            isChronic -> "Chronic Respiratory Alkalosis"
                            else -> "Acute Respiratory Alkalosis"
                        }
                        compensationStatus = when {
                            hasMetAlkToo -> "Concurrent Metabolic Alkalosis (HCO3⁻ elevated)"
                            hco3 >= hco3Low -> "Uncompensated Acute"
                            else -> "Partially Compensated"
                        }
                        expectedCompensationDetail =
                            "Acute exp. HCO3⁻ ≈ ${MedicMath.formatNumber(expAcuteHco3, 1)} | Chronic exp. HCO3⁻ ≈ ${MedicMath.formatNumber(expChronHco3, 1)} mEq/L."
                        addRespAlkalosisCauses(causes)
                        if (hasMetAlkToo) addMetAlkalosisCauses(causes)
                    }
                    metAlk || hco3 > hco3Baseline -> {
                        val expCo2 = 40.0 + 0.7 * (hco3 - 24.0)
                        val expLow = expCo2 - 3.0
                        val expHigh = expCo2 + 3.0
                        val concurrentRespAlk = paco2 < expLow
                        val concurrentRespAcid = paco2 > expHigh

                        primaryDisorder = when {
                            concurrentRespAlk -> "Mixed Metabolic & Respiratory Alkalosis"
                            concurrentRespAcid -> "Metabolic Alkalosis + Respiratory Acidosis"
                            else -> "Metabolic Alkalosis"
                        }
                        compensationStatus = when {
                            concurrentRespAlk -> "Inadequate Hypoventilation (Concurrent Resp. Alkalosis)"
                            concurrentRespAcid -> "Excess CO2 Retention (Concurrent Resp. Acidosis)"
                            paco2 > co2High -> "Partially Compensated"
                            else -> "Uncompensated"
                        }
                        expectedCompensationDetail =
                            "Expected PaCO2 = ${MedicMath.formatNumber(expLow, 0)}–${MedicMath.formatNumber(expHigh, 0)} mmHg (Measured: ${MedicMath.formatNumber(paco2, 0)})."
                        addMetAlkalosisCauses(causes)
                        if (concurrentRespAlk) addRespAlkalosisCauses(causes)
                        if (concurrentRespAcid) addRespAcidosisCauses(causes)
                    }
                    else -> {
                        primaryDisorder = "Alkalemia (Atypical)"
                        compensationStatus = "Verify Values"
                        expectedCompensationDetail =
                            "Elevated pH without clear PCO2 drop or HCO3⁻ rise."
                        addRespAlkalosisCauses(causes)
                    }
                }
            }
        }

        // Oxygenation & P/F Ratio (Arterial only for PaO2 / P:F)
        val pfRatio = if (sampleType == AbgSampleType.ARTERIAL && pao2 != null && fio2Percent != null && fio2Percent >= 21.0) {
            pao2 / (fio2Percent / 100.0)
        } else null

        val oxygenationText = when {
            sampleType == AbgSampleType.VENOUS -> {
                if (pao2 != null) {
                    "Venous PO2 ${MedicMath.formatNumber(pao2, 0)} mmHg (Ref 25–44; use SpO2/ABG for oxygenation)"
                } else {
                    "VBG cannot assess arterial PaO2 or P/F ratio; monitor SpO2."
                }
            }
            pao2 == null -> "Enter PaO2 & FiO2% to evaluate hypoxemia and P/F ratio."
            else -> {
                val pao2Label = when {
                    pao2 < 45.0 -> "Severe Hypoxemia (PaO2 ${MedicMath.formatNumber(pao2, 0)})"
                    pao2 < 60.0 -> "Moderate Hypoxemia (PaO2 ${MedicMath.formatNumber(pao2, 0)})"
                    pao2 < 74.0 -> "Mild Hypoxemia (PaO2 ${MedicMath.formatNumber(pao2, 0)})"
                    pao2 > 110.0 -> "Hyperoxia (PaO2 ${MedicMath.formatNumber(pao2, 0)} — wean FiO2 if SpO2 >93%)"
                    else -> "Normoxemia (PaO2 ${MedicMath.formatNumber(pao2, 0)} mmHg)"
                }
                val pfLabel = pfRatio?.let { pf ->
                    val pfStr = MedicMath.formatNumber(pf, 0)
                    when {
                        pf <= 100.0 -> "P/F $pfStr (Severe Refractory ARDS ≤100 — ECMO / Prone)"
                        pf < 150.0 -> "P/F $pfStr (Severe ARDS <150 — Consider Prone + High PEEP)"
                        pf < 200.0 -> "P/F $pfStr (Moderate ARDS 150–200)"
                        pf < 300.0 -> "P/F $pfStr (Mild ARDS 200–300)"
                        else -> "P/F $pfStr (Normal >300)"
                    }
                }
                if (pfLabel != null) "$pao2Label · $pfLabel" else pao2Label
            }
        }

        val metAcidosisVentAlert = (primaryDisorder.contains("Metabolic Acidosis") && ph < 7.25) ||
            (anionGap != null && anionGap > 16.0)

        // Tailored Air Methods PCG Field Actions
        if (metAcidosisVentAlert) {
            actions.add("PCG 7.33.5.2 (Metabolic Acidosis): Intubated target VE = 120 mL/kg/min, ETCO2 25–30 mmHg (NEVER < 25). Avoid APV/PRVC — use PCV+ / PC.")
        }
        if (primaryDisorder.contains("Respiratory Acidosis")) {
            actions.add("Increase alveolar minute ventilation (VE): check airway patency/DOPES, treat bronchospasm, increase RR or VT carefully; if COPD/asthma use I:E 1:4–1:6 to avoid AutoPEEP.")
        }
        if (primaryDisorder.contains("Respiratory Alkalosis")) {
            actions.add("Evaluate hypoxia, PE, pain, fever, or sepsis. If ventilated, decrease RR or VT to avoid over-ventilation & cerebral vasoconstriction.")
        }
        if (primaryDisorder.contains("Metabolic Alkalosis")) {
            actions.add("Address volume contraction / chloride depletion (NS resuscitation if hypovolemic), check K⁺, avoid over-hyperventilating.")
        }
        if (sampleType == AbgSampleType.ARTERIAL && pfRatio != null && pfRatio < 200.0) {
            actions.add("PCG 7.33.5.4 / 7.45.2 (ARDS Hypoxemia): Use low VT 4–6 mL/kg IBW, keep Pplat < 30 cmH2O, titrate PEEP over FiO2, consider prone positioning if P/F < 150.")
        } else if (sampleType == AbgSampleType.ARTERIAL && pao2 != null && pao2 < 60.0) {
            actions.add("Treat acute hypoxemia: increase FiO2 / PEEP, rule out pneumothorax, mainstem intubation, or pulmonary edema.")
        } else if (sampleType == AbgSampleType.ARTERIAL && pao2 != null && pao2 > 120.0) {
            actions.add("PCG 7.33.5.1: Titrate FiO2 down to maintain SpO2 94–98% (avoid unnecessary hyperoxia).")
        }
        if (sampleType == AbgSampleType.VENOUS) {
            actions.add("VBG Note: Venous pH averages ~0.03–0.05 lower and PCO2 ~4–6 mmHg higher than arterial blood.")
        }
        if (actions.isEmpty()) {
            actions.add("Continue routine monitoring (SpO2 94–98%, ETCO2 35–45 mmHg) and reassess clinically.")
        }

        val severity = when {
            ph < 7.20 || ph > 7.55 -> AbgSeverity.CRITICAL
            (sampleType == AbgSampleType.ARTERIAL && pao2 != null && pao2 < 55.0) -> AbgSeverity.CRITICAL
            (pfRatio != null && pfRatio < 150.0) -> AbgSeverity.CRITICAL
            metAcidosisVentAlert -> AbgSeverity.CRITICAL
            ph < phLow || ph > phHigh || paco2 < co2Low || paco2 > co2High || hco3 < hco3Low || hco3 > hco3High -> AbgSeverity.CAUTION
            else -> AbgSeverity.NORMAL
        }

        return AbgResult(
            sampleType = sampleType,
            severity = severity,
            primaryDisorder = primaryDisorder,
            compensationStatus = compensationStatus,
            expectedCompensationDetail = expectedCompensationDetail,
            phStatusText = phStatusText,
            paco2StatusText = paco2StatusText,
            hco3StatusText = hco3StatusText,
            anionGap = anionGap,
            anionGapText = anionGapText,
            deltaDeltaText = deltaDeltaText,
            pfRatio = pfRatio,
            oxygenationText = oxygenationText,
            metAcidosisVentAlert = metAcidosisVentAlert,
            likelyCauses = causes.toList(),
            fieldActions = actions.toList()
        )
    }

    private fun addMetAcidosisCauses(causes: MutableSet<String>, isHighAnionGap: Boolean) {
        if (isHighAnionGap) {
            addHagmaCauses(causes)
        } else {
            causes.add("High-AG (if AG elevated): Lactic acidosis (shock/sepsis/hypoxia), DKA/ketoacidosis, Renal failure (uremia), Toxins (methanol, ethylene glycol, salicylates, cyanide)")
            causes.add("Non-AG (Hyperchloremic): Severe diarrhea, high-volume Normal Saline resuscitation, renal tubular acidosis")
        }
    }

    private fun addHagmaCauses(causes: MutableSet<String>) {
        causes.add("Lactic acidosis (shock, sepsis, cardiac arrest, mesenteric ischemia, cyanide/CO)")
        causes.add("Ketoacidosis (DKA, alcoholic ketoacidosis, starvation)")
        causes.add("Renal failure / uremia")
        causes.add("Ingestions / toxins (salicylates, methanol, ethylene glycol, propylene glycol)")
    }

    private fun addRespAcidosisCauses(causes: MutableSet<String>) {
        causes.add("CNS depression (opioids, sedatives, TBI, stroke)")
        causes.add("Obstructive lung disease (COPD exacerbation, status asthmaticus, air trapping / AutoPEEP)")
        causes.add("Airway obstruction / ETT kink, secretions, or inadequate ventilator MV (low VT or RR)")
        causes.add("Neuromuscular weakness / paralytics / chest trauma (flail chest, pneumothorax)")
    }

    private fun addRespAlkalosisCauses(causes: MutableSet<String>) {
        causes.add("Hypoxemia-driven tachypnea (pulmonary embolism, pneumonia, CHF/pulmonary edema, high altitude)")
        causes.add("Iatrogenic hyperventilation on ventilator (excessive RR or VT)")
        causes.add("Sepsis (early), fever, pain, anxiety, salicylate toxicity, pregnancy")
        causes.add("CNS stimulation (SAH, stroke, hyperventilation)")
    }

    private fun addMetAlkalosisCauses(causes: MutableSet<String>) {
        causes.add("GI acid loss: Persistent vomiting or NG tube suctioning")
        causes.add("Diuretic therapy (loop/thiazide), hypokalemia, volume contraction")
        causes.add("Excessive sodium bicarbonate or citrate administration (massive transfusion)")
        causes.add("Post-hypercapnia (rapid vent lowering of CO2 in chronic CO2 retainer)")
    }
}
