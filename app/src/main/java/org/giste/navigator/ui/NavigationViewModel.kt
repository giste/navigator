package org.giste.navigator.ui

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import org.giste.navigator.model.Location
import org.giste.navigator.model.LocationPermissionException
import org.giste.navigator.model.LocationRepository
import org.giste.navigator.model.PdfPage
import org.giste.navigator.model.PdfRepository
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val pdfRepository: PdfRepository,
) : ViewModel() {
    private val _tripState = MutableStateFlow(TripState())
    val tripState = _tripState.asStateFlow()

    private var lastLocation by mutableStateOf<Location?>(null)
    private var roadbookUri = mutableStateOf(Uri.EMPTY)

    /**
     * The internal mutable state flow that holds the current display state of the PDF.
     */
    private val _roadbookState = MutableStateFlow<RoadbookState>(RoadbookState.NotLoaded)

    /**
     * A public immutable state flow that exposes the current display state of the PDF.
     */
    val roadbookState = _roadbookState.asStateFlow()

    init {
        startListenForLocations()
    }

    private fun startListenForLocations() {
        locationRepository.listenToLocation(1_000L, 10f)
            .catch {
                // Location exceptions should be managed by permissions screen
                e -> if (e !is LocationPermissionException) throw e
            }
            .onEach { newLocation ->
            lastLocation?.let {
                val distance = it.distanceTo(newLocation).roundToInt()
                _tripState.update { currentTripState ->
                    currentTripState.copy(
                        partial = currentTripState.partial + distance,
                        total = currentTripState.total + distance,
                    )
                }
            }
            lastLocation = newLocation
        }.launchIn(viewModelScope)
    }

    private fun resetPartial() {
        _tripState.update { currentTripState ->
            currentTripState.copy(partial = 0)
        }
    }

    private fun decreasePartial() {
        _tripState.update { currentTripState ->
            currentTripState.copy(partial = (currentTripState.partial - 10).coerceAtLeast(0))
        }
    }

    private fun increasePartial() {
        _tripState.update { currentTripState ->
            currentTripState.copy(partial = (currentTripState.partial + 10).coerceAtMost(999_990))
        }
    }

    private fun resetAll() {
        _tripState.update { currentTripState ->
            currentTripState.copy(partial = 0, total = 0)
        }
    }

    private fun setPartial(partial: String) {
        val meters = partial.filter { it.isDigit() }.toInt() * 10

        if (meters in 0..999_990) {
            _tripState.update { currentTripState ->
                currentTripState.copy(partial = meters)
            }
        } else {
            throw IllegalArgumentException(
                "Partial must represent a number between 0 and ${"%,.2f".format(999.99f)}"
            )
        }
    }

    private fun setTotal(total: String) {
        val meters = total.filter { it.isDigit() }.toInt() * 10

        if (meters in 0..9_999_990) {
            _tripState.update { currentTripState ->
                currentTripState.copy(total = meters)
            }
        } else {
            throw IllegalArgumentException(
                "Total must represent a number between 0 and ${"%,.2f".format(9999.99f)}"
            )
        }
    }

    private fun setRoadbookUri(uri: Uri) {
        this.roadbookUri.value = uri
        this._roadbookState.value = RoadbookState.Loaded(pdfRepository.getPdfStream(uri))
    }

    sealed class UiEvent {
        data object DecreasePartial: UiEvent()
        data object ResetPartial: UiEvent()
        data object IncreasePartial: UiEvent()
        data object ResetAll: UiEvent()
        data class SetPartial(val partial: String): UiEvent()
        data class SetTotal(val total: String): UiEvent()
        data class SetUri(val uri: Uri): UiEvent()
    }

    fun onEvent(event: UiEvent) {
        when(event){
            is UiEvent.DecreasePartial -> { decreasePartial() }
            is UiEvent.ResetPartial -> { resetPartial() }
            is UiEvent.IncreasePartial -> { increasePartial() }
            is UiEvent.ResetAll -> { resetAll() }
            is UiEvent.SetPartial -> { setPartial(event.partial)}
            is UiEvent.SetTotal -> { setTotal(event.total) }
            is UiEvent.SetUri -> { setRoadbookUri(event.uri) }
        }
    }

    data class TripState(
        val partial: Int = 0,
        val total: Int = 0,
    )

    sealed class RoadbookState {
        data object NotLoaded : RoadbookState()
        data class Loaded(val pages: Flow<PagingData<PdfPage>>) : RoadbookState()
    }

}

