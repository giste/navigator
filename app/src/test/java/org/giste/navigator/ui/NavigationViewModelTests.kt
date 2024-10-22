package org.giste.navigator.ui

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@DisplayName("Tests for NavigationViewModel")
@ExtendWith(MockKExtension::class)
class NavigationViewModelTests {

    private val locationRepository: LocationRepository = mockk()
    private lateinit var viewModel: NavigationViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun beforeEach() {
        //TODO: Rule or subclass???
        Dispatchers.setMain(UnconfinedTestDispatcher())

        coEvery {
            locationRepository.listenToLocation(any(), any())
        } returns EmptyRoute.getLocations().asFlow()

        viewModel = NavigationViewModel(locationRepository)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterEach
    fun afterEach() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @DisplayName("Given minimum partial (0)")
    @Nested
    inner class PartialIsMin {
        @Test
        fun `increase() should add 10 meters`() = runTest {
            viewModel.onEvent(NavigationViewModel.UiEvent.IncreasePartial)

            assertEquals(10, viewModel.uiState.partial)
        }

        @Test
        fun `decrease() should not change partial`() {
            viewModel.onEvent(NavigationViewModel.UiEvent.DecreasePartial)

            assertEquals(0, viewModel.uiState.partial)
        }
    }

    @DisplayName("Given maximum partial (>=999990)")
    @Nested
    inner class PartialIsMax {
        @BeforeEach
        fun setup() {
            viewModel.onEvent(NavigationViewModel.UiEvent.SetPartial("999,99"))
        }

        @Test
        fun `increase() should not change partial`() {
            viewModel.onEvent(NavigationViewModel.UiEvent.IncreasePartial)

            assertEquals(999990, viewModel.uiState.partial)
        }

        @Test
        fun `decrease() should subtract 10 meters`() {
            viewModel.onEvent(NavigationViewModel.UiEvent.DecreasePartial)

            assertEquals(999980, viewModel.uiState.partial)
        }
    }

    @DisplayName("Given partial > 0 and total > 0")
    @Nested
    inner class ResetTests {
        @BeforeEach
        fun setup() {
            viewModel.onEvent(NavigationViewModel.UiEvent.SetPartial("123,45"))
            viewModel.onEvent(NavigationViewModel.UiEvent.SetTotal("9876,54"))
        }

        @Test
        fun `resetPartial() should set partial to 0`() {
            viewModel.onEvent(NavigationViewModel.UiEvent.ResetPartial)

            assertEquals(0, viewModel.uiState.partial)
        }

        @Test
        fun `resetAll() should set partial and total to 0`() {
            viewModel.onEvent(NavigationViewModel.UiEvent.ResetAll)

            assertEquals(0, viewModel.uiState.partial)
            assertEquals(0, viewModel.uiState.total)
        }
    }

    @DisplayName("setPartial()")
    @Nested
    inner class SetPartialTests {
        @Test
        fun `when it's in 0-999 should update partial`() {
            viewModel.onEvent(NavigationViewModel.UiEvent.SetPartial("123,45"))

            assertEquals(123450, viewModel.uiState.partial)
        }

        @Test
        fun `when it's out of range should throw IllegalArgumentException`() {
            assertThrows(IllegalArgumentException::class.java) {
                viewModel.onEvent(NavigationViewModel.UiEvent.SetPartial("1000,00"))
            }
        }
    }

    @Test
    fun `when locations are collected it should calculate distances`() = runTest {
        val locRepository: LocationRepository = mockk()
        coEvery {
            locRepository.listenToLocation(any(), any())
        } returns TestRoute.getLocations().asFlow()
        val viewModel = NavigationViewModel(locRepository)

        assertEquals(TestRoute.getDistance(), viewModel.uiState.partial)
        assertEquals(TestRoute.getDistance(), viewModel.uiState.total)
    }

}