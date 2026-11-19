package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.domain.MedicMath
import com.example.domain.VentAlertSeverity
import com.example.domain.VentFinding
import com.example.domain.VentGuideCard
import com.example.domain.VentMath
import com.example.domain.VentMechanicsScenario
import com.example.ui.VentUiState
import com.example.ui.components.ClinicalSectionCard
import com.example.ui.components.HeroResultCard
import com.example.ui.components.MedicNumericField
import com.example.ui.components.SelectablePillChip
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalClinicalStatusColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VentTroubleshootContent(
    state: VentUiState,
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
    onP01Change: (String) -> Unit
) {
    val mech = state.mechanicsResult
    val statusColors = LocalClinicalStatusColors.current

    val accentColor = when (mech.overallSeverity) {
        VentAlertSeverity.NORMAL -> statusColors.safe
        VentAlertSeverity.CAUTION -> statusColors.caution
        VentAlertSeverity.CRITICAL -> statusColors.critical
    }

    val pipPplatText = mech.pipMinusPplat?.let { "${MedicMath.formatNumber(it, 0)}" } ?: "--"
    val minTeText = mech.minTeThreeRcExp?.let { "${MedicMath.formatNumber(it, 1)} s" } ?: "--"
    val actualTeText = mech.actualTeSec?.let { "${MedicMath.formatNumber(it, 1)} s" } ?: "--"
    val maxRrText = mech.maxSafeRr?.let { "$it bpm" } ?: "--"
    val calcCstatText = mech.computedCstat?.let { "${MedicMath.formatNumber(it, 0)}" } ?: "--"
    val pmeanText = mech.computedPmean?.let { "${MedicMath.formatNumber(it, 1)}" } ?: "--"

    // Quick Troubleshoot Scenarios
    ClinicalSectionCard(title = "Quick Ventilator Scenarios") {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VentMath.MECHANICS_SCENARIOS.forEach { scenario ->
                val tagSuffix = scenario.name.lowercase().replace(" ", "_")
                SelectablePillChip(
                    label = scenario.name,
                    subLabel = scenario.subtitle,
                    selected = state.selectedScenarioName == scenario.name,
                    onClick = { onApplyScenario(scenario) },
                    testTag = "vent_scenario_$tagSuffix"
                )
            }
        }
    }

    // Mechanics Summary Hero Card
    HeroResultCard(
        label = "Ventilator Mechanics & Alarm Triage",
        primaryValue = pipPplatText,
        primaryUnit = "cmH2O PIP−Pplat",
        subtitle = if ((mech.pipMinusPplat ?: 0.0) > 5.0) {
            "Gradient > 5 → Airway Resistance"
        } else {
            "Gradient ≤ 5 → Lung/Chest Compliance"
        },
        accentColor = accentColor,
        metrics = listOf(
            "Min TE (3×RC)" to minTeText,
            "Actual TE" to actualTeText,
            "Max Safe RR" to maxRrText
        ),
        modifier = Modifier.testTag("vent_mechanics_result_card")
    ) {
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
                    text = mech.headlineDiagnosis,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Calc Cstat: $calcCstatText mL/cmH2O · Pmean: $pmeanText cmH2O" +
                        (mech.predictedPplat?.let { " · Pred Pplat: ${MedicMath.formatNumber(it, 0)}" } ?: ""),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = JetBrainsMonoFontFamily
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Active Troubleshooting Findings & Actions
    ClinicalSectionCard(
        title = "Bedside Findings & Troubleshooting Steps",
        modifier = Modifier.testTag("vent_findings_card")
    ) {
        mech.findings.forEach { finding ->
            FindingAlertCard(finding = finding)
        }
    }

    // Ventilator Measured Parameters
    ClinicalSectionCard(title = "Enter Ventilator Readings") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MedicNumericField(
                value = state.cstat,
                onValueChange = onCstatChange,
                label = "Cstat",
                unitSuffix = "mL/cm",
                testTag = "vent_cstat_input",
                modifier = Modifier.weight(1f),
                imeAction = ImeAction.Next
            )
            MedicNumericField(
                value = state.rinsp,
                onValueChange = onRinspChange,
                label = "Rinsp",
                unitSuffix = "cm/L/s",
                testTag = "vent_rinsp_input",
                modifier = Modifier.weight(1f),
                imeAction = ImeAction.Next
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MedicNumericField(
                value = state.rcexp,
                onValueChange = onRcexpChange,
                label = "RCexp",
                unitSuffix = "sec",
                testTag = "vent_rcexp_input",
                modifier = Modifier.weight(1f),
                imeAction = ImeAction.Next
            )
            MedicNumericField(
                value = state.autoPeep,
                onValueChange = onAutoPeepChange,
                label = "AutoPEEP",
                unitSuffix = "cmH2O",
                testTag = "vent_autopeep_input",
                modifier = Modifier.weight(1f),
                imeAction = ImeAction.Next
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MedicNumericField(
                value = state.ppeak,
                onValueChange = onPpeakChange,
                label = "Ppeak (PIP)",
                unitSuffix = "cmH2O",
                testTag = "vent_ppeak_input",
                modifier = Modifier.weight(1f),
                imeAction = ImeAction.Next
            )
            MedicNumericField(
                value = state.pplat,
                onValueChange = onPplatChange,
                label = "Pplat (Hold)",
                unitSuffix = "cmH2O",
                testTag = "vent_pplat_input",
                modifier = Modifier.weight(1f),
                imeAction = ImeAction.Next
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MedicNumericField(
                value = state.peep,
                onValueChange = onPeepChange,
                label = "Set PEEP",
                unitSuffix = "cmH2O",
                testTag = "vent_peep_input",
                modifier = Modifier.weight(1f),
                imeAction = ImeAction.Next
            )
            MedicNumericField(
                value = state.vte,
                onValueChange = onVteChange,
                label = "Exhaled VTE",
                unitSuffix = "mL",
                testTag = "vent_vte_input",
                modifier = Modifier.weight(1f),
                imeAction = ImeAction.Next
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MedicNumericField(
                value = state.rr,
                onValueChange = onRrChange,
                label = "Set/Total RR",
                unitSuffix = "bpm",
                testTag = "vent_rr_input",
                modifier = Modifier.weight(1f),
                imeAction = ImeAction.Next
            )
            MedicNumericField(
                value = state.tiSec,
                onValueChange = onTiSecChange,
                label = "I-Time (TI)",
                unitSuffix = "sec",
                testTag = "vent_ti_input",
                modifier = Modifier.weight(1f),
                imeAction = ImeAction.Next
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MedicNumericField(
                value = state.vLeakPercent,
                onValueChange = onVLeakChange,
                label = "VLeak",
                unitSuffix = "%",
                testTag = "vent_vleak_input",
                modifier = Modifier.weight(1f),
                imeAction = ImeAction.Next
            )
            MedicNumericField(
                value = state.p01,
                onValueChange = onP01Change,
                label = "P0.1 Drive",
                unitSuffix = "cmH2O",
                testTag = "vent_p01_input",
                modifier = Modifier.weight(1f),
                imeAction = ImeAction.Done
            )
        }
    }

    // Protocol Reference Guides (DOPES, Cstat, Rinsp, RCexp, Alarms, NIV)
    VentMath.TROUBLESHOOTING_GUIDES.forEachIndexed { index, guide ->
        ExpandableGuideCard(
            guide = guide,
            initiallyExpanded = index == 0,
            testTag = "vent_guide_$index"
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun FindingAlertCard(finding: VentFinding) {
    val statusColors = LocalClinicalStatusColors.current
    val color = when (finding.severity) {
        VentAlertSeverity.NORMAL -> statusColors.safe
        VentAlertSeverity.CAUTION -> statusColors.caution
        VentAlertSeverity.CRITICAL -> statusColors.critical
    }
    val icon = when (finding.severity) {
        VentAlertSeverity.NORMAL -> Icons.Rounded.CheckCircle
        VentAlertSeverity.CAUTION -> Icons.Rounded.WarningAmber
        VentAlertSeverity.CRITICAL -> Icons.Rounded.ErrorOutline
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = finding.headline,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = finding.parameter,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMonoFontFamily
                    ),
                    color = color,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = finding.headline,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Text(
            text = "Causes: ${finding.causes}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Action: ${finding.action}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ExpandableGuideCard(
    guide: VentGuideCard,
    initiallyExpanded: Boolean,
    testTag: String
) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
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
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = guide.badge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMonoFontFamily
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = guide.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    guide.bullets.forEach { bullet ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 7.dp)
                                    .size(5.dp)
                                    .clip(RoundedCornerShape(2.5.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = bullet,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
