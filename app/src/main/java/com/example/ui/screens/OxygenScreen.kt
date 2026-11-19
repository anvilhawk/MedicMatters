package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.domain.MedicMath
import com.example.domain.OxygenCylinder
import com.example.domain.OxygenStatus
import com.example.ui.OxygenUiState
import com.example.ui.components.ClinicalSectionCard
import com.example.ui.components.HeroResultCard
import com.example.ui.components.MedicNumericField
import com.example.ui.components.SelectablePillChip
import com.example.ui.theme.LocalClinicalStatusColors

@Composable
fun OxygenScreen(
    state: OxygenUiState,
    onCylinderSelect: (OxygenCylinder) -> Unit,
    onPsiChange: (String) -> Unit,
    onPsiSliderChange: (Int) -> Unit,
    onFlowRateChange: (String) -> Unit,
    onToggleSafeResidual: () -> Unit,
    modifier: Modifier = Modifier
) {
    val result = state.result
    val statusColors = LocalClinicalStatusColors.current

    val accentColor = when (result?.status) {
        OxygenStatus.SAFE -> statusColors.safe
        OxygenStatus.CAUTION -> statusColors.caution
        OxygenStatus.CRITICAL -> statusColors.critical
        null -> MaterialTheme.colorScheme.primary
    }

    val statusSubtitle = when (result?.status) {
        OxygenStatus.SAFE -> "${state.cylinder.fullName} · Adequate Reserve"
        OxygenStatus.CAUTION -> "${state.cylinder.fullName} · Low Reserve (<30m)"
        OxygenStatus.CRITICAL -> "${state.cylinder.fullName} · Change Tank (<10m)"
        null -> state.cylinder.fullName
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HeroResultCard(
            label = if (state.safeResidualPsi > 0) "Usable Time (to 200 PSI)" else "Total Time (to Empty)",
            primaryValue = result?.let { MedicMath.formatDuration(it.usableMinutes) } ?: "--",
            primaryUnit = result?.let {
                if (it.usableMinutes >= 60.0) "(${MedicMath.formatNumber(it.usableMinutes, 0)} min)" else ""
            } ?: "",
            accentColor = accentColor,
            subtitle = statusSubtitle,
            warningText = when {
                result != null && state.safeResidualPsi > 0 && result.usableMinutes <= 0.0 ->
                    "Tank is at or below 200 PSI safe residual pressure — swap cylinder immediately."
                result != null && result.usableMinutes < 10.0 ->
                    "Less than 10 minutes remaining at ${state.flowRateLpm} L/min — prepare backup O2 cylinder."
                else -> null
            },
            metrics = listOf(
                "Usable O2" to (result?.let { "${MedicMath.formatNumber(it.usableLiters, 0)} L" } ?: "--"),
                "To 0 PSI" to (result?.let { MedicMath.formatDuration(it.totalMinutesToZero) } ?: "--"),
                "Factor" to "${state.cylinder.factor}"
            ),
            modifier = Modifier.testTag("oxygen_result_card")
        ) {
            OxygenTankGauge(
                fillFraction = result?.gaugeFraction ?: 0f,
                safeResidualFraction = result?.safeResidualFraction ?: 0.1f,
                fillColor = accentColor
            )
        }

        // Cylinder Selector
        ClinicalSectionCard(title = "EMS Cylinder Size") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OxygenCylinder.entries.forEach { cyl ->
                    SelectablePillChip(
                        label = cyl.shortName,
                        subLabel = "× ${cyl.factor}",
                        selected = state.cylinder == cyl,
                        onClick = { onCylinderSelect(cyl) },
                        testTag = "oxygen_cyl_${cyl.name.lowercase()}"
                    )
                }
            }
        }

        // Tank Pressure Card
        ClinicalSectionCard(
            title = "Tank Gauge Pressure",
            trailingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "200 PSI Safe Buffer",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Switch(
                        checked = state.safeResidualPsi > 0.0,
                        onCheckedChange = { onToggleSafeResidual() },
                        modifier = Modifier.testTag("oxygen_residual_switch")
                    )
                }
            }
        ) {
            MedicNumericField(
                value = state.currentPsi,
                onValueChange = onPsiChange,
                label = "Current Pressure",
                unitSuffix = "PSI",
                testTag = "oxygen_psi_input",
                imeAction = ImeAction.Next,
                allowDecimal = false
            )

            val sliderVal = (state.currentPsi.toFloatOrNull() ?: 0f).coerceIn(0f, 2500f)
            Slider(
                value = sliderVal,
                onValueChange = { onPsiSliderChange((it / 50f).toInt() * 50) },
                valueRange = 0f..2500f,
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor
                ),
                modifier = Modifier.testTag("oxygen_psi_slider")
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(2000, 1500, 1000, 500, 200).forEach { presetPsi ->
                    SelectablePillChip(
                        label = "$presetPsi",
                        subLabel = "PSI",
                        selected = state.currentPsi == presetPsi.toString(),
                        onClick = { onPsiSliderChange(presetPsi) },
                        testTag = "oxygen_psi_preset_$presetPsi"
                    )
                }
            }
        }

        // Flow Rate Card
        ClinicalSectionCard(title = "Oxygen Flow Rate (L/min)") {
            MedicNumericField(
                value = state.flowRateLpm,
                onValueChange = onFlowRateChange,
                label = "Flow Rate",
                unitSuffix = "L/min",
                testTag = "oxygen_flow_input"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val flowPresets = listOf(
                    "2" to "NC Low",
                    "4" to "NC",
                    "6" to "NC/Neb",
                    "10" to "Mask",
                    "15" to "NRB/BVM",
                    "25" to "High Flow"
                )
                flowPresets.forEach { (lpm, device) ->
                    SelectablePillChip(
                        label = "$lpm L/m",
                        subLabel = device,
                        selected = state.flowRateLpm == lpm,
                        onClick = { onFlowRateChange(lpm) },
                        testTag = "oxygen_flow_preset_$lpm"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun OxygenTankGauge(
    fillFraction: Float,
    safeResidualFraction: Float,
    fillColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedFill by animateFloatAsState(
        targetValue = fillFraction.coerceIn(0f, 1f),
        animationSpec = tween(350),
        label = "tankFill"
    )
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val markerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp)
        ) {
            val barHeight = size.height * 0.65f
            val topOffset = (size.height - barHeight) / 2f
            val corner = CornerRadius(barHeight / 2f, barHeight / 2f)

            drawRoundRect(
                color = trackColor,
                topLeft = Offset(0f, topOffset),
                size = Size(size.width, barHeight),
                cornerRadius = corner
            )

            if (animatedFill > 0f) {
                drawRoundRect(
                    color = fillColor,
                    topLeft = Offset(0f, topOffset),
                    size = Size((size.width * animatedFill).coerceAtLeast(barHeight), barHeight),
                    cornerRadius = corner
                )
            }

            if (safeResidualFraction in 0.01f..0.95f) {
                val markerX = size.width * safeResidualFraction
                drawLine(
                    color = markerColor,
                    start = Offset(markerX, 0f),
                    end = Offset(markerX, size.height),
                    strokeWidth = 2.5.dp.toPx()
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "0 PSI (200 PSI Safe Line)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "2000 PSI Full",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
