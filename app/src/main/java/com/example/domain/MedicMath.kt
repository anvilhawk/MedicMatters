package com.example.domain

import java.util.Locale
import kotlin.math.ceil
import kotlin.math.roundToInt

object MedicMath {

    val STANDARD_DROP_FACTORS = listOf(10, 15, 20, 60)

    val DRIP_PRESETS = listOf(
        DripPreset(
            name = "Generic",
            subtitle = "Custom Mix",
            mode = DripMode.MED_INFUSION,
            doseValue = "5",
            doseRateUnit = DoseRateUnit.MCG_KG_MIN,
            drugAmount = "250",
            drugUnit = MedicationUnit.MG,
            bagVolumeMl = "250",
            dropFactor = 60,
            pcgSection = "Custom",
            averageDoseSummary = "User-defined rate",
            normalRangeSummary = "No fixed protocol limits (select concentration & volume)",
            minRatePrimary = null,
            maxRatePrimary = null,
            primaryRateUnit = null,
            protocolNotes = "Generic infusion — select concentration (/mL or total drug) and bag volume below."
        ),
        DripPreset(
            name = "Dopamine",
            subtitle = "400mg/250mL",
            mode = DripMode.MED_INFUSION,
            doseValue = "5",
            doseRateUnit = DoseRateUnit.MCG_KG_MIN,
            drugAmount = "400",
            drugUnit = MedicationUnit.MG,
            bagVolumeMl = "250",
            dropFactor = 60,
            pcgSection = "PCG 4.1 / 2.3 / 3.13",
            averageDoseSummary = "Start 5 mcg/kg/min",
            normalRangeSummary = "2 – 20 mcg/kg/min (Shock: 5–20 | Brady: 2–10)",
            minRatePrimary = 2.0,
            maxRatePrimary = 20.0,
            primaryRateUnit = DoseRateUnit.MCG_KG_MIN,
            protocolNotes = "1,600 mcg/mL std mix. Titrate to SBP ≥90 or MAP ≥65 mmHg."
        ),
        DripPreset(
            name = "Norepi",
            subtitle = "8mg/250mL",
            mode = DripMode.MED_INFUSION,
            doseValue = "5",
            doseRateUnit = DoseRateUnit.MCG_MIN,
            drugAmount = "8",
            drugUnit = MedicationUnit.MG,
            bagVolumeMl = "250",
            dropFactor = 60,
            pcgSection = "PCG 4.1 / 3.13 / 3.24",
            averageDoseSummary = "Start 2–5 mcg/min",
            normalRangeSummary = "2 – 30 mcg/min (or 0.05 – 2 mcg/kg/min)",
            minRatePrimary = 2.0,
            maxRatePrimary = 30.0,
            primaryRateUnit = DoseRateUnit.MCG_MIN,
            altMinRate = 0.05,
            altMaxRate = 2.0,
            altRateUnit = DoseRateUnit.MCG_KG_MIN,
            protocolNotes = "32 mcg/mL std mix. Titrate q2–5 min to target MAP ≥65 mmHg."
        ),
        DripPreset(
            name = "Epi Drip",
            subtitle = "1mg/250mL",
            mode = DripMode.MED_INFUSION,
            doseValue = "2",
            doseRateUnit = DoseRateUnit.MCG_MIN,
            drugAmount = "1",
            drugUnit = MedicationUnit.MG,
            bagVolumeMl = "250",
            dropFactor = 60,
            pcgSection = "PCG 4.1 / 2.3 / 3.3",
            averageDoseSummary = "Start 2 mcg/min",
            normalRangeSummary = "2 – 10 mcg/min (or 0.05 – 1 mcg/kg/min)",
            minRatePrimary = 2.0,
            maxRatePrimary = 10.0,
            primaryRateUnit = DoseRateUnit.MCG_MIN,
            altMinRate = 0.05,
            altMaxRate = 1.0,
            altRateUnit = DoseRateUnit.MCG_KG_MIN,
            protocolNotes = "4 mcg/mL std mix (alt 1 mg/100 mL). Bradycardia, anaphylaxis, or shock."
        ),
        DripPreset(
            name = "Dobutamine",
            subtitle = "250mg/250mL",
            mode = DripMode.MED_INFUSION,
            doseValue = "5",
            doseRateUnit = DoseRateUnit.MCG_KG_MIN,
            drugAmount = "250",
            drugUnit = MedicationUnit.MG,
            bagVolumeMl = "250",
            dropFactor = 60,
            pcgSection = "PCG 4.1 / 3.13",
            averageDoseSummary = "Start 5 mcg/kg/min",
            normalRangeSummary = "2 – 20 mcg/kg/min",
            minRatePrimary = 2.0,
            maxRatePrimary = 20.0,
            primaryRateUnit = DoseRateUnit.MCG_KG_MIN,
            protocolNotes = "1,000 mcg/mL std mix. Cardiogenic shock / inotropic support."
        ),
        DripPreset(
            name = "Lidocaine",
            subtitle = "1g/250mL",
            mode = DripMode.MED_INFUSION,
            doseValue = "2",
            doseRateUnit = DoseRateUnit.MG_MIN,
            drugAmount = "1",
            drugUnit = MedicationUnit.G,
            bagVolumeMl = "250",
            dropFactor = 60,
            pcgSection = "PCG 4.1 / 2.4 / 2.9",
            averageDoseSummary = "Usual 2 mg/min",
            normalRangeSummary = "1 – 4 mg/min",
            minRatePrimary = 1.0,
            maxRatePrimary = 4.0,
            primaryRateUnit = DoseRateUnit.MG_MIN,
            protocolNotes = "4 mg/mL std mix (alt 1 g/100 mL). Post-ROSC / VT maintenance infusion."
        ),
        DripPreset(
            name = "Amiodarone",
            subtitle = "150mg/100mL",
            mode = DripMode.VOLUME_TIME,
            bagVolumeMl = "100",
            timeValue = "10",
            timeUnit = TimeUnit.MINUTES,
            dropFactor = 10,
            pcgSection = "PCG 2.2 / 2.5 / 2.9",
            averageDoseSummary = "150 mg in 100 mL over 10 min",
            normalRangeSummary = "Load: 150 mg / 10 min · Maint: 60 mg/hr ×6h, then 30 mg/hr ×18h",
            protocolNotes = "Peds VT/SVT: 5 mg/kg over 20–60 min (max 150 mg)."
        ),
        DripPreset(
            name = "Nitroglycerin",
            subtitle = "50mg/250mL",
            mode = DripMode.MED_INFUSION,
            doseValue = "10",
            doseRateUnit = DoseRateUnit.MCG_MIN,
            drugAmount = "50",
            drugUnit = MedicationUnit.MG,
            bagVolumeMl = "250",
            dropFactor = 60,
            pcgSection = "PCG 4.1 / 2.1 / 2.6",
            averageDoseSummary = "Start 10 mcg/min (CHF: 40 mcg/min)",
            normalRangeSummary = "10 – 200 mcg/min",
            minRatePrimary = 10.0,
            maxRatePrimary = 200.0,
            primaryRateUnit = DoseRateUnit.MCG_MIN,
            protocolNotes = "200 mcg/mL std mix. Hold if SBP < 90 mmHg or recent PDE5 inhibitor."
        ),
        DripPreset(
            name = "Nicardipine",
            subtitle = "25mg/250mL",
            mode = DripMode.MED_INFUSION,
            doseValue = "2.5",
            doseRateUnit = DoseRateUnit.MG_HR,
            drugAmount = "25",
            drugUnit = MedicationUnit.MG,
            bagVolumeMl = "250",
            dropFactor = 60,
            pcgSection = "PCG 4.1 / 3.10 / 3.26",
            averageDoseSummary = "Start 2.5 mg/hr (Maint ~3 mg/hr)",
            normalRangeSummary = "2.5 – 15 mg/hr",
            minRatePrimary = 2.5,
            maxRatePrimary = 15.0,
            primaryRateUnit = DoseRateUnit.MG_HR,
            protocolNotes = "0.1 mg/mL std mix. Titrate q5–15 min by 2.5 mg/hr up to 15 mg/hr max."
        ),
        DripPreset(
            name = "Propofol",
            subtitle = "1000mg/100mL",
            mode = DripMode.MED_INFUSION,
            doseValue = "20",
            doseRateUnit = DoseRateUnit.MCG_KG_MIN,
            drugAmount = "1000",
            drugUnit = MedicationUnit.MG,
            bagVolumeMl = "100",
            dropFactor = 60,
            pcgSection = "PCG 4.1 / 1.8",
            averageDoseSummary = "Usual 20–50 mcg/kg/min",
            normalRangeSummary = "5 – 80 mcg/kg/min usual (Max 200 mcg/kg/min)",
            minRatePrimary = 5.0,
            maxRatePrimary = 200.0,
            primaryRateUnit = DoseRateUnit.MCG_KG_MIN,
            protocolNotes = "10 mg/mL (1%). Usual sedation 5–80 mcg/kg/min; NO IV bolus allowed."
        ),
        DripPreset(
            name = "Vasopressin",
            subtitle = "20units/100mL",
            mode = DripMode.MED_INFUSION,
            doseValue = "0.03",
            doseRateUnit = DoseRateUnit.UNITS_MIN,
            drugAmount = "20",
            drugUnit = MedicationUnit.UNITS,
            bagVolumeMl = "100",
            dropFactor = 60,
            pcgSection = "PCG 4.1 / 3.13 / 3.24",
            averageDoseSummary = "Shock: 0.03 units/min",
            normalRangeSummary = "Shock: 0.01 – 0.04 units/min (GI bleed: 0.2 units/min)",
            minRatePrimary = 0.01,
            maxRatePrimary = 0.04,
            primaryRateUnit = DoseRateUnit.UNITS_MIN,
            protocolNotes = "0.2 units/mL std mix. Fixed shock dose 0.01–0.04 units/min; GI bleed 0.2 units/min."
        )
    )

