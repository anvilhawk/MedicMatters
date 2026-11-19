package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.DoseRateUnit
import com.example.domain.DripMode
import com.example.domain.DripPreset
import com.example.domain.MedicMath
import com.example.domain.MedicationUnit
import com.example.domain.SyringePreset
import com.example.domain.TimeUnit
import com.example.ui.components.MedicNumericField
import com.example.ui.components.SegmentedChoiceRow
import com.example.ui.components.SelectablePillChip

@Composable
fun DripProtocolEditorDialog(
    initial: DripPreset?,
    onDismiss: () -> Unit,
    onSave: (DripPreset) -> Unit
) {
    var name by remember(initial) { mutableStateOf(initial?.name ?: "") }
    var section by remember(initial) { mutableStateOf(initial?.pcgSection ?: "") }
    var mode by remember(initial) { mutableStateOf(initial?.mode ?: DripMode.MED_INFUSION) }
    var doseValue by remember(initial) { mutableStateOf(initial?.doseValue ?: "5") }
    var doseRateUnit by remember(initial) {
        mutableStateOf(initial?.primaryRateUnit ?: initial?.doseRateUnit ?: DoseRateUnit.MCG_KG_MIN)
    }
    var minDose by remember(initial) {
        mutableStateOf(
            initial?.minRatePrimary?.let { MedicMath.formatNumber(it, 4) }
                ?: if (initial == null) "2" else ""
        )
    }
    var maxDose by remember(initial) {
        mutableStateOf(
            initial?.maxRatePrimary?.let { MedicMath.formatNumber(it, 4) }
                ?: if (initial == null) "20" else ""
        )
    }
    var altMinDose by remember(initial) {
        mutableStateOf(initial?.altMinRate?.let { MedicMath.formatNumber(it, 4) } ?: "")
    }
    var altMaxDose by remember(initial) {
        mutableStateOf(initial?.altMaxRate?.let { MedicMath.formatNumber(it, 4) } ?: "")
    }
    var altRateUnit by remember(initial) { mutableStateOf(initial?.altRateUnit) }
    var drugAmount by remember(initial) { mutableStateOf(initial?.drugAmount ?: "400") }
    var drugUnit by remember(initial) { mutableStateOf(initial?.drugUnit ?: MedicationUnit.MG) }
    var bagVolume by remember(initial) { mutableStateOf(initial?.bagVolumeMl ?: "250") }
    var timeValue by remember(initial) { mutableStateOf(initial?.timeValue ?: "10") }
    var timeUnit by remember(initial) { mutableStateOf(initial?.timeUnit ?: TimeUnit.MINUTES) }
    var dropFactor by remember(initial) { mutableIntStateOf(initial?.dropFactor ?: 60) }
    var notes by remember(initial) { mutableStateOf(initial?.protocolNotes ?: "") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .widthIn(max = 560.dp)
                .heightIn(max = 720.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initial == null) "Add Drip Medication" else "Edit ${initial.name}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close editor")
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; errorMsg = null },
                        label = { Text("Medication Name *") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_name_input")
                    )

                    OutlinedTextField(
                        value = section,
                        onValueChange = { section = it },
                        label = { Text("Protocol Section (optional)") },
                        placeholder = { Text("e.g. Agency PCG 4.1") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_section_input")
                    )

                    SegmentedChoiceRow(
                        options = DripMode.entries,
                        selected = mode,
                        onSelect = { mode = it; errorMsg = null },
                        labelProvider = { it.label },
                        testTagPrefix = "editor_drip_mode"
                    )

                    if (mode == DripMode.MED_INFUSION) {
                        MedicNumericField(
                            value = doseValue,
                            onValueChange = { doseValue = sanitizeDecimalInput(it); errorMsg = null },
                            label = "Average / Starting Dose *",
                            unitSuffix = doseRateUnit.label,
                            testTag = "editor_dose_input",
                            imeAction = ImeAction.Next
                        )

                        Text(
                            text = "Rate Unit",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DoseRateUnit.entries.forEach { unit ->
                                SelectablePillChip(
                                    label = unit.label,
                                    selected = doseRateUnit == unit,
                                    onClick = { doseRateUnit = unit },
                                    testTag = "editor_rate_unit_${unit.name.lowercase()}"
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MedicNumericField(
                                value = minDose,
                                onValueChange = { minDose = sanitizeDecimalInput(it); errorMsg = null },
                                label = "Min Normal",
                                unitSuffix = "",
                                testTag = "editor_min_dose_input",
                                modifier = Modifier.weight(1f),
                                imeAction = ImeAction.Next
                            )
                            MedicNumericField(
                                value = maxDose,
                                onValueChange = { maxDose = sanitizeDecimalInput(it); errorMsg = null },
                                label = "Max Normal",
                                unitSuffix = "",
                                testTag = "editor_max_dose_input",
                                modifier = Modifier.weight(1f),
                                imeAction = ImeAction.Next
                            )
                        }

                        if (altRateUnit != null) {
                            Text(
                                text = "Alternate Range (${altRateUnit?.label})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                MedicNumericField(
                                    value = altMinDose,
                                    onValueChange = { altMinDose = sanitizeDecimalInput(it) },
                                    label = "Alt Min",
                                    unitSuffix = "",
                                    testTag = "editor_alt_min_input",
                                    modifier = Modifier.weight(1f)
                                )
                                MedicNumericField(
                                    value = altMaxDose,
                                    onValueChange = { altMaxDose = sanitizeDecimalInput(it) },
                                    label = "Alt Max",
                                    unitSuffix = "",
                                    testTag = "editor_alt_max_input",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MedicNumericField(
                                value = drugAmount,
                                onValueChange = { drugAmount = sanitizeDecimalInput(it); errorMsg = null },
                                label = "Drug in Bag *",
                                unitSuffix = drugUnit.label,
                                testTag = "editor_bag_drug_input",
                                modifier = Modifier.weight(1f),
                                imeAction = ImeAction.Next
                            )
                            MedicNumericField(
                                value = bagVolume,
                                onValueChange = { bagVolume = sanitizeDecimalInput(it); errorMsg = null },
                                label = "Bag Vol *",
                                unitSuffix = "mL",
                                testTag = "editor_bag_volume_input",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Text(
                            text = "Bag Drug Unit",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    selected = drugUnit == unit,
                                    onClick = { drugUnit = unit },
                                    testTag = "editor_bag_unit_${unit.label.lowercase()}"
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MedicNumericField(
                                value = drugAmount,
                                onValueChange = { drugAmount = sanitizeDecimalInput(it) },
                                label = "Drug Label (opt)",
                                unitSuffix = drugUnit.label,
                                testTag = "editor_bag_drug_input",
                                modifier = Modifier.weight(1f)
                            )
                            MedicNumericField(
                                value = bagVolume,
                                onValueChange = { bagVolume = sanitizeDecimalInput(it); errorMsg = null },
                                label = "Bag Vol *",
                                unitSuffix = "mL",
                                testTag = "editor_bag_volume_input",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MedicNumericField(
                                value = timeValue,
                                onValueChange = { timeValue = sanitizeDecimalInput(it); errorMsg = null },
                                label = "Duration *",
                                unitSuffix = timeUnit.label,
                                testTag = "editor_time_input",
                                modifier = Modifier.weight(1.2f)
                            )
                            SegmentedChoiceRow(
                                options = TimeUnit.entries,
                                selected = timeUnit,
                                onSelect = { timeUnit = it },
                                labelProvider = { it.label },
                                testTagPrefix = "editor_time_unit",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Text(
                        text = "Default Drop Factor",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MedicMath.STANDARD_DROP_FACTORS.forEach { factor ->
                            SelectablePillChip(
                                label = if (factor == 60) "60 (Micro)" else "$factor gtt/mL",
                                selected = dropFactor == factor,
                                onClick = { dropFactor = factor },
                                testTag = "editor_drop_factor_$factor"
                            )
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Protocol Notes (optional)") },
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_notes_input")
                    )

                    if (errorMsg != null) {
                        Text(
                            text = errorMsg!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val trimmedName = name.trim()
                            val bagVolNum = bagVolume.toDoubleOrNull()
                            if (trimmedName.isEmpty()) {
                                errorMsg = "Enter a medication name."
                                return@Button
                            }
                            if (bagVolNum == null || bagVolNum <= 0.0) {
                                errorMsg = "Enter a valid bag volume (> 0 mL)."
                                return@Button
                            }

                            if (mode == DripMode.MED_INFUSION) {
                                val doseNum = doseValue.toDoubleOrNull()
                                val drugNum = drugAmount.toDoubleOrNull()
                                val minNum = minDose.toDoubleOrNull()
                                val maxNum = maxDose.toDoubleOrNull()
                                if (doseNum == null || doseNum <= 0.0) {
                                    errorMsg = "Enter a valid average/starting dose."
                                    return@Button
                                }
                                if (drugNum == null || drugNum <= 0.0) {
                                    errorMsg = "Enter a valid drug amount in bag."
                                    return@Button
                                }
                                if ((minDose.isNotBlank() || maxDose.isNotBlank()) &&
                                    (minNum == null || maxNum == null || minNum < 0.0 || maxNum < minNum)
                                ) {
                                    errorMsg = "Normal min must be ≤ normal max."
                                    return@Button
                                }
                                val altMinNum = altMinDose.toDoubleOrNull()
                                val altMaxNum = altMaxDose.toDoubleOrNull()
                                val validAlt = altRateUnit != null &&
                                    altMinNum != null && altMaxNum != null && altMaxNum >= altMinNum

                                val subtitle = "${MedicMath.formatNumber(drugNum, 2)}${drugUnit.label}/${MedicMath.formatNumber(bagVolNum, 0)}mL"
                                val avgSummary = "Start ${MedicMath.formatNumber(doseNum, 4)} ${doseRateUnit.label}"
                                val hasRange = minNum != null && maxNum != null
                                val rangeText = buildString {
                                    if (hasRange) {
                                        append("${MedicMath.formatNumber(minNum!!, 4)} – ${MedicMath.formatNumber(maxNum!!, 4)} ${doseRateUnit.label}")
                                        if (validAlt) {
                                            append(" (or ${MedicMath.formatNumber(altMinNum!!, 4)} – ${MedicMath.formatNumber(altMaxNum!!, 4)} ${altRateUnit!!.label})")
                                        }
                                    } else {
                                        append("No fixed protocol limits (custom mix)")
                                    }
                                }

                                onSave(
                                    DripPreset(
                                        id = initial?.id ?: 0L,
                                        sortOrder = initial?.sortOrder ?: 0,
                                        name = trimmedName,
                                        subtitle = subtitle,
                                        mode = DripMode.MED_INFUSION,
                                        doseValue = MedicMath.formatNumber(doseNum, 4),
                                        doseRateUnit = doseRateUnit,
                                        drugAmount = MedicMath.formatNumber(drugNum, 4),
                                        drugUnit = drugUnit,
                                        bagVolumeMl = MedicMath.formatNumber(bagVolNum, 2),
                                        dropFactor = dropFactor,
                                        pcgSection = section.trim(),
                                        averageDoseSummary = avgSummary,
                                        normalRangeSummary = rangeText,
                                        minRatePrimary = minNum,
                                        maxRatePrimary = maxNum,
                                        primaryRateUnit = if (hasRange) doseRateUnit else null,
                                        altMinRate = if (validAlt) altMinNum else null,
                                        altMaxRate = if (validAlt) altMaxNum else null,
                                        altRateUnit = if (validAlt) altRateUnit else null,
                                        protocolNotes = notes.trim().ifEmpty { null }
                                    )
                                )
                            } else {
                                val timeNum = timeValue.toDoubleOrNull()
                                if (timeNum == null || timeNum <= 0.0) {
                                    errorMsg = "Enter a valid infusion duration (> 0)."
                                    return@Button
                                }
                                val drugNum = drugAmount.toDoubleOrNull()
                                val subtitle = if (drugNum != null && drugNum > 0.0) {
                                    "${MedicMath.formatNumber(drugNum, 2)}${drugUnit.label}/${MedicMath.formatNumber(bagVolNum, 0)}mL"
                                } else {
                                    "${MedicMath.formatNumber(bagVolNum, 0)}mL/${MedicMath.formatNumber(timeNum, 0)}${timeUnit.label}"
                                }
                                val avgSummary = if (drugNum != null && drugNum > 0.0) {
                                    "${MedicMath.formatNumber(drugNum, 2)} ${drugUnit.label} in ${MedicMath.formatNumber(bagVolNum, 0)} mL over ${MedicMath.formatNumber(timeNum, 0)} ${timeUnit.label}"
                                } else {
                                    "${MedicMath.formatNumber(bagVolNum, 0)} mL over ${MedicMath.formatNumber(timeNum, 0)} ${timeUnit.label}"
                                }
                                onSave(
                                    DripPreset(
                                        id = initial?.id ?: 0L,
                                        sortOrder = initial?.sortOrder ?: 0,
                                        name = trimmedName,
                                        subtitle = subtitle,
                                        mode = DripMode.VOLUME_TIME,
                                        drugAmount = drugNum?.let { MedicMath.formatNumber(it, 2) } ?: "",
                                        drugUnit = drugUnit,
                                        bagVolumeMl = MedicMath.formatNumber(bagVolNum, 2),
                                        timeValue = MedicMath.formatNumber(timeNum, 2),
                                        timeUnit = timeUnit,
                                        dropFactor = dropFactor,
                                        pcgSection = section.trim(),
                                        averageDoseSummary = avgSummary,
                                        normalRangeSummary = "Bolus: $avgSummary",
                                        protocolNotes = notes.trim().ifEmpty { null }
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("editor_save_button")
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun SyringeProtocolEditorDialog(
    initial: SyringePreset?,
    onDismiss: () -> Unit,
    onSave: (SyringePreset) -> Unit
) {
    var name by remember(initial) { mutableStateOf(initial?.name ?: "") }
    var section by remember(initial) { mutableStateOf(initial?.pcgSection ?: "") }
    var isWeightBased by remember(initial) { mutableStateOf(initial?.isWeightBased ?: false) }
    var desiredDose by remember(initial) { mutableStateOf(initial?.desiredDose ?: "1") }
    var desiredUnit by remember(initial) { mutableStateOf(initial?.desiredUnit ?: MedicationUnit.MG) }
    var drugOnHand by remember(initial) { mutableStateOf(initial?.drugOnHand ?: "10") }
    var onHandUnit by remember(initial) { mutableStateOf(initial?.onHandUnit ?: MedicationUnit.MG) }
    var volumeOnHand by remember(initial) { mutableStateOf(initial?.volumeOnHandMl ?: "2") }

    var minDose by remember(initial) {
        val defaultMin = if (initial?.isWeightBased == true) initial.weightMin else initial?.fixedMin
        mutableStateOf(defaultMin?.let { MedicMath.formatNumber(it, 4) } ?: "")
    }
    var maxDose by remember(initial) {
        val defaultMax = if (initial?.isWeightBased == true) initial.weightMax else initial?.fixedMax
        mutableStateOf(defaultMax?.let { MedicMath.formatNumber(it, 4) } ?: "")
    }
    var maxSingleDose by remember(initial) {
        mutableStateOf(initial?.maxSingleDose?.let { MedicMath.formatNumber(it, 4) } ?: "")
    }
    var notes by remember(initial) { mutableStateOf(initial?.protocolNotes ?: "") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .widthIn(max = 560.dp)
                .heightIn(max = 720.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initial == null) "Add Vial / Bolus Drug" else "Edit ${initial.name}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close editor")
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; errorMsg = null },
                        label = { Text("Medication Name *") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_name_input")
                    )

                    OutlinedTextField(
                        value = section,
                        onValueChange = { section = it },
                        label = { Text("Protocol Section (optional)") },
                        placeholder = { Text("e.g. Agency PCG 1.7") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_section_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Weight-Based Dosing (/kg)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Switch(
                            checked = isWeightBased,
                            onCheckedChange = { isWeightBased = it },
                            modifier = Modifier.testTag("editor_weight_switch")
                        )
                    }

                    MedicNumericField(
                        value = desiredDose,
                        onValueChange = { desiredDose = sanitizeDecimalInput(it); errorMsg = null },
                        label = if (isWeightBased) "Average Dose per kg *" else "Average / Starting Dose *",
                        unitSuffix = if (isWeightBased) "${desiredUnit.label}/kg" else desiredUnit.label,
                        testTag = "editor_dose_input",
                        imeAction = ImeAction.Next
                    )

                    Text(
                        text = "Dose Unit",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MedicationUnit.entries.forEach { unit ->
                            SelectablePillChip(
                                label = if (isWeightBased) "${unit.label}/kg" else unit.label,
                                selected = desiredUnit == unit,
                                onClick = { desiredUnit = unit },
                                testTag = "editor_dose_unit_${unit.label.lowercase()}"
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MedicNumericField(
                            value = minDose,
                            onValueChange = { minDose = sanitizeDecimalInput(it); errorMsg = null },
                            label = if (isWeightBased) "Min (${desiredUnit.label}/kg)" else "Min (${desiredUnit.label})",
                            unitSuffix = "",
                            testTag = "editor_min_dose_input",
                            modifier = Modifier.weight(1f),
                            imeAction = ImeAction.Next
                        )
                        MedicNumericField(
                            value = maxDose,
                            onValueChange = { maxDose = sanitizeDecimalInput(it); errorMsg = null },
                            label = if (isWeightBased) "Max (${desiredUnit.label}/kg)" else "Max (${desiredUnit.label})",
                            unitSuffix = "",
                            testTag = "editor_max_dose_input",
                            modifier = Modifier.weight(1f),
                            imeAction = ImeAction.Next
                        )
                    }

                    MedicNumericField(
                        value = maxSingleDose,
                        onValueChange = { maxSingleDose = sanitizeDecimalInput(it); errorMsg = null },
                        label = "Max Single Dose Cap (optional)",
                        unitSuffix = desiredUnit.label,
                        testTag = "editor_max_single_input",
                        imeAction = ImeAction.Next
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MedicNumericField(
                            value = drugOnHand,
                            onValueChange = { drugOnHand = sanitizeDecimalInput(it); errorMsg = null },
                            label = "Drug in Vial *",
                            unitSuffix = onHandUnit.label,
                            testTag = "editor_vial_drug_input",
                            modifier = Modifier.weight(1f),
                            imeAction = ImeAction.Next
                        )
                        MedicNumericField(
                            value = volumeOnHand,
                            onValueChange = { volumeOnHand = sanitizeDecimalInput(it); errorMsg = null },
                            label = "Vial Vol *",
                            unitSuffix = "mL",
                            testTag = "editor_vial_volume_input",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text(
                        text = "Vial Unit",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                selected = onHandUnit == unit,
                                onClick = { onHandUnit = unit },
                                testTag = "editor_vial_unit_${unit.label.lowercase()}"
                            )
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Protocol Notes (optional)") },
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_notes_input")
                    )

                    if (errorMsg != null) {
                        Text(
                            text = errorMsg!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val trimmedName = name.trim()
                            val doseNum = desiredDose.toDoubleOrNull()
                            val vialDrugNum = drugOnHand.toDoubleOrNull()
                            val vialVolNum = volumeOnHand.toDoubleOrNull()
                            val minNum = minDose.toDoubleOrNull()
                            val maxNum = maxDose.toDoubleOrNull()
                            val maxSingleNum = maxSingleDose.toDoubleOrNull()

                            if (trimmedName.isEmpty()) {
                                errorMsg = "Enter a medication name."
                                return@Button
                            }
                            if (doseNum == null || doseNum <= 0.0) {
                                errorMsg = "Enter a valid average/starting dose."
                                return@Button
                            }
                            if (vialDrugNum == null || vialDrugNum <= 0.0 || vialVolNum == null || vialVolNum <= 0.0) {
                                errorMsg = "Enter valid vial drug amount and volume (> 0)."
                                return@Button
                            }
                            if ((minDose.isNotBlank() || maxDose.isNotBlank()) &&
                                (minNum == null || maxNum == null || minNum < 0.0 || maxNum < minNum)
                            ) {
                                errorMsg = "Normal min must be ≤ normal max."
                                return@Button
                            }
                            if (maxSingleDose.isNotBlank() && (maxSingleNum == null || maxSingleNum <= 0.0)) {
                                errorMsg = "Max single dose must be > 0."
                                return@Button
                            }

                            val detail = "${MedicMath.formatNumber(vialDrugNum, 2)}${onHandUnit.label}/${MedicMath.formatNumber(vialVolNum, 2)}mL"
                            val unitLabel = if (isWeightBased) "${desiredUnit.label}/kg" else desiredUnit.label
                            val avgSummary = "Avg ${MedicMath.formatNumber(doseNum, 4)} $unitLabel"
                            val rangeText = buildString {
                                if (minNum != null && maxNum != null) {
                                    if (minNum == maxNum) {
                                        append("${MedicMath.formatNumber(minNum, 4)} $unitLabel")
                                    } else {
                                        append("${MedicMath.formatNumber(minNum, 4)} – ${MedicMath.formatNumber(maxNum, 4)} $unitLabel")
                                    }
                                } else {
                                    append("Custom protocol ($avgSummary)")
                                }
                                if (maxSingleNum != null) {
                                    append(" (Max single ${MedicMath.formatNumber(maxSingleNum, 4)} ${desiredUnit.label})")
                                }
                            }

                            // Preserve secondary range from initial preset if switching modes wasn't requested
                            val keptFixedMin = if (!isWeightBased) minNum else initial?.fixedMin
                            val keptFixedMax = if (!isWeightBased) maxNum else initial?.fixedMax
                            val keptFixedUnit = if (!isWeightBased && minNum != null) desiredUnit else initial?.fixedUnit
                            val keptWeightMin = if (isWeightBased) minNum else initial?.weightMin
                            val keptWeightMax = if (isWeightBased) maxNum else initial?.weightMax
                            val keptWeightUnit = if (isWeightBased && minNum != null) desiredUnit else initial?.weightUnit

                            onSave(
                                SyringePreset(
                                    id = initial?.id ?: 0L,
                                    sortOrder = initial?.sortOrder ?: 0,
                                    name = trimmedName,
                                    detail = detail,
                                    isWeightBased = isWeightBased,
                                    desiredDose = MedicMath.formatNumber(doseNum, 4),
                                    desiredUnit = desiredUnit,
                                    drugOnHand = MedicMath.formatNumber(vialDrugNum, 4),
                                    onHandUnit = onHandUnit,
                                    volumeOnHandMl = MedicMath.formatNumber(vialVolNum, 2),
                                    pcgSection = section.trim(),
                                    averageDoseSummary = avgSummary,
                                    normalRangeSummary = rangeText,
                                    fixedMin = keptFixedMin,
                                    fixedMax = keptFixedMax,
                                    fixedUnit = keptFixedUnit,
                                    weightMin = keptWeightMin,
                                    weightMax = keptWeightMax,
                                    weightUnit = keptWeightUnit,
                                    maxSingleDose = maxSingleNum,
                                    maxSingleUnit = if (maxSingleNum != null) desiredUnit else null,
                                    protocolNotes = notes.trim().ifEmpty { null }
                                )
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("editor_save_button")
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

private fun sanitizeDecimalInput(input: String): String {
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
