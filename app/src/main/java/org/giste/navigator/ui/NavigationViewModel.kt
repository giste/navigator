package org.giste.navigator.ui

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.giste.navigator.model.Location
import org.giste.navigator.model.LocationPermissionException
import org.giste.navigator.model.LocationRepository
import org.giste.navigator.model.MapRepository
import org.giste.navigator.model.PdfPage
import org.giste.navigator.model.RoadbookRepository
import org.giste.navigator.model.RoadbookScroll
import org.giste.navigator.model.Settings
import org.giste.navigator.model.SettingsRepository
import org.giste.navigator.model.Trip
import org.giste.navigator.model.TripRepository
import javax.inject.Inject
import kotlin.math.roundToInt

private const val CLASS_NAME = "NavigationViewModel"

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val roadbookRepository: RoadbookRepository,
    private val tripRepository: TripRepository,
    private val settingsRepository: SettingsRepository,
    private val mapRepository: MapRepository,
) : ViewModel() {
    private var lastUiState: UiState = runBlocking { collectFirstState() }
    var initialized by mutableStateOf(false)
        private set
    private var lastRoadbookUri = ""
    private val _locationState: MutableStateFlow<Location?> = MutableStateFlow(null)

    private val _maps: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
    val maps = _maps.asStateFlow()

    val uiState: StateFlow<UiState> = collectUiState()

    val settingState: StateFlow<Settings> = settingsRepository.get().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = Settings()
    )

    fun initialize() {
        Log.d(CLASS_NAME, "Entering initialize()")
        if (initialized) return

        viewModelScope.launch {
            startListenForLocations()
            _maps.update { mapRepository.getMaps() }
            initialized = true
            Log.d(CLASS_NAME, "Initialized: $initialized")
        }
    }

    private suspend fun collectFirstState(): UiState {
        val roadbookScroll = roadbookRepository.getScroll().first()

        return UiState(
            trip = tripRepository.get().first(),
            roadbookState = if (roadbookRepository.getRoadbookUri().first() == "") {
                RoadbookState.NotLoaded
            } else {
                RoadbookState.Loaded(roadbookRepository.getPages())
            },
            pageIndex = roadbookScroll.pageIndex,
            pageOffset = roadbookScroll.pageOffset,
        )
    }

    private fun collectUiState(): StateFlow<UiState> {
        return combine(
            tripRepository.get(),
            roadbookRepository.getRoadbookUri(),
            roadbookRepository.getScroll(),
            _locationState,
        ) { trip, roadbookUri, scroll, location->
            var newUiState = lastUiState
            if (lastUiState.trip != trip) newUiState = newUiState.copy(trip = trip)
            if (lastRoadbookUri != roadbookUri) {
                newUiState = newUiState.copy(
                    roadbookState = if (roadbookUri == "") {
                        RoadbookState.NotLoaded
                    } else {
                        RoadbookState.Loaded(roadbookRepository.getPages())
                    }
                )
                lastRoadbookUri = roadbookUri
            }
            if (lastUiState.pageIndex != scroll.pageIndex) newUiState =
                newUiState.copy(pageIndex = scroll.pageIndex)
            if (lastUiState.pageOffset != scroll.pageOffset) newUiState =
                newUiState.copy(pageOffset = scroll.pageOffset)
            if (lastUiState.location != location) newUiState = newUiState.copy(location = location)

            lastUiState = newUiState

            Log.d(CLASS_NAME, "uiSate: $newUiState")

            return@combine newUiState
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = lastUiState
        )
    }

    private fun startListenForLocations() {
        locationRepository.listenToLocation(1_000L, 10f)
            .catch { e ->
                // Location permissions exceptions should be managed by permissions screen
                if (e !is LocationPermissionException) throw e
            }
            .onEach { newLocation ->
                _locationState.value?.let {
                    val distance = it.distanceTo(newLocation).roundToInt()
                    tripRepository.addDistance(distance)
                }

                _locationState.update { newLocation }
            }.launchIn(viewModelScope)
    }

    private fun resetPartial() {
        viewModelScope.launch { tripRepository.resetPartial() }
    }

    private fun decreasePartial() {
        viewModelScope.launch { tripRepository.decrementPartial() }
    }

    private fun increasePartial() {
        viewModelScope.launch { tripRepository.incrementPartial() }
    }

    private fun resetTrip() {
        viewModelScope.launch { tripRepository.resetTrip() }
    }

    private fun setPartial(partial: String) {
        val meters = partial.filter { it.isDigit() }.toInt() * 10

        if (meters in 0..999_990) {
            viewModelScope.launch { tripRepository.setPartial(meters) }
        } else {
            throw IllegalArgumentException(
                "Partial must represent a number between 0 and ${"%,.2f".format(999.99f)}"
            )
        }
    }

    private fun setTotal(total: String) {
        val meters = total.filter { it.isDigit() }.toInt() * 10

        if (meters in 0..9_999_990) {
            viewModelScope.launch { tripRepository.setTotal(meters) }
        } else {
            throw IllegalArgumentException(
                "Total must represent a number between 0 and ${"%,.2f".format(9999.99f)}"
            )
        }
    }

    private fun setRoadbookUri(uri: Uri) {
        viewModelScope.launch { roadbookRepository.load(uri.toString()) }
    }

    private fun setScroll(pageIndex: Int, pageOffset: Int) {
        viewModelScope.launch {
            roadbookRepository.setScroll(
                RoadbookScroll(pageIndex, pageOffset)
            )
        }
    }

    private fun setSettings(settings: Settings) {
        viewModelScope.launch {
            settingsRepository.save(settings)
        }
    }

    sealed class UiAction {
        data object DecreasePartial : UiAction()
        data object ResetPartial : UiAction()
        data object IncreasePartial : UiAction()
        data object ResetTrip : UiAction()
        data class SetPartial(val partial: String) : UiAction()
        data class SetTotal(val total: String) : UiAction()
        data class SetUri(val uri: Uri) : UiAction()
        data class SetScroll(val pageIndex: Int, val pageOffset: Int) : UiAction()
        data class SetSettings(val settings: Settings) : UiAction()
    }

    fun onAction(event: UiAction) {
        when (event) {
            is UiAction.DecreasePartial -> decreasePartial()
            is UiAction.ResetPartial -> resetPartial()
            is UiAction.IncreasePartial -> increasePartial()
            is UiAction.ResetTrip -> resetTrip()
            is UiAction.SetPartial -> setPartial(event.partial)
            is UiAction.SetTotal -> setTotal(event.total)
            is UiAction.SetUri -> setRoadbookUri(event.uri)
            is UiAction.SetScroll -> setScroll(event.pageIndex, event.pageOffset)
            is UiAction.SetSettings -> setSettings(event.settings)
        }
    }

    data class UiState(
        val trip: Trip = Trip(),
        val roadbookState: RoadbookState = RoadbookState.NotLoaded,
        val pageIndex: Int = 0,
        val pageOffset: Int = 0,
        val location: Location? = null,
    )

    sealed class RoadbookState {
        data object NotLoaded : RoadbookState()
        data class Loaded(val pages: Flow<PagingData<PdfPage>>) : RoadbookState()
    }

}