    val SYRINGE_PRESETS = listOf(
        SyringePreset(
            name = "Generic",
            detail = "Custom Mix",
            isWeightBased = false,
            desiredDose = "75",
            desiredUnit = MedicationUnit.MCG,
            drugOnHand = "100",
            onHandUnit = MedicationUnit.MCG,
            volumeOnHandMl = "2",
            pcgSection = "Custom",
            averageDoseSummary = "User-defined dose",
            normalRangeSummary = "No fixed protocol limits (select concentration & volume)",
            protocolNotes = "Generic vial — select concentration (/mL or total drug) and vial volume below."
        ),
        SyringePreset(
            name = "Fentanyl",
            detail = "100mcg/2mL",
            isWeightBased = true,
            desiredDose = "1",
            desiredUnit = MedicationUnit.MCG,
            drugOnHand = "100",
            onHandUnit = MedicationUnit.MCG,
            volumeOnHandMl = "2",
            pcgSection = "PCG 1.7 / 2.7",
            averageDoseSummary = "0.5–1 mcg/kg (Avg 1 mcg/kg)",
            normalRangeSummary = "0.5 – 1 mcg/kg IV/IN (Max single dose 100 mcg)",
            weightMin = 0.5,
            weightMax = 1.0,
            weightUnit = MedicationUnit.MCG,
            maxSingleDose = 100.0,
            maxSingleUnit = MedicationUnit.MCG,
            protocolNotes = "Adult & Peds analgesia: 0.5–1 mcg/kg IV/IN, max 100 mcg per dose."
        ),
        SyringePreset(
            name = "Ketamine",
            detail = "500mg/10mL",
            isWeightBased = true,
            desiredDose = "1",
            desiredUnit = MedicationUnit.MG,
            drugOnHand = "500",
            onHandUnit = MedicationUnit.MG,
            volumeOnHandMl = "10",
            pcgSection = "PCG 1.7 / 1.8 / 1.9",
            averageDoseSummary = "Pain: 0.1–0.25 mg/kg | RSI/Sed: 1–2 mg/kg",
            normalRangeSummary = "0.1 – 2 mg/kg IV (Pain 0.1–0.25; Sed/RSI 0.5–2; IM 4 mg/kg)",
            weightMin = 0.1,
            weightMax = 2.0,
            weightUnit = MedicationUnit.MG,
            protocolNotes = "IV analgesia 0.1–0.25 mg/kg; Sedation/RSI 0.5–2 mg/kg IV (4 mg/kg IM-only)."
        ),
        SyringePreset(
            name = "Midazolam",
            detail = "10mg/2mL",
            isWeightBased = false,
            desiredDose = "2.5",
            desiredUnit = MedicationUnit.MG,
            drugOnHand = "10",
            onHandUnit = MedicationUnit.MG,
            volumeOnHandMl = "2",
            pcgSection = "PCG 1.8 / 3.23 / 6.13",
            averageDoseSummary = "Adult: 2.5–5 mg | Peds: 0.05–0.2 mg/kg",
            normalRangeSummary = "Adult: 2 – 5 mg IV/IN (10 mg IM seizure) | Peds: 0.05 – 0.2 mg/kg",
            fixedMin = 2.0,
            fixedMax = 5.0,
            fixedUnit = MedicationUnit.MG,
            weightMin = 0.05,
            weightMax = 0.2,
            weightUnit = MedicationUnit.MG,
            maxSingleDose = 5.0,
            maxSingleUnit = MedicationUnit.MG,
            protocolNotes = "Adult IV/IN 2.5–5 mg (ROSC 2 mg; IM seizure 10 mg). Peds 0.05–0.2 mg/kg (max 5 mg IV/IN)."
        ),
        SyringePreset(
            name = "Epi 1:1,000",
            detail = "1mg/1mL",
            isWeightBased = false,
            desiredDose = "0.3",
            desiredUnit = MedicationUnit.MG,
            drugOnHand = "1",
            onHandUnit = MedicationUnit.MG,
            volumeOnHandMl = "1",
            pcgSection = "PCG 3.3 / 3.6 / 6.3",
            averageDoseSummary = "Adult IM: 0.3–0.5 mg | Peds: 0.01 mg/kg",
            normalRangeSummary = "Adult IM: 0.3 – 0.5 mg | Peds IM: 0.01 mg/kg (max 0.3–0.5 mg)",
            fixedMin = 0.3,
            fixedMax = 0.5,
            fixedUnit = MedicationUnit.MG,
            weightMin = 0.01,
            weightMax = 0.01,
            weightUnit = MedicationUnit.MG,
            maxSingleDose = 0.5,
            maxSingleUnit = MedicationUnit.MG,
            protocolNotes = "IM ONLY for anaphylaxis/asthma (never IV push 1:1,000). Peds 0.01 mg/kg (max 0.3–0.5 mg)."
        ),
        SyringePreset(
            name = "Epi 1:10,000",
            detail = "1mg/10mL",
            isWeightBased = false,
            desiredDose = "1",
            desiredUnit = MedicationUnit.MG,
            drugOnHand = "1",
            onHandUnit = MedicationUnit.MG,
            volumeOnHandMl = "10",
            pcgSection = "PCG 2.4 / 3.3 / 6.5",
            averageDoseSummary = "Arrest: 1 mg IV | Shock: 0.1–0.5 mg",
            normalRangeSummary = "Adult IV: 0.1 – 1 mg | Peds: 0.01 mg/kg (max 1 mg)",
            fixedMin = 0.1,
            fixedMax = 1.0,
            fixedUnit = MedicationUnit.MG,
            weightMin = 0.01,
            weightMax = 0.02,
            weightUnit = MedicationUnit.MG,
            maxSingleDose = 1.0,
            maxSingleUnit = MedicationUnit.MG,
            protocolNotes = "Cardiac arrest 1 mg IV q3–5m; anaphylactic shock 0.1–0.5 mg IV; Peds 0.01 mg/kg (neonatal 0.02)."
        ),
        SyringePreset(
            name = "Ondansetron",
            detail = "4mg/2mL",
            isWeightBased = false,
            desiredDose = "4",
            desiredUnit = MedicationUnit.MG,
            drugOnHand = "4",
            onHandUnit = MedicationUnit.MG,
            volumeOnHandMl = "2",
            pcgSection = "PCG 1.3",
            averageDoseSummary = "Adult: 4 mg | Peds: 0.1 mg/kg",
            normalRangeSummary = "Adult: 4 mg IV/IM/SL | Peds: 0.1 mg/kg (max 4 mg)",
            fixedMin = 4.0,
            fixedMax = 4.0,
            fixedUnit = MedicationUnit.MG,
            weightMin = 0.1,
            weightMax = 0.1,
            weightUnit = MedicationUnit.MG,
            maxSingleDose = 4.0,
            maxSingleUnit = MedicationUnit.MG,
            protocolNotes = "Standard single adult dose 4 mg; Peds 0.1 mg/kg up to 4 mg max."
        ),
        SyringePreset(
            name = "Morphine",
            detail = "10mg/1mL",
            isWeightBased = false,
            desiredDose = "4",
            desiredUnit = MedicationUnit.MG,
            drugOnHand = "10",
            onHandUnit = MedicationUnit.MG,
            volumeOnHandMl = "1",
            pcgSection = "PCG 1.7",
            averageDoseSummary = "Adult: 2–5 mg (Avg 4 mg) | Peds: 0.1 mg/kg",
            normalRangeSummary = "Adult: 2 – 5 mg IV | Peds: 0.1 mg/kg (max single 5 mg)",
            fixedMin = 2.0,
            fixedMax = 5.0,
            fixedUnit = MedicationUnit.MG,
            weightMin = 0.1,
            weightMax = 0.1,
            weightUnit = MedicationUnit.MG,
            maxSingleDose = 5.0,
            maxSingleUnit = MedicationUnit.MG,
            protocolNotes = "Hold or reduce if SBP < 90 mmHg or respiratory depression."
        ),
        SyringePreset(
            name = "Rocuronium",
            detail = "50mg/5mL",
            isWeightBased = true,
            desiredDose = "1",
            desiredUnit = MedicationUnit.MG,
            drugOnHand = "50",
            onHandUnit = MedicationUnit.MG,
            volumeOnHandMl = "5",
            pcgSection = "PCG 1.8 / 7.3",
            averageDoseSummary = "RSI: 1 mg/kg IV",
            normalRangeSummary = "0.6 – 1.2 mg/kg IV",
            weightMin = 0.6,
            weightMax = 1.2,
            weightUnit = MedicationUnit.MG,
            protocolNotes = "Standard RSI paralytic dose 1 mg/kg IV (range 0.6–1.2 mg/kg)."
        ),
        SyringePreset(
            name = "Etomidate",
            detail = "40mg/20mL",
            isWeightBased = true,
            desiredDose = "0.3",
            desiredUnit = MedicationUnit.MG,
            drugOnHand = "40",
            onHandUnit = MedicationUnit.MG,
            volumeOnHandMl = "20",
            pcgSection = "PCG 1.9 / 7.3",
            averageDoseSummary = "RSI: 0.3 mg/kg IV (Max 40 mg)",
            normalRangeSummary = "0.2 – 0.3 mg/kg IV (Max single 40 mg)",
            weightMin = 0.2,
            weightMax = 0.3,
            weightUnit = MedicationUnit.MG,
            maxSingleDose = 40.0,
            maxSingleUnit = MedicationUnit.MG,
            protocolNotes = "RSI induction 0.3 mg/kg (infant/neonate 0.2–0.3 mg/kg), max 40 mg. Excited delirium 10 mg IV x1."
        ),
        SyringePreset(
            name = "Atropine",
            detail = "1mg/10mL",
            isWeightBased = false,
            desiredDose = "1",
            desiredUnit = MedicationUnit.MG,
            drugOnHand = "1",
            onHandUnit = MedicationUnit.MG,
            volumeOnHandMl = "10",
            pcgSection = "PCG 2.3 / 3.19 / 6.5",
            averageDoseSummary = "Bradycardia: 1 mg IV | Peds: 0.02 mg/kg",
            normalRangeSummary = "Adult: 0.5 – 1 mg IV (OP: 2 mg) | Peds: 0.02 mg/kg (max 1 mg)",
            fixedMin = 0.5,
            fixedMax = 1.0,
            fixedUnit = MedicationUnit.MG,
            weightMin = 0.02,
            weightMax = 0.02,
            weightUnit = MedicationUnit.MG,
            maxSingleDose = 1.0,
            maxSingleUnit = MedicationUnit.MG,
            protocolNotes = "Symptomatic bradycardia 0.5–1 mg IV q3–5m (max 3 mg). Organophosphate poisoning: 2 mg IV."
        )
    )

