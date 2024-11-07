package org.giste.navigator.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.giste.navigator.model.Location
import org.giste.navigator.model.LocationPermissionException
import org.giste.navigator.model.LocationRepository
import org.giste.navigator.model.PdfPage
import org.giste.navigator.model.RoadbookRepository
import org.giste.navigator.model.State
import org.giste.navigator.model.StateRepository
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val roadbookRepository: RoadbookRepository,
    private val stateRepository: StateRepository,
) : ViewModel() {
    private var lastLocation: Location? = null
    private var lastState = State()
    private var lastNavigationState = NavigationState()

    val navigationState: StateFlow<NavigationState> = stateRepository.getState().map {
        var newState = lastNavigationState
        if (lastState.partial != it.partial) newState = newState.copy(partial = it.partial)
        if (lastState.total != it.total) newState = newState.copy(total = it.total)
        if (lastState.roadbookUri != it.roadbookUri) {
            newState = newState.copy(
                roadbookState = if (it.roadbookUri == "") {
                    RoadbookState.NotLoaded
                } else {
                    RoadbookState.Loaded(roadbookRepository.getPages())
                }
            )
        }
        lastState = it
        lastNavigationState = newState

        return@map newState
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = NavigationState()
    )

    init {
        startListenForLocations()
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
                    with(stateRepository) {
                        setPartial(navigationState.value.partial + distance)
                        setTotal(navigationState.value.total + distance)
                    }
                }

                lastLocation = newLocation
            }.launchIn(viewModelScope)
    }

    private fun resetPartial() {
        viewModelScope.launch { stateRepository.setPartial(0) }
    }

    private fun decreasePartial() {
        viewModelScope.launch {
            stateRepository.setPartial((navigationState.value.partial - 10).coerceAtLeast(0))
        }
    }

    private fun increasePartial() {
        viewModelScope.launch {
            stateRepository.setPartial((navigationState.value.partial + 10).coerceAtMost(999_990))
        }
    }

    private fun resetTrip() {
        viewModelScope.launch {
            stateRepository.setPartial(0)
            stateRepository.setTotal(0)
        }

    }

    private fun setPartial(partial: String) {
        val meters = partial.filter { it.isDigit() }.toInt() * 10

        if (meters in 0..999_990) {
            viewModelScope.launch { stateRepository.setPartial(meters) }
        } else {
            throw IllegalArgumentException(
                "Partial must represent a number between 0 and ${"%,.2f".format(999.99f)}"
            )
        }
    }

    private fun setTotal(total: String) {
        val meters = total.filter { it.isDigit() }.toInt() * 10

        if (meters in 0..9_999_990) {
            viewModelScope.launch { stateRepository.setTotal(meters) }
        } else {
            throw IllegalArgumentException(
                "Total must represent a number between 0 and ${"%,.2f".format(9999.99f)}"
            )
        }
    }

    private fun setRoadbookUri(uri: Uri) {
        viewModelScope.launch {
            roadbookRepository.load(uri.toString())
            stateRepository.setRoadbookUri(uri.toString())
        }
    }

    sealed class UiEvent {
        data object DecreasePartial : UiEvent()
        data object ResetPartial : UiEvent()
        data object IncreasePartial : UiEvent()
        data object ResetTrip : UiEvent()
        data class SetPartial(val partial: String) : UiEvent()
        data class SetTotal(val total: String) : UiEvent()
        data class SetUri(val uri: Uri) : UiEvent()
    }

    fun onEvent(event: UiEvent) {
        when (event) {
            is UiEvent.DecreasePartial -> {
                decreasePartial()
            }

            is UiEvent.ResetPartial -> {
                resetPartial()
            }

            is UiEvent.IncreasePartial -> {
                increasePartial()
            }

            is UiEvent.ResetTrip -> {
                resetTrip()
            }

            is UiEvent.SetPartial -> {
                setPartial(event.partial)
            }

            is UiEvent.SetTotal -> {
                setTotal(event.total)
            }

            is UiEvent.SetUri -> {
                setRoadbookUri(event.uri)
            }
        }
    }

    data class NavigationState(
        val partial: Int = 0,
        val total: Int = 0,
        val roadbookState: RoadbookState = RoadbookState.NotLoaded
    )

    sealed class RoadbookState {
        data object NotLoaded : RoadbookState()
        data class Loaded(val pages: Flow<PagingData<PdfPage>>) : RoadbookState()
    }

}
