package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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

        // Default Drip screen shows 31 gtt/min (500 mL / 4 hr @ 15 gtt/mL)
        composeTestRule.onNodeWithTag("drip_result_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("31").assertIsDisplayed()

        // Switch to O2 Tank tab
        composeTestRule.onNodeWithTag("nav_tab_oxygen").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("oxygen_result_card").assertIsDisplayed()

        // Switch to Syringe tab
        composeTestRule.onNodeWithTag("nav_tab_syringe").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("syringe_result_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("1.5").assertIsDisplayed()

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
}
