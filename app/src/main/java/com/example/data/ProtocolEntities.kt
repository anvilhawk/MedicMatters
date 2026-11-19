package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.DoseRateUnit
import com.example.domain.DripMode
import com.example.domain.DripPreset
import com.example.domain.MedicationUnit
import com.example.domain.SyringePreset
import com.example.domain.TimeUnit

@Entity(tableName = "drip_protocols")
data class DripProtocolEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sortOrder: Int = 0,
    val name: String,
    val subtitle: String,
    val modeName: String,
    val doseValue: String,
    val doseRateUnitName: String,
    val drugAmount: String,
    val drugUnitName: String,
    val bagVolumeMl: String,
    val timeValue: String,
    val timeUnitName: String,
    val dropFactor: Int,
    val pcgSection: String,
    val averageDoseSummary: String,
    val normalRangeSummary: String,
    val minRatePrimary: Double?,
    val maxRatePrimary: Double?,
    val primaryRateUnitName: String?,
    val altMinRate: Double?,
    val altMaxRate: Double?,
    val altRateUnitName: String?,
    val protocolNotes: String?
)

@Entity(tableName = "syringe_protocols")
data class SyringeProtocolEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sortOrder: Int = 0,
    val name: String,
    val detail: String,
    val isWeightBased: Boolean,
    val desiredDose: String,
    val desiredUnitName: String,
    val drugOnHand: String,
    val onHandUnitName: String,
    val volumeOnHandMl: String,
    val pcgSection: String,
    val averageDoseSummary: String,
    val normalRangeSummary: String,
    val fixedMin: Double?,
    val fixedMax: Double?,
    val fixedUnitName: String?,
    val weightMin: Double?,
    val weightMax: Double?,
    val weightUnitName: String?,
    val maxSingleDose: Double?,
    val maxSingleUnitName: String?,
    val protocolNotes: String?
)

fun DripProtocolEntity.toPreset(): DripPreset = DripPreset(
    id = id,
    sortOrder = sortOrder,
    name = name,
    subtitle = subtitle,
    mode = enumValueOrDefault(modeName, DripMode.MED_INFUSION),
    doseValue = doseValue,
    doseRateUnit = enumValueOrDefault(doseRateUnitName, DoseRateUnit.MCG_KG_MIN),
    drugAmount = drugAmount,
    drugUnit = enumValueOrDefault(drugUnitName, MedicationUnit.MG),
    bagVolumeMl = bagVolumeMl,
    timeValue = timeValue,
    timeUnit = enumValueOrDefault(timeUnitName, TimeUnit.MINUTES),
    dropFactor = dropFactor,
    pcgSection = pcgSection,
    averageDoseSummary = averageDoseSummary,
    normalRangeSummary = normalRangeSummary,
    minRatePrimary = minRatePrimary,
    maxRatePrimary = maxRatePrimary,
    primaryRateUnit = primaryRateUnitName?.let { enumValueOrNull<DoseRateUnit>(it) },
    altMinRate = altMinRate,
    altMaxRate = altMaxRate,
    altRateUnit = altRateUnitName?.let { enumValueOrNull<DoseRateUnit>(it) },
    protocolNotes = protocolNotes
)

fun DripPreset.toEntity(sortOrderOverride: Int = sortOrder): DripProtocolEntity = DripProtocolEntity(
    id = id,
    sortOrder = sortOrderOverride,
    name = name,
    subtitle = subtitle,
    modeName = mode.name,
    doseValue = doseValue,
    doseRateUnitName = doseRateUnit.name,
    drugAmount = drugAmount,
    drugUnitName = drugUnit.name,
    bagVolumeMl = bagVolumeMl,
    timeValue = timeValue,
    timeUnitName = timeUnit.name,
    dropFactor = dropFactor,
    pcgSection = pcgSection,
    averageDoseSummary = averageDoseSummary,
    normalRangeSummary = normalRangeSummary,
    minRatePrimary = minRatePrimary,
    maxRatePrimary = maxRatePrimary,
    primaryRateUnitName = primaryRateUnit?.name,
    altMinRate = altMinRate,
    altMaxRate = altMaxRate,
    altRateUnitName = altRateUnit?.name,
    protocolNotes = protocolNotes
)

fun SyringeProtocolEntity.toPreset(): SyringePreset = SyringePreset(
    id = id,
    sortOrder = sortOrder,
    name = name,
    detail = detail,
    isWeightBased = isWeightBased,
    desiredDose = desiredDose,
    desiredUnit = enumValueOrDefault(desiredUnitName, MedicationUnit.MG),
    drugOnHand = drugOnHand,
    onHandUnit = enumValueOrDefault(onHandUnitName, MedicationUnit.MG),
    volumeOnHandMl = volumeOnHandMl,
    pcgSection = pcgSection,
    averageDoseSummary = averageDoseSummary,
    normalRangeSummary = normalRangeSummary,
    fixedMin = fixedMin,
    fixedMax = fixedMax,
    fixedUnit = fixedUnitName?.let { enumValueOrNull<MedicationUnit>(it) },
    weightMin = weightMin,
    weightMax = weightMax,
    weightUnit = weightUnitName?.let { enumValueOrNull<MedicationUnit>(it) },
    maxSingleDose = maxSingleDose,
    maxSingleUnit = maxSingleUnitName?.let { enumValueOrNull<MedicationUnit>(it) },
    protocolNotes = protocolNotes
)

fun SyringePreset.toEntity(sortOrderOverride: Int = sortOrder): SyringeProtocolEntity = SyringeProtocolEntity(
    id = id,
    sortOrder = sortOrderOverride,
    name = name,
    detail = detail,
    isWeightBased = isWeightBased,
    desiredDose = desiredDose,
    desiredUnitName = desiredUnit.name,
    drugOnHand = drugOnHand,
    onHandUnitName = onHandUnit.name,
    volumeOnHandMl = volumeOnHandMl,
    pcgSection = pcgSection,
    averageDoseSummary = averageDoseSummary,
    normalRangeSummary = normalRangeSummary,
    fixedMin = fixedMin,
    fixedMax = fixedMax,
    fixedUnitName = fixedUnit?.name,
    weightMin = weightMin,
    weightMax = weightMax,
    weightUnitName = weightUnit?.name,
    maxSingleDose = maxSingleDose,
    maxSingleUnitName = maxSingleUnit?.name,
    protocolNotes = protocolNotes
)

private inline fun <reified T : Enum<T>> enumValueOrDefault(name: String, default: T): T =
    enumValues<T>().firstOrNull { it.name == name } ?: default

private inline fun <reified T : Enum<T>> enumValueOrNull(name: String): T? =
    enumValues<T>().firstOrNull { it.name == name }
