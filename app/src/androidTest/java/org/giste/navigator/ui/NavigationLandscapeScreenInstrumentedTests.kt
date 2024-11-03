package org.giste.navigator.ui

import android.Manifest
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import dagger.hilt.android.testing.HiltAndroidTest
import de.mannodermaus.junit5.compose.createAndroidComposeExtension
import de.mannodermaus.junit5.extensions.GrantPermissionExtension
import org.giste.navigator.MainActivity
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension


@HiltAndroidTest
class NavigationLandscapeScreenInstrumentedTests {
    @OptIn(ExperimentalTestApi::class)
    @RegisterExtension
    @JvmField
    val extension = createAndroidComposeExtension<MainActivity>()

    @RegisterExtension
    @JvmField
    val permissions = GrantPermissionExtension.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testPartialButtonsAndKeys() {
        extension.use {
            // Increment by screen button click
            waitForIdle()
            onNodeWithTag(INCREASE_PARTIAL).performClick()
            waitForIdle()
            onNodeWithTag(TRIP_PARTIAL).assertTextEquals("%,.2f".format(0.01f))

            // Increment by keyboard press
            waitForIdle()
            onNodeWithTag(NAVIGATION_LANDSCAPE).performKeyInput {
                pressKey(Key.DirectionRight)
            }
            waitForIdle()
            onNodeWithTag(TRIP_PARTIAL).assertTextEquals("%,.2f".format(0.02f))

            // Decrement by screen button click
            waitForIdle()
            onNodeWithTag(DECREASE_PARTIAL).performClick()
            waitForIdle()
            onNodeWithTag(TRIP_PARTIAL).assertTextEquals("%,.2f".format(0.01f))

            // Decrement by keyboard press
            waitForIdle()
            onNodeWithTag(NAVIGATION_LANDSCAPE).performKeyInput {
                pressKey(Key.DirectionLeft)
            }
            waitForIdle()
            onNodeWithTag(TRIP_PARTIAL).assertTextEquals("%,.2f".format(0f))

            // Reset partial
            waitForIdle()
            onNodeWithTag(INCREASE_PARTIAL).performClick()
            waitForIdle()
            onNodeWithTag(TRIP_PARTIAL).assertTextEquals("%,.2f".format(0.01f))
            waitForIdle()
            onNodeWithTag(RESET_PARTIAL).performClick()
            waitForIdle()
            onNodeWithTag(TRIP_PARTIAL).assertTextEquals("%,.2f".format(0f))
        }
    }
}