    fun calculateVolumeDrip(
        volumeMl: Double?,
        timeValue: Double?,
        timeUnit: TimeUnit,
        dropFactorGttPerMl: Int?
    ): DripResult? {
        if (volumeMl == null || timeValue == null || dropFactorGttPerMl == null) return null
        if (volumeMl <= 0.0 || timeValue <= 0.0 || dropFactorGttPerMl <= 0) return null

        val totalMinutes = timeValue * timeUnit.toMinutesFactor
        val totalHours = totalMinutes / 60.0
        val gttPerMin = (volumeMl * dropFactorGttPerMl) / totalMinutes
        val mlPerHour = volumeMl / totalHours
        val secondsPerDrop = if (gttPerMin > 0.0) 60.0 / gttPerMin else null

        val warning = if (gttPerMin > 300.0) {
            "Rate exceeds 300 gtt/min — consider wider-bore macro tubing (10 gtt/mL) or pressure infusion."
        } else null

        return DripResult(
            gttPerMinute = gttPerMin,
            roundedGttPerMinute = gttPerMin.roundToInt(),
            gttPer15Seconds = gttPerMin / 4.0,
            mlPerHour = mlPerHour,
            secondsPerDrop = secondsPerDrop,
            warning = warning
        )
    }

    fun calculateDoseDrip(
        weight: Double?,
        weightUnit: WeightUnit,
        desiredDose: Double?,
        doseRateUnit: DoseRateUnit,
        drugAmount: Double?,
        drugUnit: MedicationUnit,
        bagVolumeMl: Double?,
        dropFactorGttPerMl: Int?
    ): DripResult? {
        if (desiredDose == null || drugAmount == null || bagVolumeMl == null || dropFactorGttPerMl == null) {
            return null
        }
        if (desiredDose <= 0.0 || drugAmount <= 0.0 || bagVolumeMl <= 0.0 || dropFactorGttPerMl <= 0) {
            return null
        }

        if (doseRateUnit.baseUnit.dimension != drugUnit.dimension) {
            return DripResult(
                gttPerMinute = 0.0,
                roundedGttPerMinute = 0,
                gttPer15Seconds = 0.0,
                mlPerHour = 0.0,
                secondsPerDrop = null,
                warning = "Unit mismatch: ${doseRateUnit.label} cannot be calculated from a ${drugUnit.label} bag."
            )
        }

        val weightKg = if (doseRateUnit.isWeightBased) {
            if (weight == null || weight <= 0.0) return null
            weightUnit.toKg(weight)
        } else {
            1.0
        }

        // Convert desired dose rate into base-units per minute
        val desiredInBaseUnitPerMin =
            (desiredDose * weightKg * doseRateUnit.baseUnit.factorToBase) /
                (if (doseRateUnit.perHour) 60.0 else 1.0)

        // Convert total bag drug into same base-units
        val bagDrugInBaseUnit = drugAmount * drugUnit.factorToBase
        val concentrationBasePerMl = bagDrugInBaseUnit / bagVolumeMl

        val mlPerMin = desiredInBaseUnitPerMin / concentrationBasePerMl
        val mlPerHour = mlPerMin * 60.0
        val gttPerMin = mlPerMin * dropFactorGttPerMl
        val secondsPerDrop = if (gttPerMin > 0.0) 60.0 / gttPerMin else null

        val concInDisplayUnit = concentrationBasePerMl / doseRateUnit.baseUnit.factorToBase
        val concentrationText = "${formatNumber(concInDisplayUnit)} ${doseRateUnit.baseUnit.label}/mL"

        val warning = if (gttPerMin > 300.0) {
            "Rate exceeds 300 gtt/min — verify concentration and tubing drop factor."
        } else null

        return DripResult(
            gttPerMinute = gttPerMin,
            roundedGttPerMinute = gttPerMin.roundToInt(),
            gttPer15Seconds = gttPerMin / 4.0,
            mlPerHour = mlPerHour,
            secondsPerDrop = secondsPerDrop,
            concentrationText = concentrationText,
            warning = warning
        )
    }

