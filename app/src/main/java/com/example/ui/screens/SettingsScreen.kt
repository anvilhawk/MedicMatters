package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.DripPreset
import com.example.domain.SyringePreset
import com.example.ui.components.ClinicalSectionCard
import com.example.ui.components.SegmentedChoiceRow
import com.example.ui.theme.JetBrainsMonoFontFamily

enum class SettingsProtocolCategory(val label: String) {
    DRIPS("Drip Infusions"),
    SYRINGE("Syringe / Vials")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    dripPresets: List<DripPreset>,
    syringePresets: List<SyringePreset>,
    initialCategory: SettingsProtocolCategory,
    onSaveDrip: (DripPreset) -> Unit,
    onDeleteDrip: (Long) -> Unit,
    onSaveSyringe: (SyringePreset) -> Unit,
    onDeleteSyringe: (Long) -> Unit,
    onResetDefaults: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var selectedCategory by remember(initialCategory) { mutableStateOf(initialCategory) }
    var editingDrip by remember { mutableStateOf<DripPreset?>(null) }
    var showAddDrip by remember { mutableStateOf(false) }
    var editingSyringe by remember { mutableStateOf<SyringePreset?>(null) }
    var showAddSyringe by remember { mutableStateOf(false) }
    var pendingDeleteDrip by remember { mutableStateOf<DripPreset?>(null) }
    var pendingDeleteSyringe by remember { mutableStateOf<SyringePreset?>(null) }
    var showResetConfirm by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.settings_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Customize medications & dose ranges",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back to calculators"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showResetConfirm = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("settings_reset_defaults_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RestartAlt,
                            contentDescription = "Reset to Air Methods PCG defaults"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SegmentedChoiceRow(
                    options = SettingsProtocolCategory.entries,
                    selected = selectedCategory,
                    onSelect = { selectedCategory = it },
                    labelProvider = { cat ->
                        when (cat) {
                            SettingsProtocolCategory.DRIPS -> "Drips (${dripPresets.size})"
                            SettingsProtocolCategory.SYRINGE -> "Syringe (${syringePresets.size})"
                        }
                    },
                    testTagPrefix = "settings_category"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (selectedCategory == SettingsProtocolCategory.DRIPS) {
                                showAddDrip = true
                            } else {
                                showAddSyringe = true
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("settings_add_drug_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = if (selectedCategory == SettingsProtocolCategory.DRIPS) {
                                "Add Drip Drug"
                            } else {
                                "Add Vial Drug"
                            }
                        )
                    }
                    OutlinedButton(
                        onClick = { showResetConfirm = true },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text("Reset")
                    }
                }

                if (selectedCategory == SettingsProtocolCategory.DRIPS) {
                    if (dripPresets.isEmpty()) {
                        EmptyProtocolCard(
                            message = "No drip medications configured. Tap Add Drip Drug or Reset to restore defaults."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("settings_protocol_list"),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(dripPresets, key = { "drip_${it.id}_${it.name}" }) { preset ->
                                ProtocolListItemCard(
                                    name = preset.name,
                                    mixSummary = preset.subtitle,
                                    section = preset.pcgSection,
                                    averageDose = preset.averageDoseSummary,
                                    normalRange = preset.normalRangeSummary,
                                    onEdit = { editingDrip = preset },
                                    onDelete = { pendingDeleteDrip = preset }
                                )
                            }
                        }
                    }
                } else {
                    if (syringePresets.isEmpty()) {
                        EmptyProtocolCard(
                            message = "No syringe medications configured. Tap Add Vial Drug or Reset to restore defaults."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("settings_protocol_list"),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(syringePresets, key = { "syringe_${it.id}_${it.name}" }) { preset ->
                                ProtocolListItemCard(
                                    name = preset.name,
                                    mixSummary = preset.detail,
                                    section = preset.pcgSection,
                                    averageDose = preset.averageDoseSummary,
                                    normalRange = preset.normalRangeSummary,
                                    onEdit = { editingSyringe = preset },
                                    onDelete = { pendingDeleteSyringe = preset }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDrip || editingDrip != null) {
        DripProtocolEditorDialog(
            initial = editingDrip,
            onDismiss = {
                showAddDrip = false
                editingDrip = null
            },
            onSave = { updated ->
                onSaveDrip(updated)
                showAddDrip = false
                editingDrip = null
            }
        )
    }

    if (showAddSyringe || editingSyringe != null) {
        SyringeProtocolEditorDialog(
            initial = editingSyringe,
            onDismiss = {
                showAddSyringe = false
                editingSyringe = null
            },
            onSave = { updated ->
                onSaveSyringe(updated)
                showAddSyringe = false
                editingSyringe = null
            }
        )
    }

    pendingDeleteDrip?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDeleteDrip = null },
            title = { Text("Remove ${target.name}?") },
            text = { Text("This removes ${target.name} (${target.subtitle}) from your Drip protocol chips. You can re-add it or restore defaults anytime.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteDrip(target.id)
                        pendingDeleteDrip = null
                    },
                    modifier = Modifier.testTag("confirm_delete_drug")
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteDrip = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    pendingDeleteSyringe?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDeleteSyringe = null },
            title = { Text("Remove ${target.name}?") },
            text = { Text("This removes ${target.name} (${target.detail}) from your Syringe protocol chips. You can re-add it or restore defaults anytime.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSyringe(target.id)
                        pendingDeleteSyringe = null
                    },
                    modifier = Modifier.testTag("confirm_delete_drug")
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteSyringe = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Restore Default Protocols?") },
            text = {
                Text("This will replace all custom edits and additions with the original Air Methods PCG Rev 10 drip and vial presets.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetDefaults()
                        showResetConfirm = false
                    },
                    modifier = Modifier.testTag("confirm_reset_defaults")
                ) {
                    Text("Restore Defaults")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ProtocolListItemCard(
    name: String,
    mixSummary: String,
    section: String,
    averageDose: String,
    normalRange: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("settings_item_${name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = mixSummary,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMonoFontFamily
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("settings_edit_${name.lowercase()}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit $name",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("settings_delete_${name.lowercase()}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Remove $name",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (section.isNotBlank()) {
                Text(
                    text = section,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (averageDose.isNotBlank()) {
                Text(
                    text = "Avg: $averageDose",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = JetBrainsMonoFontFamily
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (normalRange.isNotBlank()) {
                Text(
                    text = "Range: $normalRange",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = JetBrainsMonoFontFamily
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun EmptyProtocolCard(message: String) {
    ClinicalSectionCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Medication,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}
