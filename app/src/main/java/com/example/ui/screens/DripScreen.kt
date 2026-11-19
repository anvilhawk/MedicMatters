package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.domain.DoseRateUnit
import com.example.domain.DripMode
import com.example.domain.DripPreset
import com.example.domain.MedicMath
import com.example.domain.MedicationUnit
import com.example.domain.TimeUnit
import com.example.domain.WeightUnit
import com.example.ui.DripUiState
import com.example.ui.components.ClinicalSectionCard
import com.example.ui.components.HeroResultCard
import com.example.ui.components.MedicNumericField
import com.example.ui.components.SegmentedChoiceRow
import com.example.ui.components.SelectablePillChip
import com.example.ui.theme.JetBrainsMonoFontFamily
import kotlinx.coroutines.delay

@Composable
fun DripScreen(
    state: DripUiState,
    onModeChange: (DripMode) -> Unit,
    onVolumeChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onTimeUnitChange: (TimeUnit) -> Unit,
    onDropFactorSelect: (Int) -> Unit,
    onEnableCustomDropFactor: () -> Unit,
    onCustomDropFactorChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onWeightUnitChange: (WeightUnit) -> Unit,
    onDesiredDoseChange: (String) -> Unit,
    onDoseRateUnitChange: (DoseRateUnit) -> Unit,
    onDrugAmountChange: (String) -> Unit,
    onDrugUnitChange: (MedicationUnit) -> Unit,
    onBagVolumeChange: (String) -> Unit,
    onApplyPreset: (DripPreset) -> Unit,
    onToggleMetronome: () -> Unit,
    onTapDrip: () -> Unit,
    onResetTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val result = state.result
    var beatActive by remember { mutableStateOf(false) }

    val canMetronomeBeat = result != null &&
        result.warning == null &&
        result.gttPerMinute in 1.0..180.0

    LaunchedEffect(state.metronomeEnabled, result?.secondsPerDrop, canMetronomeBeat) {
        if (state.metronomeEnabled && canMetronomeBeat && result?.secondsPerDrop != null) {
            val intervalMs = (result.secondsPerDrop * 1000.0).toLong().coerceAtLeast(333L)
            while (true) {
                beatActive = true
                delay(140L.coerceAtMost(intervalMs / 2))
                beatActive = false
                delay((intervalMs - 140L).coerceAtLeast(160L))
            }
        } else {
            beatActive = false
        }
    }

    val pulseScale by animateFloatAsState(
        targetValue = if (beatActive) 1.24f else 1.0f,
        animationSpec = tween(durationMillis = 120),
        label = "dripBeatScale"
    )
    val pulseColor by animateColorAsState(
        targetValue = if (beatActive) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(durationMillis = 120),
        label = "dripBeatColor"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Quick EMS Drip Presets
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "COMMON EMS DRIPS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MedicMath.DRIP_PRESETS.forEach { preset ->
                    SelectablePillChip(
                        label = preset.name,
                        subLabel = preset.subtitle,
                        selected = false,
                        onClick = { onApplyPreset(preset) },
                        testTag = "drip_preset_${preset.name.lowercase()}"
                    )
                }
            }
        }

        // Hero Result Card + Visual Drip Beat & Tap-to-Count
        val secPerDropStr = result?.secondsPerDrop?.let { sec ->
            if (sec >= 1.0) "${MedicMath.formatNumber(sec, 2)} s" else "< 1 s"
        } ?: "--"

        val metricsList = buildList {
            add("15s Count" to (result?.let { "${MedicMath.formatNumber(it.gttPer15Seconds, 1)} gtt" } ?: "--"))
            add("Pump Rate" to (result?.let { "${MedicMath.formatNumber(it.mlPerHour, 1)} mL/h" } ?: "--"))
            add(
                if (result?.concentrationText != null) "Bag Conc." to result.concentrationText
                else "Drop Interval" to secPerDropStr
            )
        }

        HeroResultCard(
            label = "Target IV Drip Rate",
            primaryValue = result?.let {
                if (it.warning?.startsWith("Unit mismatch") == true) "--"
                else "${it.roundedGttPerMinute}"
            } ?: "--",
            primaryUnit = "gtt/min",
            subtitle = result?.let {
                if (it.warning?.startsWith("Unit mismatch") == true) null
                else "Exact ${MedicMath.formatNumber(it.gttPerMinute, 1)} gtt/min · ${state.effectiveDropFactor ?: "--"} gtt/mL"
            },
            warningText = result?.warning,
            metrics = metricsList,
            modifier = Modifier.testTag("drip_result_card")
        ) {
            // Drip Cadence Beat + Manual Chamber Tap Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = canMetronomeBeat) { onToggleMetronome() }
                        .testTag("drip_metronome_button"),
                    shape = RoundedCornerShape(12.dp),
                    color = if (state.metronomeEnabled && canMetronomeBeat) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    },
                    border = BorderStroke(
                        1.dp,
                        if (state.metronomeEnabled && canMetronomeBeat) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(pulseColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (beatActive) {
                                    Icons.Rounded.WaterDrop
                                } else if (state.metronomeEnabled && canMetronomeBeat) {
                                    Icons.Rounded.Stop
                                } else {
                                    Icons.Rounded.PlayArrow
                                },
                                contentDescription = "Toggle Visual Drip Beat",
                                tint = if (beatActive) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (state.metronomeEnabled && canMetronomeBeat) "Stop Beat" else "Visual Beat",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when {
                                    result == null -> "Enter values"
                                    !canMetronomeBeat -> ">180 gtt/m too fast"
                                    else -> "Flash every $secPerDropStr"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Manual Tap-to-Measure existing chamber
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTapDrip() }
                        .testTag("drip_tap_measure_button"),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.TouchApp,
                                contentDescription = "Tap to count live drops",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = state.measuredTapGttPerMin?.let {
                                        "${MedicMath.formatNumber(it, 0)} gtt/m"
                                    } ?: "Tap Drops",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontFamily = if (state.measuredTapGttPerMin != null) {
                                            JetBrainsMonoFontFamily
                                        } else MaterialTheme.typography.labelLarge.fontFamily
                                    ),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (state.tapTimestampsMs.isEmpty()) "Count live IV"
                                    else "${state.tapTimestampsMs.size} taps",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (state.tapTimestampsMs.isNotEmpty()) {
                            IconButton(
                                onClick = onResetTap,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = "Reset Tap Counter",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Mode Switcher
        SegmentedChoiceRow(
            options = DripMode.entries,
            selected = state.mode,
            onSelect = onModeChange,
            labelProvider = { it.label },
            testTagPrefix = "drip_mode"
        )

        // Inputs Card
        AnimatedVisibility(visible = state.mode == DripMode.VOLUME_TIME) {
            ClinicalSectionCard(title = "Infusion Volume & Time") {
                MedicNumericField(
                    value = state.volumeMl,
                    onValueChange = onVolumeChange,
                    label = "Total Volume to Infuse",
                    unitSuffix = "mL",
                    testTag = "drip_volume_input",
                    imeAction = ImeAction.Next
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MedicNumericField(
                        value = state.timeValue,
                        onValueChange = onTimeChange,
                        label = "Infusion Duration",
                        unitSuffix = state.timeUnit.label,
                        testTag = "drip_time_input",
                        modifier = Modifier.weight(1.3f)
                    )
                    SegmentedChoiceRow(
                        options = TimeUnit.entries,
                        selected = state.timeUnit,
                        onSelect = onTimeUnitChange,
                        labelProvider = { it.label },
                        testTagPrefix = "drip_time_unit",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        AnimatedVisibility(visible = state.mode == DripMode.MED_INFUSION) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ClinicalSectionCard(title = "Ordered Dose & Weight") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MedicNumericField(
                            value = state.desiredDose,
                            onValueChange = onDesiredDoseChange,
                            label = "Ordered Dose",
                            unitSuffix = state.doseRateUnit.label,
                            testTag = "drip_dose_input",
                            modifier = Modifier.weight(1f),
                            imeAction = ImeAction.Next
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DoseRateUnit.entries.forEach { unit ->
                            SelectablePillChip(
                                label = unit.label,
                                selected = state.doseRateUnit == unit,
                                onClick = { onDoseRateUnitChange(unit) },
                                testTag = "drip_dose_unit_${unit.name.lowercase()}"
                            )
                        }
                    }

                    AnimatedVisibility(visible = state.doseRateUnit.isWeightBased) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MedicNumericField(
                                value = state.weight,
                                onValueChange = onWeightChange,
                                label = "Patient Weight",
                                unitSuffix = state.weightUnit.label,
                                testTag = "drip_weight_input",
                                modifier = Modifier.weight(1.3f),
                                imeAction = ImeAction.Next
                            )
                            SegmentedChoiceRow(
                                options = WeightUnit.entries,
                                selected = state.weightUnit,
                                onSelect = onWeightUnitChange,
                                labelProvider = { it.label },
                                testTagPrefix = "drip_weight_unit",
                                modifier = Modifier.weight(0.9f)
                            )
                        }
                    }
                }

                ClinicalSectionCard(title = "IV Bag Concentration") {
                    MedicNumericField(
                        value = state.drugAmount,
                        onValueChange = onDrugAmountChange,
                        label = "Drug Added to Bag",
                        unitSuffix = state.drugUnit.label,
                        testTag = "drip_drug_amount_input",
                        imeAction = ImeAction.Next
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            MedicationUnit.MCG,
                            MedicationUnit.MG,
                            MedicationUnit.G,
                            MedicationUnit.UNITS
                        ).forEach { unit ->
                            SelectablePillChip(
                                label = unit.label,
                                selected = state.drugUnit == unit,
                                onClick = { onDrugUnitChange(unit) },
                                testTag = "drip_bag_unit_${unit.label.lowercase()}"
                            )
                        }
                    }
                    MedicNumericField(
                        value = state.bagVolumeMl,
                        onValueChange = onBagVolumeChange,
                        label = "Bag Fluid Volume",
                        unitSuffix = "mL",
                        testTag = "drip_bag_volume_input"
                    )
                }
            }
        }

        // Tubing Drop Factor Card
        ClinicalSectionCard(title = "IV Tubing Drop Factor (gtt/mL)") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MedicMath.STANDARD_DROP_FACTORS.forEach { factor ->
                    val label = if (factor == 60) "60 (Micro)" else "$factor"
                    SelectablePillChip(
                        label = label,
                        subLabel = "gtt/mL",
                        selected = !state.isCustomDropFactor && state.dropFactor == factor,
                        onClick = { onDropFactorSelect(factor) },
                        testTag = "drip_factor_$factor"
                    )
                }
                SelectablePillChip(
                    label = "Custom",
                    subLabel = "gtt/mL",
                    selected = state.isCustomDropFactor,
                    onClick = onEnableCustomDropFactor,
                    testTag = "drip_factor_custom"
                )
            }

            AnimatedVisibility(visible = state.isCustomDropFactor) {
                MedicNumericField(
                    value = state.customDropFactor,
                    onValueChange = onCustomDropFactorChange,
                    label = "Custom Drop Factor",
                    unitSuffix = "gtt/mL",
                    testTag = "drip_custom_factor_input",
                    allowDecimal = false
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