    fun calculateOxygenDuration(
        cylinder: OxygenCylinder,
        currentPsi: Double?,
        flowRateLpm: Double?,
        safeResidualPsi: Double = 200.0
    ): OxygenResult? {
        if (currentPsi == null || flowRateLpm == null) return null
        if (currentPsi < 0.0 || flowRateLpm <= 0.0) return null

        val usablePsi = (currentPsi - safeResidualPsi).coerceAtLeast(0.0)
        val usableLiters = usablePsi * cylinder.factor
        val totalLiters = currentPsi * cylinder.factor

        val usableMinutes = usableLiters / flowRateLpm
        val totalMinutesToZero = totalLiters / flowRateLpm

        val gaugeFraction = (currentPsi / cylinder.standardFullPsi).toFloat().coerceIn(0f, 1f)
        val safeResidualFraction = (safeResidualPsi / cylinder.standardFullPsi).toFloat().coerceIn(0f, 1f)

        val status = when {
            usableMinutes <= 0.0 || usableMinutes < 10.0 -> OxygenStatus.CRITICAL
            usableMinutes < 30.0 -> OxygenStatus.CAUTION
            else -> OxygenStatus.SAFE
        }

        return OxygenResult(
            usableMinutes = usableMinutes,
            totalMinutesToZero = totalMinutesToZero,
            usableLiters = usableLiters,
            totalLiters = totalLiters,
            gaugeFraction = gaugeFraction,
            safeResidualFraction = safeResidualFraction,
            status = status
        )
    }

