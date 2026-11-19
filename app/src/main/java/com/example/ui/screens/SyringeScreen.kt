package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.domain.DoseRangeStatus
import com.example.domain.MedicMath
import com.example.domain.MedicationUnit
import com.example.domain.SyringePreset
import com.example.domain.WeightUnit
import com.example.ui.SyringeUiState
import com.example.ui.components.ClinicalSectionCard
import com.example.ui.components.HeroResultCard
import com.example.ui.components.MedicNumericField
import com.example.ui.components.ProtocolDoseBanner
import com.example.ui.components.SegmentedChoiceRow
import com.example.ui.components.SelectablePillChip
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalClinicalStatusColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SyringeScreen(
    state: SyringeUiState,
    onWeightBasedChange: (Boolean) -> Unit,
    onWeightChange: (String) -> Unit,
    onWeightUnitChange: (WeightUnit) -> Unit,
    onDesiredDoseChange: (String) -> Unit,
    onDesiredUnitChange: (MedicationUnit) -> Unit,
    onConcentrationModeChange: (Boolean) -> Unit,
    onConcentrationPerMlChange: (String) -> Unit,
    onConcentrationUnitChange: (MedicationUnit) -> Unit,
    onQuickConcentrationSelect: (String, MedicationUnit) -> Unit,
    onDrugOnHandChange: (String) -> Unit,
    onOnHandUnitChange: (MedicationUnit) -> Unit,
    onVolumeOnHandChange: (String) -> Unit,
    onApplyPreset: (SyringePreset) -> Unit,
    onClearPreset: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val result = state.result

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Common EMS Vial Presets
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PROTOCOL VIALS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Customize",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onOpenSettings() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("syringe_customize_protocols")
                )
            }
            if (state.presets.isEmpty()) {
                SelectablePillChip(
                    label = "+ Add Vial Protocol",
                    subLabel = "Open Settings",
                    selected = false,
                    onClick = onOpenSettings,
                    testTag = "syringe_empty_add_chip"
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.presets.forEach { preset ->
                        val isSelected = (state.selectedPresetId != null && state.selectedPresetId == preset.id) ||
                            (state.selectedPresetId == null && state.selectedPresetName == preset.name)
                        SelectablePillChip(
                            label = preset.name,
                            subLabel = preset.detail,
                            selected = isSelected,
                            onClick = { onApplyPreset(preset) },
                            testTag = "syringe_preset_${preset.name.lowercase()}"
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = state.protocolValidation != null) {
            state.protocolValidation?.let { validation ->
                ProtocolDoseBanner(
                    validation = validation,
                    onDismiss = onClearPreset,
                    testTag = "syringe_protocol_banner"
                )
            }
        }

        // Hero Result Card + Visual Syringe Barrel
        val statusColors = LocalClinicalStatusColors.current
        val validation = state.protocolValidation
        val isProtocolCritical = validation?.status == DoseRangeStatus.ABOVE_MAX ||
            validation?.status == DoseRangeStatus.MAX_SINGLE_EXCEEDED
        val isProtocolCaution = validation?.status == DoseRangeStatus.BELOW_MIN
        val heroAccent = when {
            isProtocolCritical -> statusColors.critical
            isProtocolCaution -> statusColors.caution
            else -> MaterialTheme.colorScheme.primary
        }
        val protocolWarning = if (isProtocolCritical || isProtocolCaution) validation?.message else null
        val combinedWarning = listOfNotNull(protocolWarning, result?.warning)
            .distinct()
            .joinToString("\n")
            .ifEmpty { null }

        HeroResultCard(
            label = "Volume to Draw Up",
            primaryValue = result?.takeIf { it.error == null }?.let {
                MedicMath.formatNumber(it.volumeToDrawMl, 2)
            } ?: "--",
            primaryUnit = "mL",
            accentColor = heroAccent,
            subtitle = result?.takeIf { it.error == null }?.let {
                "Use ${it.recommendedSyringeMl} mL syringe"
            },
            errorText = result?.error,
            warningText = combinedWarning,
            isCriticalWarning = isProtocolCritical,
            metrics = listOf(
                "Total Dose" to (result?.totalDoseFormatted ?: "--"),
                "Vial Conc." to (result?.concentrationFormatted ?: "--"),
                "Vials" to (result?.takeIf { it.error == null }?.vialsNeeded?.toString() ?: "--")
            ),
            modifier = Modifier.testTag("syringe_result_card")
        ) {
            if (result != null && result.error == null) {
                VisualSyringeCanvas(
                    volumeToDrawMl = result.volumeToDrawMl,
                    syringeCapacityMl = result.recommendedSyringeMl,
                    fluidColor = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Desired Dose Section
        ClinicalSectionCard(
            title = "Ordered Patient Dose",
            trailingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Weight-Based (/kg)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Switch(
                        checked = state.isWeightBased,
                        onCheckedChange = onWeightBasedChange,
                        modifier = Modifier.testTag("syringe_weight_based_switch")
                    )
                }
            }
        ) {
            AnimatedVisibility(visible = state.isWeightBased) {
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
                        testTag = "syringe_weight_input",
                        modifier = Modifier.weight(1.3f),
                        imeAction = ImeAction.Next
                    )
                    SegmentedChoiceRow(
                        options = WeightUnit.entries,
                        selected = state.weightUnit,
                        onSelect = onWeightUnitChange,
                        labelProvider = { it.label },
                        testTagPrefix = "syringe_weight_unit",
                        modifier = Modifier.weight(0.9f)
                    )
                }
            }

            MedicNumericField(
                value = state.desiredDose,
                onValueChange = onDesiredDoseChange,
                label = if (state.isWeightBased) "Dose per kg" else "Desired Dose",
                unitSuffix = if (state.isWeightBased) "${state.desiredUnit.label}/kg" else state.desiredUnit.label,
                testTag = "syringe_desired_dose_input",
                imeAction = ImeAction.Next
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MedicationUnit.entries.forEach { unit ->
                    SelectablePillChip(
                        label = if (state.isWeightBased) "${unit.label}/kg" else unit.label,
                        selected = state.desiredUnit == unit,
                        onClick = { onDesiredUnitChange(unit) },
                        testTag = "syringe_desired_unit_${unit.label.lowercase()}"
                    )
                }
            }
        }

        // Vial Concentration on Hand Section
        ClinicalSectionCard(title = "Vial / Ampule on Hand") {
            SegmentedChoiceRow(
                options = listOf(true, false),
                selected = state.isConcentrationMode,
                onSelect = onConcentrationModeChange,
                labelProvider = { if (it) "Conc. (/mL)" else "Total Drug" },
                testTagPrefix = "syringe_conc_mode"
            )

            if (state.isConcentrationMode) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple("50", MedicationUnit.MCG, "50 mcg/mL"),
                        Triple("1", MedicationUnit.MG, "1 mg/mL"),
                        Triple("2", MedicationUnit.MG, "2 mg/mL"),
                        Triple("5", MedicationUnit.MG, "5 mg/mL"),
                        Triple("10", MedicationUnit.MG, "10 mg/mL"),
                        Triple("50", MedicationUnit.MG, "50 mg/mL")
                    ).forEach { (amount, unit, label) ->
                        SelectablePillChip(
                            label = label,
                            selected = state.concentrationPerMl == amount && state.concentrationUnit == unit,
                            onClick = { onQuickConcentrationSelect(amount, unit) },
                            testTag = "syringe_quick_conc_${amount}_${unit.label.lowercase()}"
                        )
                    }
                }

                MedicNumericField(
                    value = state.concentrationPerMl,
                    onValueChange = onConcentrationPerMlChange,
                    label = "Vial Concentration",
                    unitSuffix = "${state.concentrationUnit.label}/mL",
                    testTag = "syringe_conc_input",
                    imeAction = ImeAction.Next
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MedicationUnit.entries.forEach { unit ->
                        SelectablePillChip(
                            label = "${unit.label}/mL",
                            selected = state.concentrationUnit == unit,
                            onClick = { onConcentrationUnitChange(unit) },
                            testTag = "syringe_conc_unit_${unit.label.lowercase()}"
                        )
                    }
                }
            } else {
                MedicNumericField(
                    value = state.drugOnHand,
                    onValueChange = onDrugOnHandChange,
                    label = "Total Drug in Vial",
                    unitSuffix = state.onHandUnit.label,
                    testTag = "syringe_drug_on_hand_input",
                    imeAction = ImeAction.Next
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MedicationUnit.entries.forEach { unit ->
                        SelectablePillChip(
                            label = unit.label,
                            selected = state.onHandUnit == unit,
                            onClick = { onOnHandUnitChange(unit) },
                            testTag = "syringe_vial_unit_${unit.label.lowercase()}"
                        )
                    }
                }
            }

            MedicNumericField(
                value = state.volumeOnHandMl,
                onValueChange = onVolumeOnHandChange,
                label = "Vial Liquid Volume",
                unitSuffix = "mL",
                testTag = "syringe_vial_volume_input"
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("1", "2", "5", "10", "20").forEach { vol ->
                    SelectablePillChip(
                        label = "$vol mL",
                        selected = state.volumeOnHandMl == vol,
                        onClick = { onVolumeOnHandChange(vol) },
                        testTag = "syringe_vial_vol_$vol"
                    )
                }
            }

            if (state.isConcentrationMode && state.drugOnHand.isNotEmpty() && state.volumeOnHandMl.isNotEmpty()) {
                Text(
                    text = "Vial Total: ${state.drugOnHand} ${state.onHandUnit.label} in ${state.volumeOnHandMl} mL",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = JetBrainsMonoFontFamily
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.testTag("syringe_vial_total_summary")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun VisualSyringeCanvas(
    volumeToDrawMl: Double,
    syringeCapacityMl: Int,
    fluidColor: Color,
    modifier: Modifier = Modifier
) {
    val targetFraction = (volumeToDrawMl / syringeCapacityMl.toDouble()).toFloat().coerceIn(0f, 1f)
    val animatedFill by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(350),
        label = "syringeFluidFill"
    )

    val outlineColor = MaterialTheme.colorScheme.outline
    val tickColor = MaterialTheme.colorScheme.onSurfaceVariant
    val plungerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
    val barrelBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            val hubWidth = 14.dp.toPx()
            val needleTipWidth = 16.dp.toPx()
            val barrelLeft = needleTipWidth + hubWidth
            val handleWidth = 20.dp.toPx()
            val barrelRight = size.width - handleWidth
            val barrelWidth = (barrelRight - barrelLeft).coerceAtLeast(40f)
            val barrelTop = size.height * 0.18f
            val barrelHeight = size.height * 0.64f
            val barrelCenterY = size.height / 2f

            // Needle tip & hub (left side)
            drawLine(
                color = outlineColor,
                start = Offset(0f, barrelCenterY),
                end = Offset(needleTipWidth, barrelCenterY),
                strokeWidth = 2.dp.toPx()
            )
            drawRoundRect(
                color = outlineColor.copy(alpha = 0.7f),
                topLeft = Offset(needleTipWidth, barrelCenterY - barrelHeight * 0.25f),
                size = Size(hubWidth, barrelHeight * 0.5f),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )

            // Syringe barrel background
            drawRoundRect(
                color = barrelBg,
                topLeft = Offset(barrelLeft, barrelTop),
                size = Size(barrelWidth, barrelHeight),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )

            // Fluid inside barrel (from left tip toward plunger)
            val fluidWidth = barrelWidth * animatedFill
            if (fluidWidth > 1f) {
                drawRoundRect(
                    color = fluidColor.copy(alpha = 0.75f),
                    topLeft = Offset(barrelLeft, barrelTop),
                    size = Size(fluidWidth, barrelHeight),
                    cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                )
            }

            // Plunger rubber stopper at fluid edge
            val stopperX = (barrelLeft + fluidWidth).coerceIn(barrelLeft, barrelRight - 6.dp.toPx())
            drawRect(
                color = plungerColor,
                topLeft = Offset(stopperX, barrelTop + 1.dp.toPx()),
                size = Size(6.dp.toPx(), barrelHeight - 2.dp.toPx())
            )

            // Plunger rod extending right
            drawRect(
                color = outlineColor.copy(alpha = 0.75f),
                topLeft = Offset(stopperX + 6.dp.toPx(), barrelCenterY - 3.dp.toPx()),
                size = Size((size.width - 6.dp.toPx() - (stopperX + 6.dp.toPx())).coerceAtLeast(0f), 6.dp.toPx())
            )

            // Plunger thumb flange (far right)
            drawRoundRect(
                color = plungerColor,
                topLeft = Offset(size.width - 6.dp.toPx(), barrelTop - 2.dp.toPx()),
                size = Size(6.dp.toPx(), barrelHeight + 4.dp.toPx()),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )

            // Barrel border
            drawRoundRect(
                color = outlineColor,
                topLeft = Offset(barrelLeft, barrelTop),
                size = Size(barrelWidth, barrelHeight),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Graduation tick marks
            val majorTicks = 10
            for (i in 1 until majorTicks) {
                val x = barrelLeft + (barrelWidth * i / majorTicks.toFloat())
                val isHalf = i % 2 == 0
                val tickLen = if (isHalf) barrelHeight * 0.34f else barrelHeight * 0.2f
                drawLine(
                    color = tickColor,
                    start = Offset(x, barrelTop),
                    end = Offset(x, barrelTop + tickLen),
                    strokeWidth = if (isHalf) 1.5.dp.toPx() else 1.dp.toPx()
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "0 mL",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Draw to ${MedicMath.formatNumber(volumeToDrawMl, 2)} mL on ${syringeCapacityMl} mL syringe",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$syringeCapacityMl mL",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
