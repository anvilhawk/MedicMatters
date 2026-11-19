package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.domain.HeightUnit
import com.example.domain.MedicMath
import com.example.domain.PatientSex
import com.example.domain.VentMath
import com.example.domain.VentMechanicsScenario
import com.example.domain.VentStrategy
import com.example.domain.VentSubMode
import com.example.ui.VentUiState
import com.example.ui.components.ClinicalSectionCard
import com.example.ui.components.HeroResultCard
import com.example.ui.components.MedicNumericField
import com.example.ui.components.SegmentedChoiceRow
import com.example.ui.components.SelectablePillChip
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalClinicalStatusColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VentScreen(
    state: VentUiState,
    onSubModeChange: (VentSubMode) -> Unit,
    onStrategyChange: (VentStrategy) -> Unit,
    onUseDirectWeightChange: (Boolean) -> Unit,
    onSexChange: (PatientSex) -> Unit,
    onHeightUnitChange: (HeightUnit) -> Unit,
    onFeetChange: (String) -> Unit,
    onInchesChange: (String) -> Unit,
    onCmChange: (String) -> Unit,
    onDirectWeightChange: (String) -> Unit,
    onTargetVtMlPerKgChange: (String) -> Unit,
    onSyncFromIbw: () -> Unit,
    onApplyScenario: (VentMechanicsScenario) -> Unit,
    onCstatChange: (String) -> Unit,
    onRinspChange: (String) -> Unit,
    onRcexpChange: (String) -> Unit,
    onPpeakChange: (String) -> Unit,
    onPplatChange: (String) -> Unit,
    onPeepChange: (String) -> Unit,
    onVteChange: (String) -> Unit,
    onAutoPeepChange: (String) -> Unit,
    onVLeakChange: (String) -> Unit,
    onRrChange: (String) -> Unit,
    onTiSecChange: (String) -> Unit,
    onP01Change: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SegmentedChoiceRow(
            options = VentSubMode.entries,
            selected = state.subMode,
            onSelect = onSubModeChange,
            labelProvider = { it.label },
            testTagPrefix = "vent_submode"
        )

        if (state.subMode == VentSubMode.SETUP) {
            VentSetupContent(
                state = state,
                onStrategyChange = onStrategyChange,
                onUseDirectWeightChange = onUseDirectWeightChange,
                onSexChange = onSexChange,
                onHeightUnitChange = onHeightUnitChange,
                onFeetChange = onFeetChange,
                onInchesChange = onInchesChange,
                onCmChange = onCmChange,
                onDirectWeightChange = onDirectWeightChange,
                onTargetVtMlPerKgChange = onTargetVtMlPerKgChange,
                onSyncFromIbw = onSyncFromIbw
            )
        } else {
            VentTroubleshootContent(
                state = state,
                onApplyScenario = onApplyScenario,
                onCstatChange = onCstatChange,
                onRinspChange = onRinspChange,
                onRcexpChange = onRcexpChange,
                onPpeakChange = onPpeakChange,
                onPplatChange = onPplatChange,
                onPeepChange = onPeepChange,
                onVteChange = onVteChange,
                onAutoPeepChange = onAutoPeepChange,
                onVLeakChange = onVLeakChange,
                onRrChange = onRrChange,
                onTiSecChange = onTiSecChange,
                onP01Change = onP01Change
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VentSetupContent(
    state: VentUiState,
    onStrategyChange: (VentStrategy) -> Unit,
    onUseDirectWeightChange: (Boolean) -> Unit,
    onSexChange: (PatientSex) -> Unit,
    onHeightUnitChange: (HeightUnit) -> Unit,
    onFeetChange: (String) -> Unit,
    onInchesChange: (String) -> Unit,
    onCmChange: (String) -> Unit,
    onDirectWeightChange: (String) -> Unit,
    onTargetVtMlPerKgChange: (String) -> Unit,
    onSyncFromIbw: () -> Unit
) {
    val setup = state.setupResult
    val statusColors = LocalClinicalStatusColors.current

    val accentColor = when {
        setup == null -> MaterialTheme.colorScheme.primary
        setup.isVtOutsideStrategyRange -> statusColors.caution
        state.strategy == VentStrategy.METABOLIC_ACIDOSIS || state.strategy == VentStrategy.PERFUSION_LIMITED -> statusColors.caution
        else -> MaterialTheme.colorScheme.primary
    }

    // Hero Setup Result Card
    HeroResultCard(
        label = "${state.strategy.shortLabel} Initial Vent Settings",
        primaryValue = setup?.targetVtMl?.toString() ?: "--",
        primaryUnit = "mL VT",
        subtitle = setup?.let {
            "${MedicMath.formatNumber(it.vtMlPerKg, 1)} mL/kg · ${it.strategy.pcgSection}"
        } ?: "Enter height or weight",
        accentColor = accentColor,
        warningText = if (setup?.isVtOutsideStrategyRange == true) {
            setup.protocolWarnings.firstOrNull()
        } else null,
        isCriticalWarning = false,
        metrics = listOf(
            "Start RR" to (setup?.let { "${it.startingRr} bpm" } ?: "--"),
            "Target VE" to (setup?.let { "${MedicMath.formatNumber(it.targetVeLMin, 1)} L/m" } ?: "--"),
            "Ref Weight" to (setup?.let { "${MedicMath.formatNumber(it.weightKg, 1)} kg" } ?: "--")
        ),
        modifier = Modifier.testTag("vent_setup_result_card")
    ) {
        if (setup != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                VentSpecLine("VT Range", "${setup.vtRangeLowMl}–${setup.vtRangeHighMl} mL (${MedicMath.formatNumber(setup.strategy.vtLowMlPerKg, 0)}–${MedicMath.formatNumber(setup.strategy.vtHighMlPerKg, 0)} mL/kg)")
                VentSpecLine("Min VE Rule", setup.minVeLabel)
                VentSpecLine("Mode", setup.strategy.modeGuidance)
                VentSpecLine("PEEP / I:E", "${setup.strategy.defaultPeep}  |  I:E ${setup.strategy.defaultIe}")
                VentSpecLine("ETCO2 Goal", setup.strategy.etco2Target)
                VentSpecLine("SpO2 Goal", setup.strategy.spo2Target)
                VentSpecLine("High P Alarm", setup.highPressureAlarmGuidance)
                VentSpecLine("Cuff Press.", setup.cuffPressureGuidance)
            }
        }
    }

    // Protocol Strategy Selector
    ClinicalSectionCard(title = "Air Methods PCG Ventilation Strategy") {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VentStrategy.entries.forEach { strat ->
                SelectablePillChip(
                    label = strat.shortLabel,
                    subLabel = strat.pcgSection,
                    selected = state.strategy == strat,
                    onClick = { onStrategyChange(strat) },
                    testTag = "vent_strategy_${strat.name.lowercase()}"
                )
            }
        }
    }

    // Weight & VT mL/kg Input Card
    ClinicalSectionCard(
        title = "Patient Weight & Target VT",
        trailingContent = {
            TextButton(
                onClick = onSyncFromIbw,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("vent_sync_ibw_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Sync,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text("Sync IBW", style = MaterialTheme.typography.labelLarge)
            }
        }
    ) {
        SegmentedChoiceRow(
            options = listOf(false, true),
            selected = state.useDirectWeight,
            onSelect = onUseDirectWeightChange,
            labelProvider = { if (it) "Direct (kg)" else "IBW (Height/Sex)" },
            testTagPrefix = "vent_weight_mode"
        )

        if (state.useDirectWeight) {
            MedicNumericField(
                value = state.directWeightKg,
                onValueChange = onDirectWeightChange,
                label = "Patient Weight (IBW or Actual Peds)",
                unitSuffix = "kg",
                testTag = "vent_direct_weight_input",
                imeAction = ImeAction.Next
            )
        } else {
            SegmentedChoiceRow(
                options = PatientSex.entries,
                selected = state.sex,
                onSelect = onSexChange,
                labelProvider = { it.label },
                testTagPrefix = "vent_sex"
            )
            SegmentedChoiceRow(
                options = HeightUnit.entries,
                selected = state.heightUnit,
                onSelect = onHeightUnitChange,
                labelProvider = { it.label },
                testTagPrefix = "vent_height_unit"
            )
            if (state.heightUnit == HeightUnit.FT_IN) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MedicNumericField(
                        value = state.feet,
                        onValueChange = onFeetChange,
                        label = "Feet",
                        unitSuffix = "ft",
                        testTag = "vent_feet_input",
                        modifier = Modifier.weight(1f),
                        imeAction = ImeAction.Next,
                        allowDecimal = false
                    )
                    MedicNumericField(
                        value = state.inches,
                        onValueChange = onInchesChange,
                        label = "Inches",
                        unitSuffix = "in",
                        testTag = "vent_inches_input",
                        modifier = Modifier.weight(1f),
                        imeAction = ImeAction.Next
                    )
                }
            } else {
                MedicNumericField(
                    value = state.cm,
                    onValueChange = onCmChange,
                    label = "Height",
                    unitSuffix = "cm",
                    testTag = "vent_cm_input",
                    imeAction = ImeAction.Next
                )
            }
        }

        MedicNumericField(
            value = state.targetVtMlPerKg,
            onValueChange = onTargetVtMlPerKgChange,
            label = "Target Tidal Volume",
            unitSuffix = "mL/kg",
            testTag = "vent_vt_ml_kg_input"
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("4", "5", "6", "7", "8", "10").forEach { vtChip ->
                SelectablePillChip(
                    label = "$vtChip mL/kg",
                    selected = state.targetVtMlPerKg == vtChip,
                    onClick = { onTargetVtMlPerKgChange(vtChip) },
                    testTag = "vent_vt_chip_$vtChip"
                )
            }
        }
    }

    // Strategy Protocol Warnings & Clinical Pearls
    if (setup != null && setup.protocolWarnings.isNotEmpty()) {
        ClinicalSectionCard(title = "${state.strategy.shortLabel} Protocol Safeguards") {
            setup.protocolWarnings.forEach { warn ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 7.dp)
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = warn,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // Pediatric & Infant Vent Card Reference (Air Methods Form 5805-CS)
    PedsVentReferenceCard(autoExpand = state.strategy == VentStrategy.PEDIATRIC)

    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun VentSpecLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(0.28f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = JetBrainsMonoFontFamily
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PedsVentReferenceCard(autoExpand: Boolean) {
    var expanded by rememberSaveable(autoExpand) { mutableStateOf(autoExpand) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vent_peds_card"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AIR METHODS FORM 5805-CS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMonoFontFamily
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pediatric & Infant Vent Quick Card",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded) "Collapse pediatric vent card" else "Expand pediatric vent card"
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text("Age", Modifier.weight(0.9f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text("Wt", Modifier.weight(0.9f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text("RR", Modifier.weight(0.9f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text("TI", Modifier.weight(1.0f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text("VT", Modifier.weight(1.2f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    VentMath.PEDS_VENT_CARD_ROWS.forEach { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(row.age, Modifier.weight(0.9f), style = MaterialTheme.typography.labelSmall)
                            Text(row.weightKg, Modifier.weight(0.9f), style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily))
                            Text(row.rateBpm, Modifier.weight(0.9f), style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily))
                            Text(row.iTimeSec, Modifier.weight(1.0f), style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily))
                            Text(row.vtMl, Modifier.weight(1.2f), style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily))
                        }
                    }
                }
            }
        }
    }
}
