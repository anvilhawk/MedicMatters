package com.example.ui

import androidx.lifecycle.ViewModel
import com.example.domain.DoseRateUnit
import com.example.domain.DripMode
import com.example.domain.DripPreset
import com.example.domain.DripResult
import com.example.domain.GcsEye
import com.example.domain.GcsMotor
import com.example.domain.GcsResult
import com.example.domain.GcsVerbal
import com.example.domain.HeightUnit
import com.example.domain.IbwResult
import com.example.domain.MedicMath
import com.example.domain.MedicTab
import com.example.domain.MedicationUnit
import com.example.domain.OxygenCylinder
import com.example.domain.OxygenResult
import com.example.domain.PatientSex
import com.example.domain.SyringePreset
import com.example.domain.SyringeResult
import com.example.domain.TimeUnit
import com.example.domain.WeightUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DripUiState(
    val mode: DripMode = DripMode.VOLUME_TIME,
    val volumeMl: String = "500",
    val timeValue: String = "4",
    val timeUnit: TimeUnit = TimeUnit.HOURS,
    val dropFactor: Int = 15,
    val customDropFactor: String = "",
    val isCustomDropFactor: Boolean = false,
    val weight: String = "80",
    val weightUnit: WeightUnit = WeightUnit.KG,
    val desiredDose: String = "5",
    val doseRateUnit: DoseRateUnit = DoseRateUnit.MCG_KG_MIN,
    val drugAmount: String = "400",
    val drugUnit: MedicationUnit = MedicationUnit.MG,
    val bagVolumeMl: String = "250",
    val metronomeEnabled: Boolean = false,
    val tapTimestampsMs: List<Long> = emptyList(),
    val measuredTapGttPerMin: Double? = null
) {
    val effectiveDropFactor: Int?
        get() = if (isCustomDropFactor) customDropFactor.toIntOrNull() else dropFactor

    val result: DripResult?
        get() = when (mode) {
            DripMode.VOLUME_TIME -> MedicMath.calculateVolumeDrip(
                volumeMl = volumeMl.toDoubleOrNull(),
                timeValue = timeValue.toDoubleOrNull(),
                timeUnit = timeUnit,
                dropFactorGttPerMl = effectiveDropFactor
            )
            DripMode.MED_INFUSION -> MedicMath.calculateDoseDrip(
                weight = weight.toDoubleOrNull(),
                weightUnit = weightUnit,
                desiredDose = desiredDose.toDoubleOrNull(),
                doseRateUnit = doseRateUnit,
                drugAmount = drugAmount.toDoubleOrNull(),
                drugUnit = drugUnit,
                bagVolumeMl = bagVolumeMl.toDoubleOrNull(),
                dropFactorGttPerMl = effectiveDropFactor
            )
        }
}

data class OxygenUiState(
    val cylinder: OxygenCylinder = OxygenCylinder.D,
    val currentPsi: String = "1500",
    val flowRateLpm: String = "15",
    val safeResidualPsi: Double = 200.0
) {
    val result: OxygenResult?
        get() = MedicMath.calculateOxygenDuration(
            cylinder = cylinder,
            currentPsi = currentPsi.toDoubleOrNull(),
            flowRateLpm = flowRateLpm.toDoubleOrNull(),
            safeResidualPsi = safeResidualPsi
        )
}

data class SyringeUiState(
    val isWeightBased: Boolean = false,
    val weight: String = "80",
    val weightUnit: WeightUnit = WeightUnit.KG,
    val desiredDose: String = "75",
    val desiredUnit: MedicationUnit = MedicationUnit.MCG,
    val drugOnHand: String = "100",
    val onHandUnit: MedicationUnit = MedicationUnit.MCG,
    val volumeOnHandMl: String = "2"
) {
    val result: SyringeResult?
        get() = MedicMath.calculateSyringeDose(
            desiredDose = desiredDose.toDoubleOrNull(),
            desiredUnit = desiredUnit,
            isWeightBased = isWeightBased,
            weight = weight.toDoubleOrNull(),
            weightUnit = weightUnit,
            drugOnHand = drugOnHand.toDoubleOrNull(),
            onHandUnit = onHandUnit,
            volumeOnHandMl = volumeOnHandMl.toDoubleOrNull()
        )
}

