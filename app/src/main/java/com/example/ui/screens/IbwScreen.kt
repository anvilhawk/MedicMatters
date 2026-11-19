package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Female
import androidx.compose.material.icons.rounded.Male
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
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
import com.example.ui.IbwUiState
import com.example.ui.components.ClinicalSectionCard
import com.example.ui.components.HeroResultCard
import com.example.ui.components.MedicNumericField
import com.example.ui.components.SegmentedChoiceRow

@Composable
fun IbwScreen(
    state: IbwUiState,
    onSexChange: (PatientSex) -> Unit,
    onHeightUnitChange: (HeightUnit) -> Unit,
    onFeetChange: (String) -> Unit,
    onInchesChange: (String) -> Unit,
    onCmChange: (String) -> Unit,
    onAdjustHeightInches: (Int) -> Unit,
    onApplyIbwToCalculators: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val result = state.result

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HeroResultCard(
            label = "Ideal Body Weight (Devine)",
            primaryValue = result?.let { MedicMath.formatNumber(it.ibwKg, 1) } ?: "--",
            primaryUnit = "kg",
            subtitle = result?.let {
                "${state.sex.label} · ${MedicMath.formatNumber(it.heightCm, 0)} cm (${MedicMath.formatNumber(it.heightInches, 1)} in)"
            },
            warningText = if (result?.isUnderFiveFeet == true) {
                "Height is under 5 ft (60 in). Standard Devine linear formula is calibrated for adults >= 5 ft; verify pediatric/small-adult dosing per protocol."
            } else null,
            metrics = listOf(
                "IBW (lbs)" to (result?.let { "${MedicMath.formatNumber(it.ibwLbs, 1)} lbs" } ?: "--"),
                "Vent Vt (6-8 mL/kg)" to (result?.let { "${it.tidalVolumeLowMl}-${it.tidalVolumeHighMl} mL" } ?: "--")
            ),
            modifier = Modifier.testTag("ibw_result_card")
        ) {
            if (result != null) {
                Button(
                    onClick = { onApplyIbwToCalculators(result.ibwKg) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("ibw_apply_weight_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Use ${MedicMath.formatNumber(result.ibwKg, 1)} kg in Drip & Syringe",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Gender Selector (Simple 2-button tactile cards)
        ClinicalSectionCard(title = "Patient Sex") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PatientSex.entries.forEach { sex ->
                    val isSelected = state.sex == sex
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSexChange(sex) }
                            .testTag("ibw_sex_${sex.name.lowercase()}"),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        },
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 14.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (sex == PatientSex.MALE) Icons.Rounded.Male else Icons.Rounded.Female,
                                contentDescription = sex.label,
                                tint = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = sex.label,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }
        }

        // Height Selector (Simple ft/in or cm + quick steppers)
        ClinicalSectionCard(
            title = "Patient Height",
            trailingContent = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedIconButton(
                        onClick = { onAdjustHeightInches(-1) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("ibw_height_minus_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Remove,
                            contentDescription = "Decrease height by 1 inch"
                        )
                    }
                    OutlinedIconButton(
                        onClick = { onAdjustHeightInches(1) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("ibw_height_plus_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Increase height by 1 inch"
                        )
                    }
                }
            }
        ) {
            SegmentedChoiceRow(
                options = HeightUnit.entries,
                selected = state.heightUnit,
                onSelect = onHeightUnitChange,
                labelProvider = { it.label },
                testTagPrefix = "ibw_height_unit"
            )

            AnimatedVisibility(visible = state.heightUnit == HeightUnit.FT_IN) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MedicNumericField(
                        value = state.feet,
                        onValueChange = onFeetChange,
                        label = "Feet",
                        unitSuffix = "ft",
                        testTag = "ibw_feet_input",
                        modifier = Modifier.weight(1f),
                        imeAction = ImeAction.Next,
                        allowDecimal = false
                    )
                    MedicNumericField(
                        value = state.inches,
                        onValueChange = onInchesChange,
                        label = "Inches",
                        unitSuffix = "in",
                        testTag = "ibw_inches_input",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            AnimatedVisibility(visible = state.heightUnit == HeightUnit.CM) {
                MedicNumericField(
                    value = state.cm,
                    onValueChange = onCmChange,
                    label = "Height",
                    unitSuffix = "cm",
                    testTag = "ibw_cm_input"
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
