package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ApplicationProvider
import com.example.ui.MedicCalcApp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MedicCalc", appName)
    }

    @Test
    fun `navigate through all four paramedic calculators and apply IBW`() {
        composeTestRule.setContent {
            MedicCalcApp()
        }
        composeTestRule.waitForIdle()

        // Default Drip screen starts on Generic (1 mg/mL in 250 mL, 5 mcg/kg/min, 80 kg, 60 gtt/mL = 24 gtt/min)
        composeTestRule.onNodeWithTag("drip_result_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("drip_preset_generic").assertIsDisplayed()
        composeTestRule.onNodeWithText("CUSTOM / GENERIC").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("24").performScrollTo().assertIsDisplayed()

        // Select concentration 4 mg/mL and volume 500 mL on Generic Drip -> 6 gtt/min
        composeTestRule.onNodeWithTag("drip_quick_conc_4_mg").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("drip_bag_vol_500").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Mix Total: 2000 mg in 500 mL").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("6").performScrollTo().assertIsDisplayed()

        // Switch to Volume / Time @ 15 gtt/mL -> 31 gtt/min (500 mL / 4 hr)
        composeTestRule.onNodeWithTag("drip_mode_Volume / Time").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("drip_factor_15").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("31").performScrollTo().assertIsDisplayed()

        // Switch to O2 Tank tab
        composeTestRule.onNodeWithTag("nav_tab_oxygen").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("oxygen_result_card").assertIsDisplayed()

        // Switch to Syringe tab (starts on Generic: 75 mcg from 50 mcg/mL in 2 mL = 1.5 mL)
        composeTestRule.onNodeWithTag("nav_tab_syringe").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("syringe_result_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("syringe_preset_generic").assertIsDisplayed()
        composeTestRule.onNodeWithText("1.5").performScrollTo().assertIsDisplayed()

        // Select concentration 5 mg/mL and 10 mL vial on Generic Syringe -> 50 mg in 10 mL
        composeTestRule.onNodeWithTag("syringe_quick_conc_5_mg").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("syringe_vial_vol_10").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Vial Total: 50 mg in 10 mL").performScrollTo().assertIsDisplayed()

        // Switch to IBW tab and tap Female
        composeTestRule.onNodeWithTag("nav_tab_ibw").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("ibw_result_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ibw_sex_female").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("68.5").performScrollTo().assertIsDisplayed()

        // Switch to GCS tab and score E2 V2 M4 = 8 (Severe)
        composeTestRule.onNodeWithTag("nav_tab_gcs").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("gcs_result_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("gcs_eye_2").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("gcs_verbal_2").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("gcs_motor_4").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("8").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Severe / Coma (3–8)").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `selecting drip and syringe medications displays average range and overdose warnings`() {
        composeTestRule.setContent {
            MedicCalcApp()
        }
        composeTestRule.waitForIdle()

        // Tap Dopamine chip at top of Drip screen
        composeTestRule.onNodeWithTag("drip_preset_dopamine").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("drip_protocol_banner").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Start 5 mcg/kg/min").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("IN PROTOCOL RANGE").performScrollTo().assertIsDisplayed()

        // Enter excessive Dopamine dose (25 mcg/kg/min > 20 max)
        composeTestRule.onNodeWithTag("drip_dose_input").performScrollTo().performTextReplacement("25")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("EXCEEDS PROTOCOL MAX").performScrollTo().assertIsDisplayed()

        // Switch to Syringe screen and tap Fentanyl
        composeTestRule.onNodeWithTag("nav_tab_syringe").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("syringe_preset_fentanyl").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("syringe_protocol_banner").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("0.5–1 mcg/kg (Avg 1 mcg/kg)").performScrollTo().assertIsDisplayed()

        // Set weight to 120 kg so 1 mcg/kg = 120 mcg > 100 mcg max single dose
        composeTestRule.onNodeWithTag("syringe_weight_input").performScrollTo().performTextReplacement("120")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("MAX SINGLE DOSE EXCEEDED").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `settings allows editing ranges, adding custom drug, and removing drugs`() {
        composeTestRule.setContent {
            MedicCalcApp()
        }
        composeTestRule.waitForIdle()

        // Open Settings from TopAppBar
        composeTestRule.onNodeWithTag("action_settings").performClick()
        composeTestRule.waitForIdle()

        // Edit Dopamine normal max from 20 -> 12
        composeTestRule.waitUntil(timeoutMillis = 5000L) {
            composeTestRule.onAllNodesWithTag("settings_edit_dopamine").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("settings_protocol_list")
            .performScrollToNode(hasTestTag("settings_item_dopamine"))
        composeTestRule.onNodeWithTag("settings_edit_dopamine").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("editor_max_dose_input").performScrollTo().performTextReplacement("12")
        composeTestRule.onNodeWithTag("editor_save_button").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000L) {
            composeTestRule.onAllNodesWithText("Range: 2 – 12 mcg/kg/min").fetchSemanticsNodes().isNotEmpty()
        }

        // Add a custom Drip drug: Milrinone
        composeTestRule.onNodeWithTag("settings_add_drug_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("editor_name_input").performScrollTo().performTextReplacement("Milrinone")
        composeTestRule.onNodeWithTag("editor_dose_input").performScrollTo().performTextReplacement("0.375")
        composeTestRule.onNodeWithTag("editor_min_dose_input").performScrollTo().performTextReplacement("0.125")
        composeTestRule.onNodeWithTag("editor_max_dose_input").performScrollTo().performTextReplacement("0.75")
        composeTestRule.onNodeWithTag("editor_bag_drug_input").performScrollTo().performTextReplacement("20")
        composeTestRule.onNodeWithTag("editor_bag_volume_input").performScrollTo().performTextReplacement("100")
        composeTestRule.onNodeWithTag("editor_save_button").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000L) {
            composeTestRule.onAllNodesWithText("Drips (12)").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("settings_protocol_list")
            .performScrollToNode(hasTestTag("settings_item_milrinone"))
        composeTestRule.onNodeWithTag("settings_item_milrinone").assertIsDisplayed()

        // Scroll to Lidocaine and delete it
        composeTestRule.onNodeWithTag("settings_protocol_list")
            .performScrollToNode(hasTestTag("settings_item_lidocaine"))
        composeTestRule.onNodeWithTag("settings_delete_lidocaine").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("confirm_delete_drug").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000L) {
            composeTestRule.onAllNodesWithText("Drips (11)").fetchSemanticsNodes().isNotEmpty()
        }

        // Return to calculators and verify Milrinone is selectable & Lidocaine is gone
        composeTestRule.onNodeWithTag("settings_back_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("drip_preset_lidocaine").assertDoesNotExist()
        composeTestRule.onNodeWithTag("drip_preset_milrinone").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("drip_protocol_banner").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("IN PROTOCOL RANGE").performScrollTo().assertIsDisplayed()

        // Enter dose > 0.75 max to verify custom protocol range warning
        composeTestRule.onNodeWithTag("drip_dose_input").performScrollTo().performTextReplacement("1.0")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("EXCEEDS PROTOCOL MAX").performScrollTo().assertIsDisplayed()
    }
}