data class IbwUiState(
    val sex: PatientSex = PatientSex.MALE,
    val heightUnit: HeightUnit = HeightUnit.FT_IN,
    val feet: String = "5",
    val inches: String = "10",
    val cm: String = "178"
) {
    val result: IbwResult?
        get() = MedicMath.calculateIbw(
            sex = sex,
            heightUnit = heightUnit,
            feet = feet.toDoubleOrNull(),
            inches = inches.toDoubleOrNull(),
            cm = cm.toDoubleOrNull()
        )
}

data class GcsUiState(
    val eye: GcsEye? = GcsEye.SPONTANEOUS,
    val verbal: GcsVerbal? = GcsVerbal.ORIENTED,
    val motor: GcsMotor? = GcsMotor.OBEYS_COMMANDS
) {
    val result: GcsResult?
        get() = MedicMath.calculateGcs(eye = eye, verbal = verbal, motor = motor)
}

data class MedicCalcUiState(
    val activeTab: MedicTab = MedicTab.DRIP,
    val isNightMode: Boolean = true,
    val showFormulaDialog: Boolean = false,
    val drip: DripUiState = DripUiState(),
    val oxygen: OxygenUiState = OxygenUiState(),
    val syringe: SyringeUiState = SyringeUiState(),
    val ibw: IbwUiState = IbwUiState(),
    val gcs: GcsUiState = GcsUiState()
)

class MedicCalcViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MedicCalcUiState())
    val uiState: StateFlow<MedicCalcUiState> = _uiState.asStateFlow()

    fun selectTab(tab: MedicTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun toggleNightMode() {
        _uiState.update { it.copy(isNightMode = !it.isNightMode) }
    }

    fun setShowFormulaDialog(show: Boolean) {
        _uiState.update { it.copy(showFormulaDialog = show) }
    }

    // --- Drip Actions ---
    fun setDripMode(mode: DripMode) {
        _uiState.update { it.copy(drip = it.drip.copy(mode = mode)) }
    }

    fun setDripVolume(valStr: String) {
        val clean = sanitizeDecimal(valStr)
        _uiState.update { it.copy(drip = it.drip.copy(volumeMl = clean)) }
    }

    fun setDripTime(valStr: String) {
        val clean = sanitizeDecimal(valStr)
        _uiState.update { it.copy(drip = it.drip.copy(timeValue = clean)) }
    }

    fun setDripTimeUnit(unit: TimeUnit) {
        _uiState.update { it.copy(drip = it.drip.copy(timeUnit = unit)) }
    }

    fun setDropFactor(factor: Int) {
        _uiState.update {
            it.copy(drip = it.drip.copy(dropFactor = factor, isCustomDropFactor = false))
        }
    }

    fun enableCustomDropFactor() {
        _uiState.update {
            it.copy(drip = it.drip.copy(isCustomDropFactor = true))
        }
    }

    fun setCustomDropFactor(valStr: String) {
        val clean = valStr.filter { it.isDigit() }.take(3)
        _uiState.update {
            it.copy(drip = it.drip.copy(customDropFactor = clean, isCustomDropFactor = true))
        }
    }

    fun setDripWeight(valStr: String) {
        val clean = sanitizeDecimal(valStr)
        _uiState.update { it.copy(drip = it.drip.copy(weight = clean)) }
    }

    fun setDripWeightUnit(unit: WeightUnit) {
        _uiState.update { it.copy(drip = it.drip.copy(weightUnit = unit)) }
    }

    fun setDripDesiredDose(valStr: String) {
        val clean = sanitizeDecimal(valStr)
        _uiState.update { it.copy(drip = it.drip.copy(desiredDose = clean)) }
    }

    fun setDripDoseRateUnit(unit: DoseRateUnit) {
        _uiState.update { it.copy(drip = it.drip.copy(doseRateUnit = unit)) }
    }

    fun setDripDrugAmount(valStr: String) {
        val clean = sanitizeDecimal(valStr)
        _uiState.update { it.copy(drip = it.drip.copy(drugAmount = clean)) }
    }

    fun setDripDrugUnit(unit: MedicationUnit) {
        _uiState.update { it.copy(drip = it.drip.copy(drugUnit = unit)) }
    }

    fun setDripBagVolume(valStr: String) {
        val clean = sanitizeDecimal(valStr)
        _uiState.update { it.copy(drip = it.drip.copy(bagVolumeMl = clean)) }
    }

    fun toggleDripMetronome() {
        _uiState.update {
            it.copy(drip = it.drip.copy(metronomeEnabled = !it.drip.metronomeEnabled))
        }
    }

    fun recordDripTap(nowMs: Long = System.currentTimeMillis()) {
        _uiState.update { state ->
            val prev = state.drip.tapTimestampsMs
            val activeHistory = if (prev.isNotEmpty() && (nowMs - prev.last()) > 6000L) {
                listOf(nowMs)
            } else {
                (prev + nowMs).takeLast(6)
            }
            val measured = if (activeHistory.size >= 2) {
                val spanMs = activeHistory.last() - activeHistory.first()
                if (spanMs > 0L) {
                    val intervals = activeHistory.size - 1
                    (intervals * 60_000.0) / spanMs.toDouble()
                } else null
            } else null

            state.copy(
                drip = state.drip.copy(
                    tapTimestampsMs = activeHistory,
                    measuredTapGttPerMin = measured
                )
            )
        }
    }

    fun resetDripTap() {
        _uiState.update {
            it.copy(drip = it.drip.copy(tapTimestampsMs = emptyList(), measuredTapGttPerMin = null))
        }
    }

    fun applyDripPreset(preset: DripPreset) {
        _uiState.update { state ->
            if (preset.mode == DripMode.VOLUME_TIME) {
                state.copy(
                    drip = state.drip.copy(
                        mode = DripMode.VOLUME_TIME,
                        volumeMl = preset.bagVolumeMl,
                        timeValue = preset.timeValue,
                        timeUnit = preset.timeUnit,
                        dropFactor = preset.dropFactor,
                        isCustomDropFactor = false
                    )
                )
            } else {
                state.copy(
                    drip = state.drip.copy(
                        mode = DripMode.MED_INFUSION,
                        desiredDose = preset.doseValue,
                        doseRateUnit = preset.doseRateUnit,
                        drugAmount = preset.drugAmount,
                        drugUnit = preset.drugUnit,
                        bagVolumeMl = preset.bagVolumeMl,
                        dropFactor = preset.dropFactor,
                        isCustomDropFactor = false
                    )
                )
            }
        }
    }

    // --- Oxygen Actions ---
    fun setOxygenCylinder(cylinder: OxygenCylinder) {
        _uiState.update { it.copy(oxygen = it.oxygen.copy(cylinder = cylinder)) }
    }

    fun setOxygenPsi(valStr: String) {
        val clean = sanitizeDecimal(valStr)
        _uiState.update { it.copy(oxygen = it.oxygen.copy(currentPsi = clean)) }
    }

    fun setOxygenPsiSlider(psi: Int) {
        _uiState.update { it.copy(oxygen = it.oxygen.copy(currentPsi = psi.toString())) }
    }

    fun setOxygenFlowRate(valStr: String) {
        val clean = sanitizeDecimal(valStr)
        _uiState.update { it.copy(oxygen = it.oxygen.copy(flowRateLpm = clean)) }
    }

    fun toggleOxygenSafeResidual() {
        _uiState.update { state ->
            val next = if (state.oxygen.safeResidualPsi > 0.0) 0.0 else 200.0
            state.copy(oxygen = state.oxygen.copy(safeResidualPsi = next))
        }
    }

    // --- Syringe Actions ---
    fun setSyringeWeightBased(enabled: Boolean) {
        _uiState.update { it.copy(syringe = it.syringe.copy(isWeightBased = enabled)) }
    }

    fun setSyringeWeight(valStr: String) {
        val clean = sanitizeDecimal(valStr)
        _uiState.update { it.copy(syringe = it.syringe.copy(weight = clean)) }
    }

    fun setSyringeWeightUnit(unit: WeightUnit) {
        _uiState.update { it.copy(syringe = it.syringe.copy(weightUnit = unit)) }
    }

    fun setSyringeDesiredDose(valStr: String) {
        val clean = sanitizeDecimal(valStr)
        _uiState.update { it.copy(syringe = it.syringe.copy(desiredDose = clean)) }
    }

    fun setSyringeDesiredUnit(unit: MedicationUnit) {
        _uiState.update { it.copy(syringe = it.syringe.copy(desiredUnit = unit)) }
    }

    fun setSyringeDrugOnHand(valStr: String) {
        val clean = sanitizeDecimal(valStr)
        _uiState.update { it.copy(syringe = it.syringe.copy(drugOnHand = clean)) }
    }

    fun setSyringeOnHandUnit(unit: MedicationUnit) {
        _uiState.update { it.copy(syringe = it.syringe.copy(onHandUnit = unit)) }
    }

    fun setSyringeVolumeOnHand(valStr: String) {
        val clean = sanitizeDecimal(valStr)
        _uiState.update { it.copy(syringe = it.syringe.copy(volumeOnHandMl = clean)) }
    }

    fun applySyringePreset(preset: SyringePreset) {
        _uiState.update { state ->
            state.copy(
                syringe = state.syringe.copy(
                    isWeightBased = preset.isWeightBased,
                    desiredDose = preset.desiredDose,
                    desiredUnit = preset.desiredUnit,
                    drugOnHand = preset.drugOnHand,
                    onHandUnit = preset.onHandUnit,
                    volumeOnHandMl = preset.volumeOnHandMl
                )
            )
        }
    }

    // --- IBW Actions ---
    fun setIbwSex(sex: PatientSex) {
        _uiState.update { it.copy(ibw = it.ibw.copy(sex = sex)) }
    }

    fun setIbwHeightUnit(unit: HeightUnit) {
        _uiState.update { state ->
            val currentIbw = state.ibw
            if (currentIbw.heightUnit == unit) return@update state
            if (unit == HeightUnit.CM) {
                val ft = currentIbw.feet.toDoubleOrNull() ?: 0.0
                val inc = currentIbw.inches.toDoubleOrNull() ?: 0.0
                val totalIn = ft * 12.0 + inc
                val convertedCm = if (totalIn > 0) MedicMath.formatNumber(totalIn * 2.54, 0) else currentIbw.cm
                state.copy(ibw = currentIbw.copy(heightUnit = unit, cm = convertedCm))
            } else {
                val cmVal = currentIbw.cm.toDoubleOrNull() ?: 0.0
                if (cmVal > 0) {
                    val totalIn = (cmVal / 2.54).toInt()
                    val ft = totalIn / 12
                    val remIn = totalIn % 12
                    state.copy(
                        ibw = currentIbw.copy(
                            heightUnit = unit,
                            feet = ft.toString(),
                            inches = remIn.toString()
                        )
                    )
                } else {
                    state.copy(ibw = currentIbw.copy(heightUnit = unit))
                }
            }
        }
    }

    fun setIbwFeet(valStr: String) {
        val clean = valStr.filter { it.isDigit() }.take(1)
        _uiState.update { it.copy(ibw = it.ibw.copy(feet = clean)) }
    }

    fun setIbwInches(valStr: String) {
        val clean = sanitizeDecimal(valStr).take(4)
        _uiState.update { it.copy(ibw = it.ibw.copy(inches = clean)) }
    }

    fun setIbwCm(valStr: String) {
        val clean = sanitizeDecimal(valStr).take(5)
        _uiState.update { it.copy(ibw = it.ibw.copy(cm = clean)) }
    }

    fun adjustIbwHeightInches(deltaInches: Int) {
        _uiState.update { state ->
            val current = state.ibw
            if (current.heightUnit == HeightUnit.FT_IN) {
                val ft = current.feet.toIntOrNull() ?: 5
                val inc = current.inches.toIntOrNull() ?: 0
                val total = (ft * 12 + inc + deltaInches).coerceIn(36, 96)
                state.copy(
                    ibw = current.copy(
                        feet = (total / 12).toString(),
                        inches = (total % 12).toString()
                    )
                )
            } else {
                val cmVal = (current.cm.toDoubleOrNull() ?: 170.0) + (deltaInches * 2.54)
                val clamped = cmVal.coerceIn(90.0, 245.0)
                state.copy(ibw = current.copy(cm = MedicMath.formatNumber(clamped, 0)))
            }
        }
    }

    fun applyIbwToCalculators(ibwKg: Double) {
        val formatted = MedicMath.formatNumber(ibwKg, 1)
        _uiState.update { state ->
            state.copy(
                drip = state.drip.copy(weight = formatted, weightUnit = WeightUnit.KG),
                syringe = state.syringe.copy(weight = formatted, weightUnit = WeightUnit.KG)
            )
        }
    }

    // --- GCS Actions ---
    fun setGcsEye(eye: GcsEye) {
        _uiState.update { it.copy(gcs = it.gcs.copy(eye = eye)) }
    }

    fun setGcsVerbal(verbal: GcsVerbal) {
        _uiState.update { it.copy(gcs = it.gcs.copy(verbal = verbal)) }
    }

    fun setGcsMotor(motor: GcsMotor) {
        _uiState.update { it.copy(gcs = it.gcs.copy(motor = motor)) }
    }

    fun resetGcsNormal() {
        _uiState.update {
            it.copy(
                gcs = GcsUiState(
                    eye = GcsEye.SPONTANEOUS,
                    verbal = GcsVerbal.ORIENTED,
                    motor = GcsMotor.OBEYS_COMMANDS
                )
            )
        }
    }

    fun clearActiveTab() {
        _uiState.update { state ->
            when (state.activeTab) {
                MedicTab.DRIP -> state.copy(
                    drip = state.drip.copy(
                        volumeMl = "",
                        timeValue = "",
                        desiredDose = "",
                        drugAmount = "",
                        bagVolumeMl = "",
                        metronomeEnabled = false,
                        tapTimestampsMs = emptyList(),
                        measuredTapGttPerMin = null
                    )
                )
                MedicTab.OXYGEN -> state.copy(
                    oxygen = state.oxygen.copy(currentPsi = "", flowRateLpm = "")
                )
                MedicTab.SYRINGE -> state.copy(
                    syringe = state.syringe.copy(
                        desiredDose = "",
                        drugOnHand = "",
                        volumeOnHandMl = ""
                    )
                )
                MedicTab.IBW -> state.copy(
                    ibw = state.ibw.copy(feet = "", inches = "", cm = "")
                )
                MedicTab.GCS -> state.copy(
                    gcs = state.gcs.copy(eye = null, verbal = null, motor = null)
                )
            }
        }
    }

    private fun sanitizeDecimal(input: String): String {
        val filtered = input.filter { it.isDigit() || it == '.' }
        val firstDot = filtered.indexOf('.')
        return if (firstDot == -1) {
            filtered.take(7)
        } else {
            val before = filtered.substring(0, firstDot + 1)
            val after = filtered.substring(firstDot + 1).replace(".", "")
            (before + after).take(8)
        }
    }
}
