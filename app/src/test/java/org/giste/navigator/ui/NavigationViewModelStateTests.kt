package org.giste.navigator.ui

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.giste.navigator.model.LocationRepository
import org.giste.navigator.model.RoadbookRepository
import org.giste.navigator.model.TripRepository
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
@ExtendWith(MainDispatcherExtension::class)
class NavigationViewModelStateTests {

    @MockK private lateinit var locationRepository: LocationRepository
    @MockK private lateinit var roadbookRepository: RoadbookRepository
    private lateinit var tripRepository: TripRepository
    private lateinit var viewModel: NavigationViewModel

    @BeforeEach
    fun beforeEach() {
        coEvery {
            locationRepository.listenToLocation(any(), any())
        } returns EmptyRoute.getLocations().asFlow()
        coEvery { roadbookRepository.getRoadbookUri() } returns flow { emit("") }

        tripRepository = TripFakeRepository()
        viewModel = NavigationViewModel(locationRepository, roadbookRepository, tripRepository)
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
            assertEquals(0, viewModel.navigationState.first().partial)

            viewModel.onEvent(NavigationViewModel.UiEvent.IncreasePartial)

            assertEquals(10, viewModel.navigationState.first().partial)
        }

        @Test
        fun `decrease() should not change partial`() = runTest {
            assertEquals(0, viewModel.navigationState.first().partial)

            viewModel.onEvent(NavigationViewModel.UiEvent.DecreasePartial)

            assertEquals(0, viewModel.navigationState.first().partial)
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
            assertEquals(999990, viewModel.navigationState.first().partial)

            viewModel.onEvent(NavigationViewModel.UiEvent.IncreasePartial)

            assertEquals(999990, viewModel.navigationState.first().partial)
        }

        @Test
        fun `decrease() should subtract 10 meters`() = runTest {
            assertEquals(999990, viewModel.navigationState.first().partial)

            viewModel.onEvent(NavigationViewModel.UiEvent.DecreasePartial)

            assertEquals(999980, viewModel.navigationState.first().partial)
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
            assertEquals(123450, viewModel.navigationState.first().partial)

            viewModel.onEvent(NavigationViewModel.UiEvent.ResetPartial)

            assertEquals(0, viewModel.navigationState.first().partial)
        }

        @Test
        fun `resetAll() should set partial and total to 0`() = runTest {
            assertEquals(123450, viewModel.navigationState.first().partial)

            viewModel.onEvent(NavigationViewModel.UiEvent.ResetTrip)

            assertEquals(0, viewModel.navigationState.first().partial)
            assertEquals(0, viewModel.navigationState.value.total)
        }
    }

    @DisplayName("setPartial()")
    @Nested
    inner class SetPartialTests {
        @Test
        fun `when it's in 0-999 should update partial`() = runTest {
            viewModel.onEvent(NavigationViewModel.UiEvent.SetPartial("123,45"))

            assertEquals(123450, viewModel.navigationState.first().partial)
        }

        @Test
        fun `when it's out of range should throw IllegalArgumentException`() = runTest {
            assertThrows(IllegalArgumentException::class.java) {
                viewModel.onEvent(NavigationViewModel.UiEvent.SetPartial("1000,00"))
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when locations are collected distances should be calculated`() = runTest {
        val locRepository: LocationRepository = mockk()
        coEvery {
            locRepository.listenToLocation(any(), any())
        } returns TestRoute.getLocations().asFlow()
        val viewModel = NavigationViewModel(locRepository, roadbookRepository, tripRepository)

        advanceUntilIdle()

        assertEquals(TestRoute.getDistance(), viewModel.navigationState.take(TestRoute.getLocations().count()).first().partial)
        assertEquals(TestRoute.getDistance(), viewModel.navigationState.value.total)
    }

    class TripFakeRepository : TripRepository {
        private var _partial = MutableStateFlow(0)
        private var _total = MutableStateFlow(0)

        override fun getPartial() = _partial.asStateFlow()

        override fun getTotal() = _total.asStateFlow()

        override suspend fun incrementPartial() {
            _partial.update { getSafePartial(it + 10)
            }
        }

        override suspend fun decrementPartial() {
            _partial.update { getSafePartial(it - 10)
            }
        }

        override suspend fun resetPartial() {
            _partial.update { 0 }
        }

        override suspend fun resetTrip() {
            _partial.update { 0 }
            _total.update { 0 }
        }

        override suspend fun addDistance(distance: Int) {
            _partial.update { getSafePartial(it + distance) }
            _total.update { getSafeTotal(it + distance) }
        }

        override suspend fun setPartial(partial: Int) {
            _partial.update { getSafePartial(partial) }
        }

        override suspend fun setTotal(total: Int) {
            _total.update { getSafeTotal(total) }
        }

        private fun getSafePartial(partial: Int): Int{
            return partial.coerceAtLeast(0).coerceAtMost(999990)
        }

        private fun getSafeTotal(total: Int): Int{
            return total.coerceAtLeast(0).coerceAtMost(9999990)
        }
    }
}