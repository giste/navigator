package org.giste.navigator.ui

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.giste.navigator.model.LocationRepository
import org.giste.navigator.model.RoadbookRepository
import org.giste.navigator.model.State
import org.giste.navigator.model.StateRepository
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
    private lateinit var stateRepository: StateRepository
    private lateinit var viewModel: NavigationViewModel

    @BeforeEach
    fun beforeEach() {
        coEvery {
            locationRepository.listenToLocation(any(), any())
        } returns EmptyRoute.getLocations().asFlow()

        stateRepository = FakeStateRepository()
        viewModel = NavigationViewModel(locationRepository, roadbookRepository, stateRepository)
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
            stateRepository.setPartial(999990)
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
            stateRepository.setPartial(123450)
            stateRepository.setTotal(9876540)
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
        @BeforeEach
        fun setup() = runTest {
            viewModel = NavigationViewModel(locationRepository, roadbookRepository, stateRepository)
        }

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
        val viewModel = NavigationViewModel(locRepository, roadbookRepository, stateRepository)

        advanceUntilIdle()

        assertEquals(TestRoute.getDistance(), viewModel.navigationState.take(TestRoute.getLocations().count()).first().partial)
        assertEquals(TestRoute.getDistance(), viewModel.navigationState.value.total)
    }

    class FakeStateRepository() : StateRepository {
        private var _state = MutableStateFlow(State())
        private val state = _state.asStateFlow()

        override fun getState(): Flow<State> {
            return state
        }

        override fun getPartial(): Flow<Int> {
            return state.map { it.partial }
        }

        override fun getTotal(): Flow<Int> {
            return state.map { it.total }
        }

        override fun getRoadbookUri(): Flow<String> {
            return state.map { it.roadbookUri }
        }

        override suspend fun setPartial(partial: Int) {
            _state.update { currentState -> currentState.copy(partial = partial) }
        }

        override suspend fun setTotal(total: Int) {
            _state.update { currentState -> currentState.copy(total = total) }
        }

        override suspend fun setRoadbookUri(roadbookUri: String) {
            _state.update { currentState -> currentState.copy(roadbookUri = roadbookUri) }
        }

    }
}