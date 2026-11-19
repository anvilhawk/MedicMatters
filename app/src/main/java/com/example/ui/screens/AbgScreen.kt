package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.domain.AbgMath
import com.example.domain.AbgPreset
import com.example.domain.AbgSampleType
import com.example.domain.AbgSeverity
import com.example.domain.MedicMath
import com.example.domain.VentStrategy
import com.example.ui.AbgUiState
import com.example.ui.components.ClinicalSectionCard
import com.example.ui.components.HeroResultCard
import com.example.ui.components.MedicNumericField
import com.example.ui.components.SegmentedChoiceRow
import com.example.ui.components.SelectablePillChip
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalClinicalStatusColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AbgScreen(
    state: AbgUiState,
    onSampleTypeChange: (AbgSampleType) -> Unit,
    onApplyPreset: (AbgPreset) -> Unit,
    onPhChange: (String) -> Unit,
    onPaco2Change: (String) -> Unit,
    onHco3Change: (String) -> Unit,
    onPao2Change: (String) -> Unit,
    onFio2Change: (String) -> Unit,
    onNaChange: (String) -> Unit,
    onClChange: (String) -> Unit,
    onOpenVentStrategy: (VentStrategy) -> Unit,
    modifier: Modifier = Modifier
) {
    val result = state.result
    val statusColors = LocalClinicalStatusColors.current

    val accentColor = when (result?.severity) {
        AbgSeverity.NORMAL -> statusColors.safe
        AbgSeverity.CAUTION -> statusColors.caution
        AbgSeverity.CRITICAL -> statusColors.critical
        null -> MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Quick Clinical Scenarios
        ClinicalSectionCard(title = "Quick Blood Gas Scenarios") {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AbgMath.PRESETS.forEach { preset ->
                    val tagSuffix = preset.name.lowercase()
                        .replace(" / ", "_")
                        .replace(" ", "_")
                        .replace("/", "")
                    SelectablePillChip(
                        label = preset.name,
                        subLabel = preset.detail,
                        selected = state.selectedPresetName == preset.name,
                        onClick = { onApplyPreset(preset) },
                        testTag = "abg_preset_$tagSuffix"
                    )
                }
            }
        }

        // Hero Result Card
        HeroResultCard(
            label = "${state.sampleType.label} Interpretation",
            primaryValue = state.ph.ifBlank { "--" },
            primaryUnit = "pH",
            subtitle = result?.pcgReference ?: "Enter pH, PCO2 & HCO3⁻",
            accentColor = accentColor,
            warningText = if (result?.metAcidosisVentAlert == true) {
                "PCG 7.33.5.2 Trigger (pH < 7.25 or AG > 16): Target ETCO2 25–30 mmHg (never < 25), VE 120 mL/kg/min, avoid APV/PRVC."
            } else null,
            isCriticalWarning = result?.metAcidosisVentAlert == true,
            metrics = listOf(
                "pH Status" to (result?.phStatusText?.substringBefore(" (") ?: "--"),
                "CO2" to (state.paco2.takeIf { it.isNotBlank() }?.let { "$it mmHg" } ?: "--"),
                "HCO3⁻" to (state.hco3.takeIf { it.isNotBlank() }?.let { "$it mEq/L" } ?: "--")
            ),
            modifier = Modifier.testTag("abg_result_card")
        ) {
            if (result != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = accentColor.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = result.primaryDisorder,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = result.compensationStatus,
                            style = MaterialTheme.typography.labelLarge,
                            color = accentColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Sample Type & Blood Gas Input Fields
        ClinicalSectionCard(title = "Blood Gas Values") {
            SegmentedChoiceRow(
                options = AbgSampleType.entries,
                selected = state.sampleType,
                onSelect = onSampleTypeChange,
                labelProvider = { it.label },
                testTagPrefix = "abg_sample"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MedicNumericField(
                    value = state.ph,
                    onValueChange = onPhChange,
                    label = "pH",
                    unitSuffix = "",
                    testTag = "abg_ph_input",
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Next
                )
                MedicNumericField(
                    value = state.paco2,
                    onValueChange = onPaco2Change,
                    label = "PaCO2",
                    unitSuffix = "mmHg",
                    testTag = "abg_paco2_input",
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Next
                )
                MedicNumericField(
                    value = state.hco3,
                    onValueChange = onHco3Change,
                    label = "HCO3⁻",
                    unitSuffix = "mEq",
                    testTag = "abg_hco3_input",
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Next
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MedicNumericField(
                    value = state.pao2,
                    onValueChange = onPao2Change,
                    label = "PaO2 (Opt)",
                    unitSuffix = "mmHg",
                    testTag = "abg_pao2_input",
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Next
                )
                MedicNumericField(
                    value = state.fio2Percent,
                    onValueChange = onFio2Change,
                    label = "FiO2 (Opt)",
                    unitSuffix = "%",
                    testTag = "abg_fio2_input",
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Next
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MedicNumericField(
                    value = state.na,
                    onValueChange = onNaChange,
                    label = "Na⁺ (For AG)",
                    unitSuffix = "mEq",
                    testTag = "abg_na_input",
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Next
                )
                MedicNumericField(
                    value = state.cl,
                    onValueChange = onClChange,
                    label = "Cl⁻ (For AG)",
                    unitSuffix = "mEq",
                    testTag = "abg_cl_input",
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Done
                )
            }
        }

        if (result != null) {
            // Compensation, Anion Gap, and Oxygenation Breakdown
            ClinicalSectionCard(
                title = "Compensation, Gap & Oxygenation",
                modifier = Modifier.testTag("abg_analysis_card")
            ) {
                AbgDetailRow("Compensation", result.expectedCompensationDetail)
                AbgDetailRow(
                    "Parameter Check",
                    "pH: ${result.phStatusText} · CO2: ${result.paco2StatusText} · HCO3⁻: ${result.hco3StatusText}"
                )
                if (result.anionGapText != null) {
                    AbgDetailRow("Anion Gap", result.anionGapText)
                }
                if (result.deltaDeltaText != null) {
                    AbgDetailRow("Delta-Delta", result.deltaDeltaText)
                }
                AbgDetailRow("Oxygenation", result.oxygenationText)
            }

            // Likely Causes Card
            ClinicalSectionCard(
                title = "Likely Causes of This Pattern",
                modifier = Modifier.testTag("abg_causes_card")
            ) {
                result.likelyCauses.forEach { cause ->
                    AbgBulletRow(text = cause)
                }
            }

            // Protocol Actions & Quick Link to Vent Strategy
            ClinicalSectionCard(
                title = "Clinical & Vent Guidance",
                modifier = Modifier.testTag("abg_actions_card")
            ) {
                result.fieldActions.forEach { action ->
                    AbgBulletRow(text = action)
                }

                val suggestedStrategy = when {
                    result.metAcidosisVentAlert -> VentStrategy.METABOLIC_ACIDOSIS
                    result.pfRatio != null && result.pfRatio < 200.0 -> VentStrategy.ARDS
                    result.primaryDisorder.contains("Respiratory Acidosis") -> VentStrategy.OBSTRUCTIVE
                    else -> null
                }

                if (suggestedStrategy != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { onOpenVentStrategy(suggestedStrategy) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("abg_open_vent_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Air,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Open ${suggestedStrategy.shortLabel} Vent Calculator",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = { onOpenVentStrategy(VentStrategy.NORMAL) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("abg_open_vent_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Open Ventilator Management",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Compact PCG Reference Ranges Footer
        ClinicalSectionCard(title = "Air Methods PCG Table 4.20 Reference") {
            Text(
                text = "ART: pH 7.35–7.46 | PCO2 36–46 | HCO3 23–29 | PO2 74–109\n" +
                    "VEN: pH 7.33–7.40 | PCO2 40–52 | HCO3 25–30 | PO2 25–44\n" +
                    "Anion Gap = Na − Cl − HCO3 (Normal < 14; Vent trigger > 16)",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun AbgDetailRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AbgBulletRow(text: String) {
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
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}
