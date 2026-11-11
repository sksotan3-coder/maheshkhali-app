package com.st.sotan.amarmaheshkhali

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Walks through the main screens like a user would and fails on any crash or missing screen. */
@RunWith(AndroidJUnit4::class)
class SmokeTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private fun waitForText(text: String, timeoutMs: Long = 20_000) {
        rule.waitUntil(timeoutMs) {
            rule.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun walkThroughApp() {
        // Home (data is loaded in the background on first launch)
        waitForText("AMAR MOHESHKHALI A TO Z")
        waitForText("টি রেকর্ড")

        // Search tab: type a word and open the first result
        rule.onNodeWithText("সার্চ").performClick()
        waitForText("অফলাইন সার্চ")
        rule.onNodeWithText("নাম বা বিবরণ লিখুন").performTextInput("মহেশখালী")
        waitForText("মহেশখালী")
        rule.onAllNodesWithText("মহেশখালী", substring = true).onFirst().performClick()
        rule.waitForIdle()

        // Settings tab and About screen
        rule.onNodeWithText("সেটিংস").performClick()
        waitForText("ভাষা")
        rule.onNodeWithText("অ্যাপ সম্পর্কে").performClick()
        waitForText("Developed By: ST SOTAN")
        waitForText("© 2026 ST SOTAN")

    }

    @Test
    fun favouritesAndHome() {
        waitForText("AMAR MOHESHKHALI A TO Z")
        rule.onNodeWithText("প্রিয়").performClick()
        waitForText("প্রিয় তথ্য")
        rule.onNodeWithText("হোম").performClick()
        waitForText("সব বিভাগ")
        rule.onNodeWithText("সব বিভাগ").assertIsDisplayed()
    }
}
