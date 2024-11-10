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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.giste.navigator.model.Location
import org.giste.navigator.model.LocationPermissionException
import org.giste.navigator.model.LocationRepository
import org.giste.navigator.model.PdfPage
import org.giste.navigator.model.RoadbookRepository
import org.giste.navigator.model.RoadbookScroll
import org.giste.navigator.model.TripRepository
import javax.inject.Inject
import kotlin.math.roundToInt

private const val CLASS_NAME = "NavigationViewModel"

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val roadbookRepository: RoadbookRepository,
    private val tripRepository: TripRepository,
) : ViewModel() {
    private var lastLocation: Location? = null
    private var lastUiState: UiState = runBlocking { collectFirstState() }
    var initialized by mutableStateOf(false)
        private set
    private var lastRoadbookUri = ""

    val uiState: StateFlow<UiState> = collectUiState()

    fun initialize() {
        Log.d(CLASS_NAME, "Entering initialize()")
        if (initialized) return

        viewModelScope.launch {
            startListenForLocations()
            initialized = true
            Log.d(CLASS_NAME, "Initialized: $initialized")
        }
    }

    private suspend fun collectFirstState(): UiState {
        val roadbookScroll = roadbookRepository.getScroll().first()

        return UiState(
            partial = tripRepository.getPartial().first(),
            total = tripRepository.getTotal().first(),
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
            tripRepository.getPartial(),
            tripRepository.getTotal(),
            roadbookRepository.getRoadbookUri(),
            roadbookRepository.getScroll(),
        ) { partial, total, roadbookUri, scroll ->
            var newUiState = lastUiState
            if (lastUiState.partial != partial) newUiState = newUiState.copy(partial = partial)
            if (lastUiState.total != total) newUiState = newUiState.copy(total = total)
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
                lastLocation?.let {
                    val distance = it.distanceTo(newLocation).roundToInt()
                    tripRepository.addDistance(distance)
                }

                lastLocation = newLocation
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

    sealed class UiAction {
        data object DecreasePartial : UiAction()
        data object ResetPartial : UiAction()
        data object IncreasePartial : UiAction()
        data object ResetTrip : UiAction()
        data class SetPartial(val partial: String) : UiAction()
        data class SetTotal(val total: String) : UiAction()
        data class SetUri(val uri: Uri) : UiAction()
        data class SetScroll(val pageIndex: Int, val pageOffset: Int) : UiAction()
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
        }
    }

    data class UiState(
        val partial: Int = 0,
        val total: Int = 0,
        val roadbookState: RoadbookState = RoadbookState.NotLoaded,
        val pageIndex: Int = 0,
        val pageOffset: Int = 0,
    )

    sealed class RoadbookState {
        data object NotLoaded : RoadbookState()
        data class Loaded(val pages: Flow<PagingData<PdfPage>>) : RoadbookState()
    }

}
