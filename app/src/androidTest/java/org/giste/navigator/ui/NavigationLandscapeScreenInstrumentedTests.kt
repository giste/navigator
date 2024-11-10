package org.giste.navigator.ui

import android.Manifest
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import dagger.hilt.android.testing.HiltAndroidTest
import de.mannodermaus.junit5.compose.createAndroidComposeExtension
import de.mannodermaus.junit5.extensions.GrantPermissionExtension
import org.giste.navigator.MainActivity
import org.junit.jupiter.api.BeforeEach
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

    @BeforeEach
    fun beforeEach() {
        extension.use {
            onNodeWithTag(RESET_TRIP).performClick()
        }
    }

    @Test
    fun `when increase button is pressed partial should increment by 1`() {
        extension.use {
            onNodeWithTag(INCREASE_PARTIAL).performClick()
            waitUntilAtLeastOneExists(hasText("%,.2f".format(0.01f)))
            onNodeWithTag(TRIP_PARTIAL).assertTextEquals("%,.2f".format(0.01f))
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `when right key is pressed partial should increment by 1`() {
        extension.use {
            onNodeWithTag(NAVIGATION_CONTENT).performKeyInput {
                pressKey(Key.DirectionRight)
            }
            waitUntilAtLeastOneExists(hasText("%,.2f".format(0.01f)))
            onNodeWithTag(TRIP_PARTIAL).assertTextEquals("%,.2f".format(0.01f))
        }
    }

    @Test
    fun `when decrease button is pressed partial should decrement by 1`() {
        extension.use {
            onNodeWithTag(INCREASE_PARTIAL).performClick()
            onNodeWithTag(INCREASE_PARTIAL).performClick()
            waitUntilAtLeastOneExists(hasText("%,.2f".format(0.02f)))
            onNodeWithTag(TRIP_PARTIAL).assertTextEquals("%,.2f".format(0.02f))

            onNodeWithTag(DECREASE_PARTIAL).performClick()
            waitUntilAtLeastOneExists(hasText("%,.2f".format(0.01f)))
            onNodeWithTag(TRIP_PARTIAL).assertTextEquals("%,.2f".format(0.01f))

        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `when left key is pressed partial should decrement by 1`() {
        extension.use {
            onNodeWithTag(INCREASE_PARTIAL).performClick()
            onNodeWithTag(INCREASE_PARTIAL).performClick()
            waitUntilAtLeastOneExists(hasText("%,.2f".format(0.02f)))
            onNodeWithTag(TRIP_PARTIAL).assertTextEquals("%,.2f".format(0.02f))

            onNodeWithTag(NAVIGATION_CONTENT).performKeyInput {
                pressKey(Key.DirectionLeft)
            }
            waitUntilAtLeastOneExists(hasText("%,.2f".format(0.01f)))
            onNodeWithTag(TRIP_PARTIAL).assertTextEquals("%,.2f".format(0.01f))
        }
    }

    @Test
    fun `when reset button is pressed partial should be set to zero`() {
        extension.use {
            onNodeWithTag(INCREASE_PARTIAL).performClick()
            waitUntilAtLeastOneExists(hasText("%,.2f".format(0.01f)))
            onNodeWithTag(TRIP_PARTIAL).assertTextEquals("%,.2f".format(0.01f))

            onNodeWithTag(RESET_PARTIAL).performClick()
            waitUntilDoesNotExist(hasText("%,.2f".format(0.01f)))
            onNodeWithTag(TRIP_PARTIAL).assertTextEquals("%,.2f".format(0.0f))
        }
    }
}