    fun calculateSyringeDose(
        desiredDose: Double?,
        desiredUnit: MedicationUnit,
        isWeightBased: Boolean,
        weight: Double?,
        weightUnit: WeightUnit,
        drugOnHand: Double?,
        onHandUnit: MedicationUnit,
        volumeOnHandMl: Double?
    ): SyringeResult? {
        if (desiredDose == null || drugOnHand == null || volumeOnHandMl == null) return null
        if (desiredDose <= 0.0 || drugOnHand <= 0.0 || volumeOnHandMl <= 0.0) return null

        if (desiredUnit.dimension != onHandUnit.dimension) {
            return SyringeResult(
                volumeToDrawMl = 0.0,
                totalDoseFormatted = "--",
                concentrationFormatted = "--",
                recommendedSyringeMl = 3,
                vialsNeeded = 0,
                error = "Incompatible units: cannot draw a ${desiredUnit.label} order from a ${onHandUnit.label} vial."
            )
        }

        val effectiveDesired = if (isWeightBased) {
            if (weight == null || weight <= 0.0) return null
            desiredDose * weightUnit.toKg(weight)
        } else {
            desiredDose
        }

        val desiredInBase = effectiveDesired * desiredUnit.factorToBase
        val onHandInBase = drugOnHand * onHandUnit.factorToBase
        val concentrationBasePerMl = onHandInBase / volumeOnHandMl

        val volumeToDrawMl = desiredInBase / concentrationBasePerMl
        val concentrationInOnHandUnit = drugOnHand / volumeOnHandMl

        val recommendedSyringe = when {
            volumeToDrawMl <= 1.0 -> 1
            volumeToDrawMl <= 3.0 -> 3
            volumeToDrawMl <= 5.0 -> 5
            volumeToDrawMl <= 10.0 -> 10
            volumeToDrawMl <= 20.0 -> 20
            else -> 60
        }

        val vialsNeeded = ceil(volumeToDrawMl / volumeOnHandMl).toInt().coerceAtLeast(1)

        val warning = when {
            volumeToDrawMl > 60.0 -> "Calculated volume exceeds 60 mL — double-check dose and concentration."
            vialsNeeded > 1 -> "Requires $vialsNeeded vials (${formatNumber(volumeOnHandMl)} mL each)."
            else -> null
        }

        return SyringeResult(
            volumeToDrawMl = volumeToDrawMl,
            totalDoseFormatted = "${formatNumber(effectiveDesired)} ${desiredUnit.label}",
            concentrationFormatted = "${formatNumber(concentrationInOnHandUnit)} ${onHandUnit.label}/mL",
            recommendedSyringeMl = recommendedSyringe,
            vialsNeeded = vialsNeeded,
            warning = warning
        )
    }

    fun calculateIbw(
        sex: PatientSex,
        heightUnit: HeightUnit,
        feet: Double?,
        inches: Double?,
        cm: Double?
    ): IbwResult? {
        val totalInches = when (heightUnit) {
            HeightUnit.FT_IN -> {
                val ft = feet ?: 0.0
                val inc = inches ?: 0.0
                if (ft <= 0.0 && inc <= 0.0) return null
                (ft * 12.0) + inc
            }
            HeightUnit.CM -> {
                val cmVal = cm ?: return null
                if (cmVal <= 0.0) return null
                cmVal / 2.54
            }
        }

        if (totalInches <= 0.0) return null
        val heightCm = totalInches * 2.54

        // Standard Devine formula: Base + 2.3 kg per inch over 60 inches (5 ft)
        val rawIbwKg = sex.devineBaseKg + 2.3 * (totalInches - 60.0)
        val ibwKg = rawIbwKg.coerceAtLeast(1.0)
        val ibwLbs = ibwKg * 2.20462262

        return IbwResult(
            ibwKg = ibwKg,
            ibwLbs = ibwLbs,
            heightInches = totalInches,
            heightCm = heightCm,
            tidalVolumeLowMl = (ibwKg * 6.0).roundToInt(),
            tidalVolumeHighMl = (ibwKg * 8.0).roundToInt(),
            isUnderFiveFeet = totalInches < 60.0
        )
    }

