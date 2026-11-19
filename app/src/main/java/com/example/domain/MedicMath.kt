package com.example.domain

import java.util.Locale
import kotlin.math.ceil
import kotlin.math.roundToInt

object MedicMath {

    val STANDARD_DROP_FACTORS = listOf(10, 15, 20, 60)

    val DRIP_PRESETS = listOf(
        DripPreset(
            name = "Dopamine",
            subtitle = "400mg/250mL",
            mode = DripMode.MED_INFUSION,
            doseValue = "5",
            doseRateUnit = DoseRateUnit.MCG_KG_MIN,
            drugAmount = "400",
            drugUnit = MedicationUnit.MG,
            bagVolumeMl = "250",
            dropFactor = 60
        ),
        DripPreset(
            name = "Norepi",
            subtitle = "4mg/250mL",
            mode = DripMode.MED_INFUSION,
            doseValue = "4",
            doseRateUnit = DoseRateUnit.MCG_MIN,
            drugAmount = "4",
            drugUnit = MedicationUnit.MG,
            bagVolumeMl = "250",
            dropFactor = 60
        ),
        DripPreset(
            name = "Epi Drip",
            subtitle = "4mg/250mL",
            mode = DripMode.MED_INFUSION,
            doseValue = "2",
            doseRateUnit = DoseRateUnit.MCG_MIN,
            drugAmount = "4",
            drugUnit = MedicationUnit.MG,
            bagVolumeMl = "250",
            dropFactor = 60
        ),
        DripPreset(
            name = "Amiodarone",
            subtitle = "150mg/100mL",
            mode = DripMode.VOLUME_TIME,
            bagVolumeMl = "100",
            timeValue = "10",
            timeUnit = TimeUnit.MINUTES,
            dropFactor = 10
        ),
        DripPreset(
            name = "Lidocaine",
            subtitle = "2g/500mL",
            mode = DripMode.MED_INFUSION,
            doseValue = "2",
            doseRateUnit = DoseRateUnit.MG_MIN,
            drugAmount = "2",
            drugUnit = MedicationUnit.G,
            bagVolumeMl = "500",
            dropFactor = 60
        )
    )

    val SYRINGE_PRESETS = listOf(
        SyringePreset(
            name = "Fentanyl",
            detail = "100mcg/2mL",
            isWeightBased = true,
            desiredDose = "1",
            desiredUnit = MedicationUnit.MCG,
            drugOnHand = "100",
            onHandUnit = MedicationUnit.MCG,
            volumeOnHandMl = "2"
        ),
        SyringePreset(
            name = "Ketamine",
            detail = "500mg/10mL",
            isWeightBased = true,
            desiredDose = "1",
            desiredUnit = MedicationUnit.MG,
            drugOnHand = "500",
            onHandUnit = MedicationUnit.MG,
            volumeOnHandMl = "10"
        ),
        SyringePreset(
            name = "Midazolam",
            detail = "10mg/2mL",
            isWeightBased = false,
            desiredDose = "5",
            desiredUnit = MedicationUnit.MG,
            drugOnHand = "10",
            onHandUnit = MedicationUnit.MG,
            volumeOnHandMl = "2"
        ),
        SyringePreset(
            name = "Epi 1:1,000",
            detail = "1mg/1mL",
            isWeightBased = false,
            desiredDose = "0.3",
            desiredUnit = MedicationUnit.MG,
            drugOnHand = "1",
            onHandUnit = MedicationUnit.MG,
            volumeOnHandMl = "1"
        ),
        SyringePreset(
            name = "Ondansetron",
            detail = "4mg/2mL",
            isWeightBased = false,
            desiredDose = "4",
            desiredUnit = MedicationUnit.MG,
            drugOnHand = "4",
            onHandUnit = MedicationUnit.MG,
            volumeOnHandMl = "2"
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
