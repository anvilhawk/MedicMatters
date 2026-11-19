package com.example.domain

enum class MedicTab(val label: String) {
    DRIP("Drip"),
    OXYGEN("O2 Tank"),
    SYRINGE("Syringe"),
    IBW("IBW"),
    GCS("GCS")
}

enum class DripMode(val label: String) {
    VOLUME_TIME("Volume / Time"),
    MED_INFUSION("Med Dose Drip")
}

enum class TimeUnit(val label: String, val toMinutesFactor: Double) {
    MINUTES("min", 1.0),
    HOURS("hr", 60.0)
}

enum class WeightUnit(val label: String) {
    KG("kg"),
    LBS("lbs");

    fun toKg(value: Double): Double = when (this) {
        KG -> value
        LBS -> value / 2.20462262
    }
}

enum class UnitDimension {
    MASS,
    UNITS,
    MEQ
}

enum class MedicationUnit(
    val label: String,
    val dimension: UnitDimension,
    val factorToBase: Double // base for MASS = mcg, UNITS = units, MEQ = mEq
) {
    MCG("mcg", UnitDimension.MASS, 1.0),
    MG("mg", UnitDimension.MASS, 1_000.0),
    G("g", UnitDimension.MASS, 1_000_000.0),
    UNITS("units", UnitDimension.UNITS, 1.0),
    MEQ("mEq", UnitDimension.MEQ, 1.0)
}

enum class DoseRateUnit(
    val label: String,
    val baseUnit: MedicationUnit,
    val isWeightBased: Boolean,
    val perHour: Boolean
) {
    MCG_KG_MIN("mcg/kg/min", MedicationUnit.MCG, true, false),
    MCG_KG_HR("mcg/kg/hr", MedicationUnit.MCG, true, true),
    MCG_MIN("mcg/min", MedicationUnit.MCG, false, false),
    MG_MIN("mg/min", MedicationUnit.MG, false, false),
    MG_HR("mg/hr", MedicationUnit.MG, false, true),
    UNITS_HR("units/hr", MedicationUnit.UNITS, false, true)
}

data class DripResult(
    val gttPerMinute: Double,
    val roundedGttPerMinute: Int,
    val gttPer15Seconds: Double,
    val mlPerHour: Double,
    val secondsPerDrop: Double?,
    val concentrationText: String? = null,
    val warning: String? = null
)

data class DripPreset(
    val name: String,
    val subtitle: String,
    val mode: DripMode,
    val doseValue: String = "",
    val doseRateUnit: DoseRateUnit = DoseRateUnit.MCG_KG_MIN,
    val drugAmount: String = "",
    val drugUnit: MedicationUnit = MedicationUnit.MG,
    val bagVolumeMl: String = "",
    val timeValue: String = "",
    val timeUnit: TimeUnit = TimeUnit.MINUTES,
    val dropFactor: Int = 60
)

enum class OxygenCylinder(
    val shortName: String,
    val fullName: String,
    val factor: Double,
    val standardFullPsi: Int = 2000
) {
    D("D", "D Cylinder", 0.16, 2000),
    JUMBO_D("Jumbo D", "Jumbo D", 0.28, 2000),
    E("E", "E Cylinder", 0.28, 2000),
    M("M", "M (Main)", 1.56, 2000),
    G("G", "G Cylinder", 2.41, 2000),
    H_K("H/K", "H / K Cylinder", 3.14, 2000)
}

enum class OxygenStatus {
    SAFE,
    CAUTION,
    CRITICAL
}

data class OxygenResult(
    val usableMinutes: Double,
    val totalMinutesToZero: Double,
    val usableLiters: Double,
    val totalLiters: Double,
    val gaugeFraction: Float,
    val safeResidualFraction: Float,
    val status: OxygenStatus
)

data class SyringeResult(
    val volumeToDrawMl: Double,
    val totalDoseFormatted: String,
    val concentrationFormatted: String,
    val recommendedSyringeMl: Int,
    val vialsNeeded: Int,
    val error: String? = null,
    val warning: String? = null
)

data class SyringePreset(
    val name: String,
    val detail: String,
    val isWeightBased: Boolean,
    val desiredDose: String,
    val desiredUnit: MedicationUnit,
    val drugOnHand: String,
    val onHandUnit: MedicationUnit,
    val volumeOnHandMl: String
)

enum class PatientSex(val label: String, val devineBaseKg: Double) {
    MALE("Male", 50.0),
    FEMALE("Female", 45.5)
}

enum class HeightUnit(val label: String) {
    FT_IN("ft / in"),
    CM("cm")
}

data class IbwResult(
    val ibwKg: Double,
    val ibwLbs: Double,
    val heightInches: Double,
    val heightCm: Double,
    val tidalVolumeLowMl: Int,
    val tidalVolumeHighMl: Int,
    val isUnderFiveFeet: Boolean
)

enum class GcsEye(val score: Int, val label: String) {
    SPONTANEOUS(4, "Spontaneous"),
    VERBAL(3, "To Verbal / Speech"),
    PAIN(2, "To Pain / Pressure"),
    NONE(1, "None")
}

enum class GcsVerbal(val score: Int, val label: String) {
    ORIENTED(5, "Oriented"),
    CONFUSED(4, "Confused"),
    INAPPROPRIATE_WORDS(3, "Inappropriate Words"),
    INCOMPREHENSIBLE(2, "Incomprehensible Sounds"),
    NONE(1, "None")
}

enum class GcsMotor(val score: Int, val label: String) {
    OBEYS_COMMANDS(6, "Obeys Commands"),
    LOCALIZES_PAIN(5, "Localizes Pain"),
    WITHDRAWS_PAIN(4, "Withdraws from Pain"),
    ABNORMAL_FLEXION(3, "Abnormal Flexion (Decorticate)"),
    ABNORMAL_EXTENSION(2, "Abnormal Extension (Decerebrate)"),
    NONE(1, "None")
}

enum class GcsSeverity(val label: String, val rangeText: String) {
    MILD("Mild Impairment", "13–15"),
    MODERATE("Moderate Impairment", "9–12"),
    SEVERE("Severe / Coma", "3–8")
}

data class GcsResult(
    val totalScore: Int,
    val eyeScore: Int,
    val verbalScore: Int,
    val motorScore: Int,
    val severity: GcsSeverity,
    val airwayAlert: Boolean
)