    fun calculateGcs(
        eye: GcsEye?,
        verbal: GcsVerbal?,
        motor: GcsMotor?
    ): GcsResult? {
        if (eye == null || verbal == null || motor == null) return null
        val total = eye.score + verbal.score + motor.score
        val severity = when {
            total >= 13 -> GcsSeverity.MILD
            total >= 9 -> GcsSeverity.MODERATE
            else -> GcsSeverity.SEVERE
        }
        return GcsResult(
            totalScore = total,
            eyeScore = eye.score,
            verbalScore = verbal.score,
            motorScore = motor.score,
            severity = severity,
            airwayAlert = total <= 8
        )
    }

    fun findDripPreset(name: String?): DripPreset? =
        if (name == null) null else DRIP_PRESETS.firstOrNull { it.name == name }

    fun findSyringePreset(name: String?): SyringePreset? =
        if (name == null) null else SYRINGE_PRESETS.firstOrNull { it.name == name }

    fun validateDripDose(
        preset: DripPreset,
        desiredDose: Double?,
        doseRateUnit: DoseRateUnit,
        mode: DripMode,
        bagVolumeMl: Double?,
        timeValue: Double?,
        timeUnit: TimeUnit
    ): ProtocolDoseValidation {
        if (preset.mode == DripMode.VOLUME_TIME) {
            return if (mode == DripMode.VOLUME_TIME) {
                val durationMin = if (timeValue != null && timeValue > 0.0) {
                    timeValue * timeUnit.toMinutesFactor
                } else null
                val (status, message) = when {
                    durationMin == null || bagVolumeMl == null || bagVolumeMl <= 0.0 ->
                        DoseRangeStatus.INFO_ONLY to "Enter volume and duration to verify 10-minute loading infusion."
                    durationMin < 9.99 ->
                        DoseRangeStatus.ABOVE_MAX to "Infusion time (${formatNumber(durationMin, 1)} min) is faster than the 10 min protocol bolus."
                    else ->
                        DoseRangeStatus.WITHIN_RANGE to "Matches protocol bolus timing (${formatNumber(durationMin, 0)} min)."
                }
                ProtocolDoseValidation(
                    medName = preset.name,
                    pcgSection = preset.pcgSection,
                    standardMix = preset.subtitle,
                    averageDoseSummary = preset.averageDoseSummary,
                    normalRangeSummary = preset.normalRangeSummary,
                    notes = preset.protocolNotes,
                    status = status,
                    message = message
                )
            } else {
                ProtocolDoseValidation(
                    medName = preset.name,
                    pcgSection = preset.pcgSection,
                    standardMix = preset.subtitle,
                    averageDoseSummary = preset.averageDoseSummary,
                    normalRangeSummary = preset.normalRangeSummary,
                    notes = preset.protocolNotes,
                    status = DoseRangeStatus.INFO_ONLY,
                    message = "Load uses Volume / Time (150 mg / 100 mL over 10 min). Maintenance is 60 mg/hr ×6h, then 30 mg/hr."
                )
            }
        }

        val hasAnyRange = (preset.primaryRateUnit != null &&
            preset.minRatePrimary != null &&
            preset.maxRatePrimary != null) ||
            (preset.altRateUnit != null &&
                preset.altMinRate != null &&
                preset.altMaxRate != null)

        if (!hasAnyRange) {
            return ProtocolDoseValidation(
                medName = preset.name,
                pcgSection = preset.pcgSection,
                standardMix = preset.subtitle,
                averageDoseSummary = preset.averageDoseSummary,
                normalRangeSummary = preset.normalRangeSummary,
                notes = preset.protocolNotes,
                status = DoseRangeStatus.INFO_ONLY,
                message = "Generic / custom infusion — select concentration & bag volume below; no protocol limits enforced."
            )
        }

        if (mode != DripMode.MED_INFUSION) {
            return ProtocolDoseValidation(
                medName = preset.name,
                pcgSection = preset.pcgSection,
                standardMix = preset.subtitle,
                averageDoseSummary = preset.averageDoseSummary,
                normalRangeSummary = preset.normalRangeSummary,
                notes = preset.protocolNotes,
                status = DoseRangeStatus.INFO_ONLY,
                message = "Switch to Med Dose Drip to validate rate against protocol range."
            )
        }

        if (desiredDose == null || desiredDose <= 0.0) {
            return ProtocolDoseValidation(
                medName = preset.name,
                pcgSection = preset.pcgSection,
                standardMix = preset.subtitle,
                averageDoseSummary = preset.averageDoseSummary,
                normalRangeSummary = preset.normalRangeSummary,
                notes = preset.protocolNotes,
                status = DoseRangeStatus.INFO_ONLY,
                message = "Enter ordered dose to check against ${preset.normalRangeSummary}."
            )
        }

        val primaryUnit = preset.primaryRateUnit
        val altUnit = preset.altRateUnit
        val matchedRange = when {
            primaryUnit != null &&
                preset.minRatePrimary != null &&
                preset.maxRatePrimary != null &&
                canConvertDoseRate(doseRateUnit, primaryUnit) -> {
                Triple(preset.minRatePrimary, preset.maxRatePrimary, primaryUnit)
            }
            altUnit != null &&
                preset.altMinRate != null &&
                preset.altMaxRate != null &&
                canConvertDoseRate(doseRateUnit, altUnit) -> {
                Triple(preset.altMinRate, preset.altMaxRate, altUnit)
            }
            else -> null
        }

        if (matchedRange == null) {
            return ProtocolDoseValidation(
                medName = preset.name,
                pcgSection = preset.pcgSection,
                standardMix = preset.subtitle,
                averageDoseSummary = preset.averageDoseSummary,
                normalRangeSummary = preset.normalRangeSummary,
                notes = preset.protocolNotes,
                status = DoseRangeStatus.INFO_ONLY,
                message = "Switch rate unit to ${primaryUnit?.label ?: "protocol unit"} to validate dose range."
            )
        }

        val (minRate, maxRate, targetUnit) = matchedRange
        val convertedDose = convertDoseRate(desiredDose, doseRateUnit, targetUnit) ?: desiredDose
        val eps = 1e-9

        val (status, message) = when {
            convertedDose < minRate - eps -> {
                DoseRangeStatus.BELOW_MIN to
                    "Below protocol min (${formatNumber(minRate)} ${targetUnit.label}). Usual: ${preset.averageDoseSummary}."
            }
            convertedDose > maxRate + eps -> {
                val extra = if (preset.name == "Vasopressin" && convertedDose <= 0.205) {
                    " (GI bleed protocol is 0.2 units/min)"
                } else ""
                DoseRangeStatus.ABOVE_MAX to
                    "Exceeds protocol max (${formatNumber(maxRate)} ${targetUnit.label})$extra. Verify order."
            }
            else -> {
                DoseRangeStatus.WITHIN_RANGE to
                    "Within protocol range (${formatNumber(minRate)}–${formatNumber(maxRate)} ${targetUnit.label})."
            }
        }

        return ProtocolDoseValidation(
            medName = preset.name,
            pcgSection = preset.pcgSection,
            standardMix = preset.subtitle,
            averageDoseSummary = preset.averageDoseSummary,
            normalRangeSummary = preset.normalRangeSummary,
            notes = preset.protocolNotes,
            status = status,
            message = message
        )
    }

