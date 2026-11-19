package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ProtocolDatabase
import com.example.data.ProtocolRepository
import com.example.domain.AbgMath
import com.example.domain.AbgPreset
import com.example.domain.AbgResult
import com.example.domain.AbgSampleType
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
import com.example.domain.ProtocolDoseValidation
import com.example.domain.SyringePreset
import com.example.domain.SyringeResult
import com.example.domain.TimeUnit
import com.example.domain.VentMath
import com.example.domain.VentMechanicsResult
import com.example.domain.VentMechanicsScenario
import com.example.domain.VentSetupResult
import com.example.domain.VentStrategy
import com.example.domain.VentSubMode
import com.example.domain.WeightUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val DEFAULT_DRIP_PRESETS: List<DripPreset> =
    MedicMath.DRIP_PRESETS.mapIndexed { index, preset ->
        preset.copy(id = index + 1L, sortOrder = index)
    }

private val DEFAULT_SYRINGE_PRESETS: List<SyringePreset> =
    MedicMath.SYRINGE_PRESETS.mapIndexed { index, preset ->
        preset.copy(id = index + 1L, sortOrder = index)
    }

data class DripUiState(
    val presets: List<DripPreset> = DEFAULT_DRIP_PRESETS,
    val selectedPresetId: Long? = null,
    val selectedPresetName: String? = "Generic",
    val mode: DripMode = DripMode.MED_INFUSION,
    val volumeMl: String = "500",
    val timeValue: String = "4",
    val timeUnit: TimeUnit = TimeUnit.HOURS,
    val dropFactor: Int = 60,
    val customDropFactor: String = "",
    val isCustomDropFactor: Boolean = false,
    val weight: String = "80",
    val weightUnit: WeightUnit = WeightUnit.KG,
    val desiredDose: String = "5",
    val doseRateUnit: DoseRateUnit = DoseRateUnit.MCG_KG_MIN,
    val isConcentrationMode: Boolean = true,
    val concentrationPerMl: String = "1",
    val concentrationUnit: MedicationUnit = MedicationUnit.MG,
    val drugAmount: String = "250",
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

    val selectedPreset: DripPreset?
        get() = presets.firstOrNull { selectedPresetId != null && it.id == selectedPresetId }
            ?: presets.firstOrNull { selectedPresetName != null && it.name == selectedPresetName }

    val protocolValidation: ProtocolDoseValidation?
        get() = selectedPreset?.let { preset ->
            MedicMath.validateDripDose(
                preset = preset,
                desiredDose = desiredDose.toDoubleOrNull(),
                doseRateUnit = doseRateUnit,
                mode = mode,
                bagVolumeMl = bagVolumeMl.toDoubleOrNull(),
                timeValue = timeValue.toDoubleOrNull(),
                timeUnit = timeUnit
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
    val presets: List<SyringePreset> = DEFAULT_SYRINGE_PRESETS,
    val selectedPresetId: Long? = null,
    val selectedPresetName: String? = "Generic",
    val isWeightBased: Boolean = false,
    val weight: String = "80",
    val weightUnit: WeightUnit = WeightUnit.KG,
    val desiredDose: String = "75",
    val desiredUnit: MedicationUnit = MedicationUnit.MCG,
    val isConcentrationMode: Boolean = true,
    val concentrationPerMl: String = "50",
    val concentrationUnit: MedicationUnit = MedicationUnit.MCG,
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

    val selectedPreset: SyringePreset?
        get() = presets.firstOrNull { selectedPresetId != null && it.id == selectedPresetId }
            ?: presets.firstOrNull { selectedPresetName != null && it.name == selectedPresetName }

    val protocolValidation: ProtocolDoseValidation?
        get() = selectedPreset?.let { preset ->
            MedicMath.validateSyringeDose(
                preset = preset,
                desiredDose = desiredDose.toDoubleOrNull(),
                desiredUnit = desiredUnit,
                isWeightBased = isWeightBased,
                weight = weight.toDoubleOrNull(),
                weightUnit = weightUnit
            )
        }
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

data class AbgUiState(
    val sampleType: AbgSampleType = AbgSampleType.ARTERIAL,
    val selectedPresetName: String? = "Normal",
    val ph: String = "7.40",
    val paco2: String = "40",
    val hco3: String = "24",
    val pao2: String = "95",
    val fio2Percent: String = "21",
    val na: String = "140",
    val cl: String = "104"
) {
    val result: AbgResult?
        get() = AbgMath.analyze(
            sampleType = sampleType,
            ph = ph.toDoubleOrNull(),
            paco2 = paco2.toDoubleOrNull(),
            hco3 = hco3.toDoubleOrNull(),
            pao2 = pao2.toDoubleOrNull(),
            fio2Percent = fio2Percent.toDoubleOrNull(),
            na = na.toDoubleOrNull(),
            cl = cl.toDoubleOrNull()
        )
}

data class VentUiState(
    val subMode: VentSubMode = VentSubMode.SETUP,
    val strategy: VentStrategy = VentStrategy.NORMAL,
    val useDirectWeight: Boolean = false,
    val sex: PatientSex = PatientSex.MALE,
    val heightUnit: HeightUnit = HeightUnit.FT_IN,
    val feet: String = "5",
    val inches: String = "10",
    val cm: String = "178",
    val directWeightKg: String = "70",
    val targetVtMlPerKg: String = "7",
    val selectedScenarioName: String? = "Normal",
    val cstat: String = "55",
    val rinsp: String = "10",
    val rcexp: String = "0.7",
    val ppeak: String = "22",
    val pplat: String = "18",
    val peep: String = "5",
    val vte: String = "480",
    val autoPeep: String = "0",
    val vLeakPercent: String = "2",
    val rr: String = "14",
    val tiSec: String = "1.0",
    val p01: String = "-1.5"
) {
    val computedIbw: IbwResult?
        get() = MedicMath.calculateIbw(
            sex = sex,
            heightUnit = heightUnit,
            feet = feet.toDoubleOrNull(),
            inches = inches.toDoubleOrNull(),
            cm = cm.toDoubleOrNull()
        )

    val effectiveWeightKg: Double?
        get() = if (useDirectWeight) {
            directWeightKg.toDoubleOrNull()
        } else {
            computedIbw?.ibwKg
        }

    val weightSourceLabel: String
        get() = if (useDirectWeight) {
            "Direct / Actual Weight"
        } else {
            "Devine IBW (${sex.label})"
        }

    val setupResult: VentSetupResult?
        get() = VentMath.calculateSetup(
            strategy = strategy,
            weightKg = effectiveWeightKg,
            weightSourceLabel = weightSourceLabel,
            customVtMlPerKg = targetVtMlPerKg.toDoubleOrNull()
        )

    val mechanicsResult: VentMechanicsResult
        get() = VentMath.analyzeMechanics(
            cstat = cstat.toDoubleOrNull(),
            rinsp = rinsp.toDoubleOrNull(),
            rcexp = rcexp.toDoubleOrNull(),
            ppeak = ppeak.toDoubleOrNull(),
            pplat = pplat.toDoubleOrNull(),
            peep = peep.toDoubleOrNull(),
            vte = vte.toDoubleOrNull(),
            autoPeep = autoPeep.toDoubleOrNull(),
            vLeakPercent = vLeakPercent.toDoubleOrNull(),
            rr = rr.toDoubleOrNull(),
            tiSec = tiSec.toDoubleOrNull(),
            p01 = p01.toDoubleOrNull()
        )
}

data class MedicCalcUiState(
    val activeTab: MedicTab = MedicTab.DRIP,
    val isNightMode: Boolean = true,
    val showFormulaDialog: Boolean = false,
    val showSettings: Boolean = false,
    val drip: DripUiState = DripUiState(),
    val oxygen: OxygenUiState = OxygenUiState(),
    val syringe: SyringeUiState = SyringeUiState(),
    val ibw: IbwUiState = IbwUiState(),
    val gcs: GcsUiState = GcsUiState(),
    val abg: AbgUiState = AbgUiState(),
    val vent: VentUiState = VentUiState()
)

class MedicCalcViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProtocolRepository(
        context = application,
        dao = ProtocolDatabase.getInstance(application).protocolDao()
    )

    private val _uiState = MutableStateFlow(MedicCalcUiState())
    val uiState: StateFlow<MedicCalcUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.ensureSeeded()
        }
        viewModelScope.launch {
            repository.dripProtocols.collect { list ->
                _uiState.update { state ->
                    val currentId = state.drip.selectedPresetId
                    val currentName = state.drip.selectedPresetName
                    val matched = list.firstOrNull { currentId != null && it.id == currentId }
                        ?: list.firstOrNull { currentName != null && it.name == currentName }
                    state.copy(
                        drip = state.drip.copy(
                            presets = list,
                            selectedPresetId = matched?.id,
                            selectedPresetName = matched?.name
                        )
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.syringeProtocols.collect { list ->
                _uiState.update { state ->
                    val currentId = state.syringe.selectedPresetId
                    val currentName = state.syringe.selectedPresetName
                    val matched = list.firstOrNull { currentId != null && it.id == currentId }
                        ?: list.firstOrNull { currentName != null && it.name == currentName }
                    state.copy(
                        syringe = state.syringe.copy(
                            presets = list,
                            selectedPresetId = matched?.id,
                            selectedPresetName = matched?.name
                        )
                    )
                }
            }
        }
    }

    fun selectTab(tab: MedicTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun toggleNightMode() {
        _uiState.update { it.copy(isNightMode = !it.isNightMode) }
    }

    fun setShowFormulaDialog(show: Boolean) {
        _uiState.update { it.copy(showFormulaDialog = show) }
    }

    fun setShowSettings(show: Boolean) {
        _uiState.update { it.copy(showSettings = show) }
    }

    // --- Protocol Settings CRUD ---
    fun saveDripProtocol(preset: DripPreset) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveDripProtocol(preset)
        }
    }

    fun deleteDripProtocol(id: Long) {
        _uiState.update { state ->
            if (state.drip.selectedPresetId == id) {
                state.copy(
                    drip = state.drip.copy(
                        selectedPresetId = null,
                        selectedPresetName = null
                    )
                )
            } else state
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDripProtocol(id)
        }
    }

    fun saveSyringeProtocol(preset: SyringePreset) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSyringeProtocol(preset)
        }
    }

    fun deleteSyringeProtocol(id: Long) {
        _uiState.update { state ->
            if (state.syringe.selectedPresetId == id) {
                state.copy(
                    syringe = state.syringe.copy(
                        selectedPresetId = null,
                        selectedPresetName = null
                    )
                )
            } else state
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSyringeProtocol(id)
        }
    }

    fun resetProtocolsToDefaults() {
        _uiState.update { state ->
            state.copy(
                drip = state.drip.copy(selectedPresetId = null, selectedPresetName = "Generic"),
                syringe = state.syringe.copy(selectedPresetId = null, selectedPresetName = "Generic")
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.resetToDefaults()
        }
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

    fun setDripConcentrationMode(enabled: Boolean) {
        _uiState.update { state ->
            val current = state.drip
            if (current.isConcentrationMode == enabled) return@update state
            if (enabled) {
                val computedConc = computeConcentrationString(current.drugAmount, current.bagVolumeMl)
                state.copy(
                    drip = current.copy(
                        isConcentrationMode = true,
                        concentrationPerMl = computedConc.ifEmpty { current.concentrationPerMl },
                        concentrationUnit = current.drugUnit
                    )
                )
            } else {
                state.copy(drip = current.copy(isConcentrationMode = false))
            }
        }
    }

    fun setDripConcentrationPerMl(valStr: String) {
        val clean = sanitizeDecimal(valStr)
        _uiState.update { state ->
            val current = state.drip
            val updatedTotal = computeTotalDrugString(clean, current.bagVolumeMl)
            state.copy(
                drip = current.copy(
                    concentrationPerMl = clean,
                    drugAmount = updatedTotal,
                    drugUnit = current.concentrationUnit
                )
            )
        }
    }

    fun setDripConcentrationUnit(unit: MedicationUnit) {
        _uiState.update { state ->
            val current = state.drip
            val updatedTotal = computeTotalDrugString(current.concentrationPerMl, current.bagVolumeMl)
            state.copy(
                drip = current.copy(
                    concentrationUnit = unit,
                    drugUnit = unit,
                    drugAmount = updatedTotal
                )
            )
        }
    }

    fun selectDripQuickConcentration(amountPerMl: String, unit: MedicationUnit) {
        _uiState.update { state ->
            val current = state.drip
            val updatedTotal = computeTotalDrugString(amountPerMl, current.bagVolumeMl)
            state.copy(
                drip = current.copy(
                    isConcentrationMode = true,
                    concentrationPerMl = amountPerMl,
                    concentrationUnit = unit,
                    drugUnit = unit,
                    drugAmount = updatedTotal
                )
            )
        }
    }

    fun setDripDrugAmount(valStr: String) {
        val clean = sanitizeDecimal(valStr)
        _uiState.update { state ->
            val current = state.drip
            val conc = computeConcentrationString(clean, current.bagVolumeMl)
            state.copy(
                drip = current.copy(
                    drugAmount = clean,
                    concentrationPerMl = if (conc.isNotEmpty()) conc else current.concentrationPerMl
                )
            )
        }
    }

    fun setDripDrugUnit(unit: MedicationUnit) {
        _uiState.update {
            it.copy(drip = it.drip.copy(drugUnit = unit, concentrationUnit = unit))
        }
    }

    fun setDripBagVolume(valStr: String) {
        val clean = sanitizeDecimal(valStr)
        _uiState.update { state ->
            val current = state.drip
            val updatedDrugAmount = if (current.isConcentrationMode) {
                computeTotalDrugString(current.concentrationPerMl, clean)
            } else {
                current.drugAmount
            }
            val updatedConc = if (!current.isConcentrationMode) {
                computeConcentrationString(current.drugAmount, clean).ifEmpty { current.concentrationPerMl }
            } else {
                current.concentrationPerMl
            }
            state.copy(
                drip = current.copy(
                    bagVolumeMl = clean,
                    drugAmount = updatedDrugAmount,
                    concentrationPerMl = updatedConc
                )
            )
        }
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
            val resolvedId = preset.id.takeIf { it > 0L }
            val isGeneric = preset.name.equals("Generic", ignoreCase = true)
            val computedConc = computeConcentrationString(preset.drugAmount, preset.bagVolumeMl)
            if (preset.mode == DripMode.VOLUME_TIME) {
                state.copy(
                    drip = state.drip.copy(
                        selectedPresetId = resolvedId,
                        selectedPresetName = preset.name,
                        mode = DripMode.VOLUME_TIME,
                        volumeMl = preset.bagVolumeMl,
                        timeValue = preset.timeValue,
                        timeUnit = preset.timeUnit,
                        dropFactor = preset.dropFactor,
                        isCustomDropFactor = false,
                        isConcentrationMode = isGeneric,
                        concentrationPerMl = computedConc.ifEmpty { state.drip.concentrationPerMl },
                        concentrationUnit = preset.drugUnit
                    )
                )
            } else {
                state.copy(
                    drip = state.drip.copy(
                        selectedPresetId = resolvedId,
                        selectedPresetName = preset.name,
                        mode = DripMode.MED_INFUSION,
                        desiredDose = preset.doseValue,
                        doseRateUnit = preset.doseRateUnit,
                        isConcentrationMode = isGeneric,
                        concentrationPerMl = computedConc.ifEmpty { "1" },
                        concentrationUnit = preset.drugUnit,
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

    fun clearDripPresetSelection() {
        _uiState.update {
            it.copy(drip = it.drip.copy(selectedPresetId = null, selectedPresetName = null))
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

    fun setSyringeConcentrationMode(enabled: Boolean) {
        _uiState.update { state ->
            val current = state.syringe
            if (current.isConcentrationMode == enabled) return@update state
            if (enabled) {
                val computedConc = computeConcentrationString(current.drugOnHand, current.volumeOnHandMl)
                state.copy(
                    syringe = current.copy(
                        isConcentrationMode = true,
                        concentrationPerMl = computedConc.ifEmpty { current.concentrationPerMl },
                        concentrationUnit = current.onHandUnit
                    )
                )
            } else {
                state.copy(syringe = current.copy(isConcentrationMode = false))
            }
        }
    }

    fun setSyringeConcentrationPerMl(valStr: String) {
        val clean = sanitizeDecimal(valStr)
        _uiState.update { state ->
            val current = state.syringe
            val updatedTotal = computeTotalDrugString(clean, current.volumeOnHandMl)
            state.copy(
                syringe = current.copy(
                    concentrationPerMl = clean,
                    drugOnHand = updatedTotal,
                    onHandUnit = current.concentrationUnit
                )
            )
        }
    }

    fun setSyringeConcentrationUnit(unit: MedicationUnit) {
        _uiState.update { state ->
            val current = state.syringe
            val updatedTotal = computeTotalDrugString(current.concentrationPerMl, current.volumeOnHandMl)
            state.copy(
                syringe = current.copy(
                    concentrationUnit = unit,
                    onHandUnit = unit,
                    drugOnHand = updatedTotal
                )
            )
        }
    }

    fun selectSyringeQuickConcentration(amountPerMl: String, unit: MedicationUnit) {
        _uiState.update { state ->
            val current = state.syringe
            val updatedTotal = computeTotalDrugString(amountPerMl, current.volumeOnHandMl)
            state.copy(
                syringe = current.copy(
                    isConcentrationMode = true,
                    concentrationPerMl = amountPerMl,
                    concentrationUnit = unit,
                    onHandUnit = unit,
                    drugOnHand = updatedTotal
                )
            )
        }
    }

    fun setSyringeDrugOnHand(valStr: String) {
        val clean = sanitizeDecimal(valStr)
        _uiState.update { state ->
            val current = state.syringe
            val conc = computeConcentrationString(clean, current.volumeOnHandMl)
            state.copy(
                syringe = current.copy(
                    drugOnHand = clean,
                    concentrationPerMl = if (conc.isNotEmpty()) conc else current.concentrationPerMl
                )
            )
        }
    }

    fun setSyringeOnHandUnit(unit: MedicationUnit) {
        _uiState.update {
            it.copy(syringe = it.syringe.copy(onHandUnit = unit, concentrationUnit = unit))
        }
    }

    fun setSyringeVolumeOnHand(valStr: String) {
        val clean = sanitizeDecimal(valStr)
        _uiState.update { state ->
            val current = state.syringe
            val updatedDrugOnHand = if (current.isConcentrationMode) {
                computeTotalDrugString(current.concentrationPerMl, clean)
            } else {
                current.drugOnHand
            }
            val updatedConc = if (!current.isConcentrationMode) {
                computeConcentrationString(current.drugOnHand, clean).ifEmpty { current.concentrationPerMl }
            } else {
                current.concentrationPerMl
            }
            state.copy(
                syringe = current.copy(
                    volumeOnHandMl = clean,
                    drugOnHand = updatedDrugOnHand,
                    concentrationPerMl = updatedConc
                )
            )
        }
    }

    fun applySyringePreset(preset: SyringePreset) {
        _uiState.update { state ->
            val isGeneric = preset.name.equals("Generic", ignoreCase = true)
            val computedConc = computeConcentrationString(preset.drugOnHand, preset.volumeOnHandMl)
            state.copy(
                syringe = state.syringe.copy(
                    selectedPresetId = preset.id.takeIf { it > 0L },
                    selectedPresetName = preset.name,
                    isWeightBased = preset.isWeightBased,
                    desiredDose = preset.desiredDose,
                    desiredUnit = preset.desiredUnit,
                    isConcentrationMode = isGeneric,
                    concentrationPerMl = computedConc.ifEmpty { "50" },
                    concentrationUnit = preset.onHandUnit,
                    drugOnHand = preset.drugOnHand,
                    onHandUnit = preset.onHandUnit,
                    volumeOnHandMl = preset.volumeOnHandMl
                )
            )
        }
    }

    fun clearSyringePresetSelection() {
        _uiState.update {
            it.copy(syringe = it.syringe.copy(selectedPresetId = null, selectedPresetName = null))
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

    // --- ABG Actions ---
    fun setAbgSampleType(sampleType: AbgSampleType) {
        _uiState.update { it.copy(abg = it.abg.copy(sampleType = sampleType)) }
    }

    fun applyAbgPreset(preset: AbgPreset) {
        _uiState.update {
            it.copy(
                abg = it.abg.copy(
                    sampleType = preset.sampleType,
                    selectedPresetName = preset.name,
                    ph = preset.ph,
                    paco2 = preset.paco2,
                    hco3 = preset.hco3,
                    pao2 = preset.pao2,
                    fio2Percent = preset.fio2Percent,
                    na = preset.na,
                    cl = preset.cl
                )
            )
        }
    }

    fun setAbgPh(valStr: String) {
        val clean = sanitizeDecimal(valStr).take(5)
        _uiState.update { it.copy(abg = it.abg.copy(ph = clean, selectedPresetName = null)) }
    }

    fun setAbgPaco2(valStr: String) {
        val clean = sanitizeDecimal(valStr).take(5)
        _uiState.update { it.copy(abg = it.abg.copy(paco2 = clean, selectedPresetName = null)) }
    }

    fun setAbgHco3(valStr: String) {
        val clean = sanitizeDecimal(valStr).take(5)
        _uiState.update { it.copy(abg = it.abg.copy(hco3 = clean, selectedPresetName = null)) }
    }

    fun setAbgPao2(valStr: String) {
        val clean = sanitizeDecimal(valStr).take(5)
        _uiState.update { it.copy(abg = it.abg.copy(pao2 = clean, selectedPresetName = null)) }
    }

    fun setAbgFio2(valStr: String) {
        val clean = sanitizeDecimal(valStr).take(5)
        _uiState.update { it.copy(abg = it.abg.copy(fio2Percent = clean, selectedPresetName = null)) }
    }

    fun setAbgNa(valStr: String) {
        val clean = sanitizeDecimal(valStr).take(5)
        _uiState.update { it.copy(abg = it.abg.copy(na = clean, selectedPresetName = null)) }
    }

    fun setAbgCl(valStr: String) {
        val clean = sanitizeDecimal(valStr).take(5)
        _uiState.update { it.copy(abg = it.abg.copy(cl = clean, selectedPresetName = null)) }
    }

    // --- Vent Actions ---
    fun setVentSubMode(subMode: VentSubMode) {
        _uiState.update { it.copy(vent = it.vent.copy(subMode = subMode)) }
    }

    fun setVentStrategy(strategy: VentStrategy) {
        _uiState.update { state ->
            val current = state.vent
            val useDirect = if (strategy == VentStrategy.PEDIATRIC) true else current.useDirectWeight
            val defaultDirectKg = if (strategy == VentStrategy.PEDIATRIC && (current.directWeightKg.toDoubleOrNull() ?: 70.0) > 45.0) {
                "15"
            } else {
                current.directWeightKg
            }
            state.copy(
                vent = current.copy(
                    strategy = strategy,
                    useDirectWeight = useDirect,
                    directWeightKg = defaultDirectKg,
                    targetVtMlPerKg = MedicMath.formatNumber(strategy.defaultVtMlPerKg, 0)
                )
            )
        }
    }

    fun setVentUseDirectWeight(useDirect: Boolean) {
        _uiState.update { it.copy(vent = it.vent.copy(useDirectWeight = useDirect)) }
    }

    fun setVentSex(sex: PatientSex) {
        _uiState.update { it.copy(vent = it.vent.copy(sex = sex)) }
    }

    fun setVentHeightUnit(unit: HeightUnit) {
        _uiState.update { state ->
            val v = state.vent
            if (v.heightUnit == unit) return@update state
            if (unit == HeightUnit.CM) {
                val totalIn = (v.feet.toDoubleOrNull() ?: 0.0) * 12.0 + (v.inches.toDoubleOrNull() ?: 0.0)
                val convertedCm = if (totalIn > 0) MedicMath.formatNumber(totalIn * 2.54, 0) else v.cm
                state.copy(vent = v.copy(heightUnit = unit, cm = convertedCm))
            } else {
                val cmVal = v.cm.toDoubleOrNull() ?: 0.0
                if (cmVal > 0) {
                    val totalIn = (cmVal / 2.54).toInt()
                    state.copy(
                        vent = v.copy(
                            heightUnit = unit,
                            feet = (totalIn / 12).toString(),
                            inches = (totalIn % 12).toString()
                        )
                    )
                } else {
                    state.copy(vent = v.copy(heightUnit = unit))
                }
            }
        }
    }

    fun setVentFeet(valStr: String) {
        val clean = valStr.filter { it.isDigit() }.take(1)
        _uiState.update { it.copy(vent = it.vent.copy(feet = clean)) }
    }

    fun setVentInches(valStr: String) {
        val clean = sanitizeDecimal(valStr).take(4)
        _uiState.update { it.copy(vent = it.vent.copy(inches = clean)) }
    }

    fun setVentCm(valStr: String) {
        val clean = sanitizeDecimal(valStr).take(5)
        _uiState.update { it.copy(vent = it.vent.copy(cm = clean)) }
    }

    fun setVentDirectWeightKg(valStr: String) {
        val clean = sanitizeDecimal(valStr).take(5)
        _uiState.update { it.copy(vent = it.vent.copy(directWeightKg = clean)) }
    }

    fun setVentTargetVtMlPerKg(valStr: String) {
        val clean = sanitizeDecimal(valStr).take(4)
        _uiState.update { it.copy(vent = it.vent.copy(targetVtMlPerKg = clean)) }
    }

    fun syncVentFromIbwTab() {
        _uiState.update { state ->
            val ibwState = state.ibw
            state.copy(
                vent = state.vent.copy(
                    useDirectWeight = false,
                    sex = ibwState.sex,
                    heightUnit = ibwState.heightUnit,
                    feet = ibwState.feet,
                    inches = ibwState.inches,
                    cm = ibwState.cm
                )
            )
        }
    }

    fun applyVentMechanicsScenario(scenario: VentMechanicsScenario) {
        _uiState.update {
            it.copy(
                vent = it.vent.copy(
                    selectedScenarioName = scenario.name,
                    cstat = scenario.cstat,
                    rinsp = scenario.rinsp,
                    rcexp = scenario.rcexp,
                    ppeak = scenario.ppeak,
                    pplat = scenario.pplat,
                    peep = scenario.peep,
                    vte = scenario.vte,
                    autoPeep = scenario.autoPeep,
                    vLeakPercent = scenario.vLeakPercent,
                    rr = scenario.rr,
                    tiSec = scenario.tiSec,
                    p01 = scenario.p01
                )
            )
        }
    }

    fun setVentCstat(valStr: String) {
        val clean = sanitizeDecimal(valStr).take(5)
        _uiState.update { it.copy(vent = it.vent.copy(cstat = clean, selectedScenarioName = null)) }
    }

    fun setVentRinsp(valStr: String) {
        val clean = sanitizeDecimal(valStr).take(5)
        _uiState.update { it.copy(vent = it.vent.copy(rinsp = clean, selectedScenarioName = null)) }
    }

    fun setVentRcexp(valStr: String) {
        val clean = sanitizeDecimal(valStr).take(5)
        _uiState.update { it.copy(vent = it.vent.copy(rcexp = clean, selectedScenarioName = null)) }
    }

    fun setVentPpeak(valStr: String) {
        val clean = sanitizeDecimal(valStr).take(5)
        _uiState.update { it.copy(vent = it.vent.copy(ppeak = clean, selectedScenarioName = null)) }
    }

    fun setVentPplat(valStr: String) {
        val clean = sanitizeDecimal(valStr).take(5)
        _uiState.update { it.copy(vent = it.vent.copy(pplat = clean, selectedScenarioName = null)) }
    }

    fun setVentPeep(valStr: String) {
        val clean = sanitizeDecimal(valStr).take(5)
        _uiState.update { it.copy(vent = it.vent.copy(peep = clean, selectedScenarioName = null)) }
    }

    fun setVentVte(valStr: String) {
        val clean = sanitizeDecimal(valStr).take(5)
        _uiState.update { it.copy(vent = it.vent.copy(vte = clean, selectedScenarioName = null)) }
    }

    fun setVentAutoPeep(valStr: String) {
        val clean = sanitizeDecimal(valStr).take(5)
        _uiState.update { it.copy(vent = it.vent.copy(autoPeep = clean, selectedScenarioName = null)) }
    }

    fun setVentVLeak(valStr: String) {
        val clean = sanitizeDecimal(valStr).take(5)
        _uiState.update { it.copy(vent = it.vent.copy(vLeakPercent = clean, selectedScenarioName = null)) }
    }

    fun setVentRr(valStr: String) {
        val clean = sanitizeDecimal(valStr).take(4)
        _uiState.update { it.copy(vent = it.vent.copy(rr = clean, selectedScenarioName = null)) }
    }

    fun setVentTiSec(valStr: String) {
        val clean = sanitizeDecimal(valStr).take(4)
        _uiState.update { it.copy(vent = it.vent.copy(tiSec = clean, selectedScenarioName = null)) }
    }

    fun setVentP01(valStr: String) {
        val clean = sanitizeSignedDecimal(valStr).take(5)
        _uiState.update { it.copy(vent = it.vent.copy(p01 = clean, selectedScenarioName = null)) }
    }

    fun clearActiveTab() {
        _uiState.update { state ->
            when (state.activeTab) {
                MedicTab.DRIP -> state.copy(
                    drip = state.drip.copy(
                        selectedPresetId = null,
                        selectedPresetName = null,
                        volumeMl = "",
                        timeValue = "",
                        desiredDose = "",
                        concentrationPerMl = "",
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
                        selectedPresetId = null,
                        selectedPresetName = null,
                        desiredDose = "",
                        concentrationPerMl = "",
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
                MedicTab.ABG -> state.copy(
                    abg = state.abg.copy(
                        selectedPresetName = null,
                        ph = "",
                        paco2 = "",
                        hco3 = "",
                        pao2 = "",
                        fio2Percent = "",
                        na = "",
                        cl = ""
                    )
                )
                MedicTab.VENT -> state.copy(
                    vent = if (state.vent.subMode == VentSubMode.SETUP) {
                        state.vent.copy(feet = "", inches = "", cm = "", directWeightKg = "")
                    } else {
                        state.vent.copy(
                            selectedScenarioName = null,
                            cstat = "",
                            rinsp = "",
                            rcexp = "",
                            ppeak = "",
                            pplat = "",
                            peep = "",
                            vte = "",
                            autoPeep = "",
                            vLeakPercent = "",
                            rr = "",
                            tiSec = "",
                            p01 = ""
                        )
                    }
                )
            }
        }
    }

    private fun computeTotalDrugString(concPerMlStr: String, volumeMlStr: String): String {
        val conc = concPerMlStr.toDoubleOrNull()
        val vol = volumeMlStr.toDoubleOrNull()
        if (conc == null || vol == null || conc <= 0.0 || vol <= 0.0) return ""
        return MedicMath.formatNumber(conc * vol, 4)
    }

    private fun computeConcentrationString(totalDrugStr: String, volumeMlStr: String): String {
        val drug = totalDrugStr.toDoubleOrNull()
        val vol = volumeMlStr.toDoubleOrNull()
        if (drug == null || vol == null || drug <= 0.0 || vol <= 0.0) return ""
        return MedicMath.formatNumber(drug / vol, 4)
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

    private fun sanitizeSignedDecimal(input: String): String {
        val trimmed = input.trim()
        val isNegative = trimmed.startsWith("-")
        val digits = sanitizeDecimal(trimmed.removePrefix("-"))
        return if (isNegative) "-$digits" else digits
    }
}
