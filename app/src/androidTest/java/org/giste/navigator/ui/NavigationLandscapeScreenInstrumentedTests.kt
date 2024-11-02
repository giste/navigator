package org.giste.navigator.ui

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import de.mannodermaus.junit5.compose.createComposeExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class NavigationLandscapeScreenInstrumentedTests {
    @OptIn(ExperimentalTestApi::class)
    @RegisterExtension
    @JvmField
    val extension = createComposeExtension()

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `when right key is pressed partial should be increased by 1`() {
        extension.use {
            var partial = 0

            setContent {
                NavigationLandscapeContent(
                    state = NavigationViewModel.TripState(),
                    roadbookState = NavigationViewModel.RoadbookState.NotLoaded,
                    onEvent = { partial++ }
                )
            }
            waitForIdle()
            onNodeWithTag("NavigationLandscape").performKeyInput {
                pressKey(Key.DirectionRight)
            }

            runOnIdle { assertEquals(1, partial) }
        }
    }
}