    fun validateSyringeDose(
        preset: SyringePreset,
        desiredDose: Double?,
        desiredUnit: MedicationUnit,
        isWeightBased: Boolean,
        weight: Double?,
        weightUnit: WeightUnit
    ): ProtocolDoseValidation {
        val hasAnyRange = (preset.fixedMin != null && preset.fixedMax != null && preset.fixedUnit != null) ||
            (preset.weightMin != null && preset.weightMax != null && preset.weightUnit != null) ||
            (preset.maxSingleDose != null && preset.maxSingleUnit != null)

        if (!hasAnyRange) {
            return ProtocolDoseValidation(
                medName = preset.name,
                pcgSection = preset.pcgSection,
                standardMix = preset.detail,
                averageDoseSummary = preset.averageDoseSummary,
                normalRangeSummary = preset.normalRangeSummary,
                notes = preset.protocolNotes,
                status = DoseRangeStatus.INFO_ONLY,
                message = "Generic / custom vial — select concentration & liquid volume below; no protocol limits enforced."
            )
        }

        if (desiredDose == null || desiredDose <= 0.0) {
            return ProtocolDoseValidation(
                medName = preset.name,
                pcgSection = preset.pcgSection,
                standardMix = preset.detail,
                averageDoseSummary = preset.averageDoseSummary,
                normalRangeSummary = preset.normalRangeSummary,
                notes = preset.protocolNotes,
                status = DoseRangeStatus.INFO_ONLY,
                message = "Enter ordered dose to check against ${preset.normalRangeSummary}."
            )
        }

        val eps = 1e-9

        if (isWeightBased) {
            val wMin = preset.weightMin
            val wMax = preset.weightMax
            val wUnit = preset.weightUnit
            if (wMin == null || wMax == null || wUnit == null) {
                return ProtocolDoseValidation(
                    medName = preset.name,
                    pcgSection = preset.pcgSection,
                    standardMix = preset.detail,
                    averageDoseSummary = preset.averageDoseSummary,
                    normalRangeSummary = preset.normalRangeSummary,
                    notes = preset.protocolNotes,
                    status = DoseRangeStatus.INFO_ONLY,
                    message = "Protocol uses fixed dosing (${preset.normalRangeSummary})."
                )
            }
            if (desiredUnit.dimension != wUnit.dimension) {
                return ProtocolDoseValidation(
                    medName = preset.name,
                    pcgSection = preset.pcgSection,
                    standardMix = preset.detail,
                    averageDoseSummary = preset.averageDoseSummary,
                    normalRangeSummary = preset.normalRangeSummary,
                    notes = preset.protocolNotes,
                    status = DoseRangeStatus.INFO_ONLY,
                    message = "Select ${wUnit.label}/kg to validate weight-based protocol range."
                )
            }

            val dosePerKgInTarget = (desiredDose * desiredUnit.factorToBase) / wUnit.factorToBase
            val maxSingle = preset.maxSingleDose
            val maxSingleUnit = preset.maxSingleUnit
            val totalInMaxUnit = if (
                maxSingle != null &&
                maxSingleUnit != null &&
                weight != null &&
                weight > 0.0 &&
                desiredUnit.dimension == maxSingleUnit.dimension
            ) {
                val totalDose = desiredDose * weightUnit.toKg(weight)
                (totalDose * desiredUnit.factorToBase) / maxSingleUnit.factorToBase
            } else null

            val (status, message) = when {
                totalInMaxUnit != null && maxSingle != null && maxSingleUnit != null && totalInMaxUnit > maxSingle + eps -> {
                    DoseRangeStatus.MAX_SINGLE_EXCEEDED to
                        "Total dose (${formatNumber(totalInMaxUnit, 1)} ${maxSingleUnit.label}) exceeds max single dose of ${formatNumber(maxSingle)} ${maxSingleUnit.label}."
                }
                dosePerKgInTarget > wMax + eps -> {
                    DoseRangeStatus.ABOVE_MAX to
                        "Exceeds protocol max (${formatNumber(wMax)} ${wUnit.label}/kg). Verify dose."
                }
                dosePerKgInTarget < wMin - eps -> {
                    DoseRangeStatus.BELOW_MIN to
                        "Below protocol min (${formatNumber(wMin)} ${wUnit.label}/kg). Usual: ${preset.averageDoseSummary}."
                }
                totalInMaxUnit != null && maxSingle != null && maxSingleUnit != null -> {
                    DoseRangeStatus.WITHIN_RANGE to
                        "Within protocol range (${formatNumber(totalInMaxUnit, 1)} / ${formatNumber(maxSingle)} ${maxSingleUnit.label} max single)."
                }
                else -> {
                    DoseRangeStatus.WITHIN_RANGE to
                        "Within protocol range (${formatNumber(wMin)}–${formatNumber(wMax)} ${wUnit.label}/kg)."
                }
            }

            return ProtocolDoseValidation(
                medName = preset.name,
                pcgSection = preset.pcgSection,
                standardMix = preset.detail,
                averageDoseSummary = preset.averageDoseSummary,
                normalRangeSummary = preset.normalRangeSummary,
                notes = preset.protocolNotes,
                status = status,
                message = message
            )
        } else {
            val fMin = preset.fixedMin
            val fMax = preset.fixedMax
            val fUnit = preset.fixedUnit
            if (fMin == null || fMax == null || fUnit == null) {
                val maxSingle = preset.maxSingleDose
                val maxSingleUnit = preset.maxSingleUnit
                if (maxSingle != null && maxSingleUnit != null && desiredUnit.dimension == maxSingleUnit.dimension) {
                    val doseInMaxUnit = (desiredDose * desiredUnit.factorToBase) / maxSingleUnit.factorToBase
                    if (doseInMaxUnit > maxSingle + eps) {
                        return ProtocolDoseValidation(
                            medName = preset.name,
                            pcgSection = preset.pcgSection,
                            standardMix = preset.detail,
                            averageDoseSummary = preset.averageDoseSummary,
                            normalRangeSummary = preset.normalRangeSummary,
                            notes = preset.protocolNotes,
                            status = DoseRangeStatus.MAX_SINGLE_EXCEEDED,
                            message = "Exceeds max single dose of ${formatNumber(maxSingle)} ${maxSingleUnit.label}."
                        )
                    }
                }
                return ProtocolDoseValidation(
                    medName = preset.name,
                    pcgSection = preset.pcgSection,
                    standardMix = preset.detail,
                    averageDoseSummary = preset.averageDoseSummary,
                    normalRangeSummary = preset.normalRangeSummary,
                    notes = preset.protocolNotes,
                    status = DoseRangeStatus.INFO_ONLY,
                    message = "Weight-based protocol (${preset.normalRangeSummary}). Enable /kg toggle to validate."
                )
            }

            if (desiredUnit.dimension != fUnit.dimension) {
                return ProtocolDoseValidation(
                    medName = preset.name,
                    pcgSection = preset.pcgSection,
                    standardMix = preset.detail,
                    averageDoseSummary = preset.averageDoseSummary,
                    normalRangeSummary = preset.normalRangeSummary,
                    notes = preset.protocolNotes,
                    status = DoseRangeStatus.INFO_ONLY,
                    message = "Select ${fUnit.label} to validate adult fixed protocol range."
                )
            }

            val doseInTarget = (desiredDose * desiredUnit.factorToBase) / fUnit.factorToBase
            val (status, message) = when {
                doseInTarget < fMin - eps -> {
                    DoseRangeStatus.BELOW_MIN to
                        "Below protocol min (${formatNumber(fMin)} ${fUnit.label}). Usual: ${preset.averageDoseSummary}."
                }
                doseInTarget > fMax + eps -> {
                    val extra = when (preset.name) {
                        "Midazolam" -> " (10 mg is IM-only active seizure)"
                        "Atropine" -> " (2 mg is organophosphate poisoning)"
                        else -> ""
                    }
                    DoseRangeStatus.ABOVE_MAX to
                        "Exceeds protocol max (${formatNumber(fMax)} ${fUnit.label})$extra. Verify dose."
                }
                else -> {
                    val rangeStr = if (fMin == fMax) {
                        "${formatNumber(fMin)} ${fUnit.label}"
                    } else {
                        "${formatNumber(fMin)}–${formatNumber(fMax)} ${fUnit.label}"
                    }
                    DoseRangeStatus.WITHIN_RANGE to "Within adult protocol range ($rangeStr)."
                }
            }

            return ProtocolDoseValidation(
                medName = preset.name,
                pcgSection = preset.pcgSection,
                standardMix = preset.detail,
                averageDoseSummary = preset.averageDoseSummary,
                normalRangeSummary = preset.normalRangeSummary,
                notes = preset.protocolNotes,
                status = status,
                message = message
            )
        }
    }

