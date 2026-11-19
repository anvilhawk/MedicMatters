package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Vaccines
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Vaccines
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.domain.MedicMath
import com.example.domain.MedicTab
import com.example.domain.VentSubMode
import com.example.ui.screens.AbgScreen
import com.example.ui.screens.DripScreen
import com.example.ui.screens.GcsScreen
import com.example.ui.screens.IbwScreen
import com.example.ui.screens.OxygenScreen
import com.example.ui.screens.SettingsProtocolCategory
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SyringeScreen
import com.example.ui.screens.VentScreen
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MedicCalcTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicCalcApp(
    viewModel: MedicCalcViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val mainScrollState = rememberScrollState()

    LaunchedEffect(uiState.activeTab) {
        mainScrollState.scrollTo(0)
    }

    MedicCalcTheme(darkTheme = uiState.isNightMode) {
        if (uiState.showSettings) {
            SettingsScreen(
                dripPresets = uiState.drip.presets,
                syringePresets = uiState.syringe.presets,
                initialCategory = if (uiState.activeTab == MedicTab.SYRINGE) {
                    SettingsProtocolCategory.SYRINGE
                } else {
                    SettingsProtocolCategory.DRIPS
                },
                onSaveDrip = viewModel::saveDripProtocol,
                onDeleteDrip = viewModel::deleteDripProtocol,
                onSaveSyringe = viewModel::saveSyringeProtocol,
                onDeleteSyringe = viewModel::deleteSyringeProtocol,
                onResetDefaults = viewModel::resetProtocolsToDefaults,
                onBack = { viewModel.setShowSettings(false) }
            )
        } else {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useNavigationRail = maxWidth >= 600.dp

                Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets.safeDrawing,
                containerColor = MaterialTheme.colorScheme.background,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.LocalHospital,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = stringResource(R.string.app_name),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "EMS Field Reference",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { viewModel.clearActiveTab() },
                                modifier = Modifier.testTag("action_clear_tab")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Backspace,
                                    contentDescription = stringResource(R.string.action_clear)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.toggleNightMode() },
                                modifier = Modifier.testTag("action_toggle_night_mode")
                            ) {
                                Icon(
                                    imageVector = if (uiState.isNightMode) {
                                        Icons.Outlined.LightMode
                                    } else {
                                        Icons.Outlined.DarkMode
                                    },
                                    contentDescription = stringResource(R.string.toggle_night_mode)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.setShowSettings(true) },
                                modifier = Modifier.testTag("action_settings")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Settings,
                                    contentDescription = stringResource(R.string.open_protocol_settings)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.setShowFormulaDialog(true) },
                                modifier = Modifier.testTag("action_clinical_info")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = stringResource(R.string.open_clinical_info)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                },
                bottomBar = {
                    if (!useNavigationRail) {
                        ScrollableMedicBottomBar(
                            activeTab = uiState.activeTab,
                            onSelectTab = viewModel::selectTab
                        )
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useNavigationRail) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Spacer(modifier = Modifier.size(12.dp))
                            MedicTab.entries.forEach { tab ->
                                val selected = uiState.activeTab == tab
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { viewModel.selectTab(tab) },
                                    icon = {
                                        Icon(
                                            imageVector = tabIcon(tab, selected),
                                            contentDescription = tab.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1
                                        )
                                    },
                                    modifier = Modifier.testTag("rail_tab_${tab.name.lowercase()}")
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(mainScrollState),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 640.dp)
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            when (uiState.activeTab) {
                                MedicTab.DRIP -> DripScreen(
                                    state = uiState.drip,
                                    onModeChange = viewModel::setDripMode,
                                    onVolumeChange = viewModel::setDripVolume,
                                    onTimeChange = viewModel::setDripTime,
                                    onTimeUnitChange = viewModel::setDripTimeUnit,
                                    onDropFactorSelect = viewModel::setDropFactor,
                                    onEnableCustomDropFactor = viewModel::enableCustomDropFactor,
                                    onCustomDropFactorChange = viewModel::setCustomDropFactor,
                                    onWeightChange = viewModel::setDripWeight,
                                    onWeightUnitChange = viewModel::setDripWeightUnit,
                                    onDesiredDoseChange = viewModel::setDripDesiredDose,
                                    onDoseRateUnitChange = viewModel::setDripDoseRateUnit,
                                    onConcentrationModeChange = viewModel::setDripConcentrationMode,
                                    onConcentrationPerMlChange = viewModel::setDripConcentrationPerMl,
                                    onConcentrationUnitChange = viewModel::setDripConcentrationUnit,
                                    onQuickConcentrationSelect = viewModel::selectDripQuickConcentration,
                                    onDrugAmountChange = viewModel::setDripDrugAmount,
                                    onDrugUnitChange = viewModel::setDripDrugUnit,
                                    onBagVolumeChange = viewModel::setDripBagVolume,
                                    onApplyPreset = viewModel::applyDripPreset,
                                    onClearPreset = viewModel::clearDripPresetSelection,
                                    onOpenSettings = { viewModel.setShowSettings(true) },
                                    onToggleMetronome = viewModel::toggleDripMetronome,
                                    onTapDrip = { viewModel.recordDripTap() },
                                    onResetTap = viewModel::resetDripTap
                                )

                                MedicTab.OXYGEN -> OxygenScreen(
                                    state = uiState.oxygen,
                                    onCylinderSelect = viewModel::setOxygenCylinder,
                                    onPsiChange = viewModel::setOxygenPsi,
                                    onPsiSliderChange = viewModel::setOxygenPsiSlider,
                                    onFlowRateChange = viewModel::setOxygenFlowRate,
                                    onToggleSafeResidual = viewModel::toggleOxygenSafeResidual
                                )

                                MedicTab.SYRINGE -> SyringeScreen(
                                    state = uiState.syringe,
                                    onWeightBasedChange = viewModel::setSyringeWeightBased,
                                    onWeightChange = viewModel::setSyringeWeight,
                                    onWeightUnitChange = viewModel::setSyringeWeightUnit,
                                    onDesiredDoseChange = viewModel::setSyringeDesiredDose,
                                    onDesiredUnitChange = viewModel::setSyringeDesiredUnit,
                                    onConcentrationModeChange = viewModel::setSyringeConcentrationMode,
                                    onConcentrationPerMlChange = viewModel::setSyringeConcentrationPerMl,
                                    onConcentrationUnitChange = viewModel::setSyringeConcentrationUnit,
                                    onQuickConcentrationSelect = viewModel::selectSyringeQuickConcentration,
                                    onDrugOnHandChange = viewModel::setSyringeDrugOnHand,
                                    onOnHandUnitChange = viewModel::setSyringeOnHandUnit,
                                    onVolumeOnHandChange = viewModel::setSyringeVolumeOnHand,
                                    onApplyPreset = viewModel::applySyringePreset,
                                    onClearPreset = viewModel::clearSyringePresetSelection,
                                    onOpenSettings = { viewModel.setShowSettings(true) }
                                )

                                MedicTab.IBW -> IbwScreen(
                                    state = uiState.ibw,
                                    onSexChange = viewModel::setIbwSex,
                                    onHeightUnitChange = viewModel::setIbwHeightUnit,
                                    onFeetChange = viewModel::setIbwFeet,
                                    onInchesChange = viewModel::setIbwInches,
                                    onCmChange = viewModel::setIbwCm,
                                    onAdjustHeightInches = viewModel::adjustIbwHeightInches,
                                    onApplyIbwToCalculators = { ibwKg ->
                                        viewModel.applyIbwToCalculators(ibwKg)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                "Applied ${MedicMath.formatNumber(ibwKg, 1)} kg IBW to Drip & Syringe calculators"
                                            )
                                        }
                                    }
                                )

                                MedicTab.GCS -> GcsScreen(
                                    state = uiState.gcs,
                                    onEyeSelect = viewModel::setGcsEye,
                                    onVerbalSelect = viewModel::setGcsVerbal,
                                    onMotorSelect = viewModel::setGcsMotor,
                                    onResetNormal = viewModel::resetGcsNormal
                                )

                                MedicTab.ABG -> AbgScreen(
                                    state = uiState.abg,
                                    onSampleTypeChange = viewModel::setAbgSampleType,
                                    onApplyPreset = viewModel::applyAbgPreset,
                                    onPhChange = viewModel::setAbgPh,
                                    onPaco2Change = viewModel::setAbgPaco2,
                                    onHco3Change = viewModel::setAbgHco3,
                                    onPao2Change = viewModel::setAbgPao2,
                                    onFio2Change = viewModel::setAbgFio2,
                                    onNaChange = viewModel::setAbgNa,
                                    onClChange = viewModel::setAbgCl,
                                    onOpenVentStrategy = { strategy ->
                                        viewModel.setVentSubMode(VentSubMode.SETUP)
                                        viewModel.setVentStrategy(strategy)
                                        viewModel.selectTab(MedicTab.VENT)
                                    }
                                )

                                MedicTab.VENT -> VentScreen(
                                    state = uiState.vent,
                                    onSubModeChange = viewModel::setVentSubMode,
                                    onStrategyChange = viewModel::setVentStrategy,
                                    onUseDirectWeightChange = viewModel::setVentUseDirectWeight,
                                    onSexChange = viewModel::setVentSex,
                                    onHeightUnitChange = viewModel::setVentHeightUnit,
                                    onFeetChange = viewModel::setVentFeet,
                                    onInchesChange = viewModel::setVentInches,
                                    onCmChange = viewModel::setVentCm,
                                    onDirectWeightChange = viewModel::setVentDirectWeightKg,
                                    onTargetVtMlPerKgChange = viewModel::setVentTargetVtMlPerKg,
                                    onSyncFromIbw = {
                                        viewModel.syncVentFromIbwTab()
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Synced height & sex from IBW tab")
                                        }
                                    },
                                    onApplyScenario = viewModel::applyVentMechanicsScenario,
                                    onCstatChange = viewModel::setVentCstat,
                                    onRinspChange = viewModel::setVentRinsp,
                                    onRcexpChange = viewModel::setVentRcexp,
                                    onPpeakChange = viewModel::setVentPpeak,
                                    onPplatChange = viewModel::setVentPplat,
                                    onPeepChange = viewModel::setVentPeep,
                                    onVteChange = viewModel::setVentVte,
                                    onAutoPeepChange = viewModel::setVentAutoPeep,
                                    onVLeakChange = viewModel::setVentVLeak,
                                    onRrChange = viewModel::setVentRr,
                                    onTiSecChange = viewModel::setVentTiSec,
                                    onP01Change = viewModel::setVentP01
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.showFormulaDialog) {
                ClinicalFormulaDialog(
                    onDismiss = { viewModel.setShowFormulaDialog(false) }
                )
            }
        }
        }
    }
}

