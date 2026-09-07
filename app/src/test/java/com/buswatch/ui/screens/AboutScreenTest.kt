// ABOUTME: Compose tests for the About screen rendered under Robolectric
// ABOUTME: Verifies the version, both legal addresses, and the Back action
package com.buswatch.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.buswatch.LegalLinks
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AboutScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `shows version and both legal addresses`() {
        composeRule.setContent { AboutScreen(versionName = "1.2.3", onBack = {}) }

        composeRule.onNodeWithText("BusWatch 1.2.3").assertIsDisplayed()
        composeRule.onNodeWithText(LegalLinks.PRIVACY_POLICY_URL, substring = true).assertIsDisplayed()
        composeRule.onNodeWithText(LegalLinks.TERMS_OF_SERVICE_URL, substring = true).assertIsDisplayed()
    }

    @Test
    fun `back chip calls onBack`() {
        var backCalled = false
        composeRule.setContent { AboutScreen(versionName = "1.0.0", onBack = { backCalled = true }) }

        // The chip is the last item of a ScalingLazyColumn, so scroll it fully into view
        // before clicking; edge items are scaled down and can miss the hit test.
        composeRule.onNodeWithText("Back").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertTrue(backCalled)
    }
}