    private fun canConvertDoseRate(from: DoseRateUnit, to: DoseRateUnit): Boolean =
        from.isWeightBased == to.isWeightBased && from.baseUnit.dimension == to.baseUnit.dimension

    private fun convertDoseRate(value: Double, from: DoseRateUnit, to: DoseRateUnit): Double? {
        if (!canConvertDoseRate(from, to)) return null
        val basePerMin = (value * from.baseUnit.factorToBase) / (if (from.perHour) 60.0 else 1.0)
        return (basePerMin * (if (to.perHour) 60.0 else 1.0)) / to.baseUnit.factorToBase
    }

    fun formatDuration(totalMinutes: Double): String {
        if (totalMinutes <= 0.0) return "0 min"
        val roundedMin = totalMinutes.roundToInt()
        val hours = roundedMin / 60
        val mins = roundedMin % 60
        return when {
            hours > 0 && mins > 0 -> "${hours}h ${mins}m"
            hours > 0 -> "${hours}h 0m"
            else -> "${mins} min"
        }
    }

    fun formatNumber(value: Double, maxDecimals: Int = 2): String {
        if (value.isNaN() || value.isInfinite()) return "--"
        val rounded = String.format(Locale.US, "%.${maxDecimals}f", value)
        return if (rounded.contains('.')) {
            rounded.trimEnd('0').trimEnd('.')
        } else {
            rounded
        }
    }
}