@Composable
private fun ScrollableMedicBottomBar(
    activeTab: MedicTab,
    onSelectTab: (MedicTab) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MedicTab.entries.forEach { tab ->
                val selected = activeTab == tab
                Surface(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .widthIn(min = 66.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectTab(tab) }
                        .testTag("nav_tab_${tab.name.lowercase()}"),
                    shape = RoundedCornerShape(14.dp),
                    color = if (selected) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        Color.Transparent
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = tabIcon(tab, selected),
                            contentDescription = tab.label,
                            tint = if (selected) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

private fun tabIcon(tab: MedicTab, selected: Boolean): ImageVector = when (tab) {
    MedicTab.DRIP -> if (selected) Icons.Rounded.WaterDrop else Icons.Outlined.WaterDrop
    MedicTab.OXYGEN -> if (selected) Icons.Rounded.Air else Icons.Outlined.Air
    MedicTab.SYRINGE -> if (selected) Icons.Rounded.Vaccines else Icons.Outlined.Vaccines
    MedicTab.IBW -> if (selected) Icons.Rounded.MonitorWeight else Icons.Outlined.MonitorWeight
    MedicTab.GCS -> if (selected) Icons.Rounded.Psychology else Icons.Outlined.Psychology
    MedicTab.ABG -> if (selected) Icons.Rounded.Science else Icons.Outlined.Science
    MedicTab.VENT -> if (selected) Icons.Rounded.MonitorHeart else Icons.Outlined.MonitorHeart
}

@Composable
private fun ClinicalFormulaDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.disclaimer_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.disclaimer_body),
                    style = MaterialTheme.typography.bodyMedium
                )
                FormulaLine("Drip Rate", "gtt/min = (mL × gtt/mL) ÷ min")
                FormulaLine("O2 Tank", "min = ((PSI − 200) × Factor) ÷ L/min")
                FormulaLine("Syringe", "mL = Desired Dose ÷ (Drug ÷ mL)")
                FormulaLine("Devine IBW", "M: 50 + 2.3×(in−60) | F: 45.5 + 2.3×(in−60)")
                FormulaLine("GCS Score", "E(1–4) + V(1–5) + M(1–6) | Mild ≥13, Mod 9–12, Sev ≤8")
                FormulaLine("ABG / Gap", "Winter's: PaCO2 = 1.5×HCO3 + 8 ± 2 | AG = Na − Cl − HCO3")
                FormulaLine("Vent Setup", "RR = VE ÷ VT | VE 100 (Norm), 120 (Acid), 150/200 (Peds)")
                FormulaLine("Vent Mechanics", "Cstat = VTE÷(Pplat−PEEP) | Min TE = 3×RCexp | PIP−Pplat > 5")
                FormulaLine("Protocol Ref", "Air Methods PCG Rev 10 (11/03/2023) Adult & Peds")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_got_it))
            }
        }
    )
}

@Composable
private fun FormulaLine(title: String, formula: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = formula,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = JetBrainsMonoFontFamily
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
