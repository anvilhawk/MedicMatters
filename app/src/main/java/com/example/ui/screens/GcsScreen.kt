package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.GcsEye
import com.example.domain.GcsMotor
import com.example.domain.GcsSeverity
import com.example.domain.GcsVerbal
import com.example.ui.GcsUiState
import com.example.ui.components.ClinicalSectionCard
import com.example.ui.components.HeroResultCard
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalClinicalStatusColors

@Composable
fun GcsScreen(
    state: GcsUiState,
    onEyeSelect: (GcsEye) -> Unit,
    onVerbalSelect: (GcsVerbal) -> Unit,
    onMotorSelect: (GcsMotor) -> Unit,
    onResetNormal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val result = state.result
    val statusColors = LocalClinicalStatusColors.current

    val accentColor = when (result?.severity) {
        GcsSeverity.MILD -> statusColors.safe
        GcsSeverity.MODERATE -> statusColors.caution
        GcsSeverity.SEVERE -> statusColors.critical
        null -> MaterialTheme.colorScheme.primary
    }

    val severitySubtitle = result?.let {
        "${it.severity.label} (${it.severity.rangeText})"
    } ?: "Select Eye, Verbal & Motor"

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HeroResultCard(
            label = "Glasgow Coma Scale",
            primaryValue = result?.totalScore?.toString() ?: "--",
            primaryUnit = "/ 15",
            accentColor = accentColor,
            subtitle = severitySubtitle,
            warningText = if (result?.airwayAlert == true) {
                "GCS ≤ 8: Severe impairment — consider definitive airway protection per local protocol."
            } else null,
            metrics = listOf(
                "Eye (1–4)" to (state.eye?.let { "E${it.score}" } ?: "--"),
                "Verbal (1–5)" to (state.verbal?.let { "V${it.score}" } ?: "--"),
                "Motor (1–6)" to (state.motor?.let { "M${it.score}" } ?: "--")
            ),
            modifier = Modifier.testTag("gcs_result_card")
        ) {
            if (result == null || result.totalScore < 15) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onResetNormal,
                        modifier = Modifier.testTag("gcs_reset_normal_button")
                    ) {
                        Text(
                            text = "Reset to Normal (15)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Eye Opening (E: 1-4)
        ClinicalSectionCard(
            title = "Eye Opening (E)",
            trailingContent = {
                ScorePillBadge(scoreText = state.eye?.let { "E${it.score}" } ?: "E--")
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GcsEye.entries.forEach { option ->
                    GcsOptionRow(
                        score = option.score,
                        label = option.label,
                        selected = state.eye == option,
                        onClick = { onEyeSelect(option) },
                        testTag = "gcs_eye_${option.score}"
                    )
                }
            }
        }

        // Verbal Response (V: 1-5)
        ClinicalSectionCard(
            title = "Verbal Response (V)",
            trailingContent = {
                ScorePillBadge(scoreText = state.verbal?.let { "V${it.score}" } ?: "V--")
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GcsVerbal.entries.forEach { option ->
                    GcsOptionRow(
                        score = option.score,
                        label = option.label,
                        selected = state.verbal == option,
                        onClick = { onVerbalSelect(option) },
                        testTag = "gcs_verbal_${option.score}"
                    )
                }
            }
        }

        // Motor Response (M: 1-6)
        ClinicalSectionCard(
            title = "Motor Response (M)",
            trailingContent = {
                ScorePillBadge(scoreText = state.motor?.let { "M${it.score}" } ?: "M--")
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GcsMotor.entries.forEach { option ->
                    GcsOptionRow(
                        score = option.score,
                        label = option.label,
                        selected = state.motor == option,
                        onClick = { onMotorSelect(option) },
                        testTag = "gcs_motor_${option.score}"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ScorePillBadge(scoreText: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = scoreText,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = JetBrainsMonoFontFamily
            ),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun GcsOptionRow(
    score: Int,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        },
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = score.toString(),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = JetBrainsMonoFontFamily
                    ),
                    fontWeight = FontWeight.Bold,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.weight(1f)
            )

            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
