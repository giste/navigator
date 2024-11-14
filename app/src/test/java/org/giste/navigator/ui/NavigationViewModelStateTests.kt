package org.giste.navigator.ui

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.giste.navigator.model.LocationRepository
import org.giste.navigator.model.MapRepository
import org.giste.navigator.model.RoadbookRepository
import org.giste.navigator.model.RoadbookScroll
import org.giste.navigator.model.TripRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mapsforge.map.datastore.MultiMapDataStore

@DisplayName("Tests for NavigationViewModel")
@ExtendWith(MockKExtension::class)
@ExtendWith(MainDispatcherExtension::class)
class NavigationViewModelStateTests {

    @MockK private lateinit var locationRepository: LocationRepository
    @MockK private lateinit var roadbookRepository: RoadbookRepository
    @MockK private lateinit var mapRepository: MapRepository
    private lateinit var tripRepository: TripRepository
    private lateinit var viewModel: NavigationViewModel

    @BeforeEach
    fun beforeEach() {
        coEvery { roadbookRepository.getRoadbookUri() } returns flow { emit("") }
        coEvery { roadbookRepository.getScroll() } returns flow { emit(RoadbookScroll())}
        coEvery { mapRepository.getMap() } returns MultiMapDataStore(MultiMapDataStore.DataPolicy.RETURN_ALL)

        tripRepository = TripFakeRepository()
        viewModel = NavigationViewModel(
            locationRepository,
            roadbookRepository,
            tripRepository,
            mapRepository,
        )
    }

    @AfterEach
    fun afterEach() {
        clearAllMocks()
    }

    @DisplayName("Given minimum partial (0)")
    @Nested
    inner class PartialIsMin {
        @Test
        fun `increase() should add 10 meters`() = runTest {
            assertEquals(0, viewModel.uiState.first().trip.partial)

            viewModel.onAction(NavigationViewModel.UiAction.IncreasePartial)

            assertEquals(10, viewModel.uiState.first().trip.partial)
        }

        @Test
        fun `decrease() should not change partial`() = runTest {
            assertEquals(0, viewModel.uiState.first().trip.partial)

            viewModel.onAction(NavigationViewModel.UiAction.DecreasePartial)

            assertEquals(0, viewModel.uiState.first().trip.partial)
        }
    }

    @DisplayName("Given maximum partial (>=999990)")
    @Nested
    inner class PartialIsMax {
        @BeforeEach
        fun setup() = runTest {
            tripRepository.setPartial(999990)
        }

        @Test
        fun `increase() should not change partial`() = runTest {
            assertEquals(999990, viewModel.uiState.first().trip.partial)

            viewModel.onAction(NavigationViewModel.UiAction.IncreasePartial)

            assertEquals(999990, viewModel.uiState.first().trip.partial)
        }

        @Test
        fun `decrease() should subtract 10 meters`() = runTest {
            assertEquals(999990, viewModel.uiState.first().trip.partial)

            viewModel.onAction(NavigationViewModel.UiAction.DecreasePartial)

            assertEquals(999980, viewModel.uiState.first().trip.partial)
        }
    }

    @DisplayName("Given partial > 0 and total > 0")
    @Nested
    inner class ResetTests {
        @BeforeEach
        fun setup() = runTest {
            tripRepository.setPartial(123450)
            tripRepository.setTotal(9876540)
        }

        @Test
        fun `resetPartial() should set partial to 0`() = runTest {
            assertEquals(123450, viewModel.uiState.first().trip.partial)

            viewModel.onAction(NavigationViewModel.UiAction.ResetPartial)

            assertEquals(0, viewModel.uiState.first().trip.partial)
        }

        @Test
        fun `resetAll() should set partial and total to 0`() = runTest {
            assertEquals(123450, viewModel.uiState.first().trip.partial)

            viewModel.onAction(NavigationViewModel.UiAction.ResetTrip)

            assertEquals(0, viewModel.uiState.first().trip.partial)
            assertEquals(0, viewModel.uiState.value.trip.total)
        }
    }

    @DisplayName("setPartial()")
    @Nested
    inner class SetPartialTests {
        @Test
        fun `when it's in 0-999 should update partial`() = runTest {
            viewModel.onAction(NavigationViewModel.UiAction.SetPartial("123,45"))

            assertEquals(123450, viewModel.uiState.first().trip.partial)
        }

        @Test
        fun `when it's out of range should throw IllegalArgumentException`() = runTest {
            assertThrows(IllegalArgumentException::class.java) {
                viewModel.onAction(NavigationViewModel.UiAction.SetPartial("1000,00"))
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when locations are collected distances should be calculated`() = runTest {
        coEvery {
            locationRepository.listenToLocation(any(), any())
        } returns TestRoute.getLocations().asFlow()
        viewModel.initialize()

        advanceUntilIdle()

        assertEquals(TestRoute.getDistance(), viewModel.uiState.take(TestRoute.getLocations().count()).first().trip.partial)
        assertEquals(TestRoute.getDistance(), viewModel.uiState.value.trip.total)
    }
}