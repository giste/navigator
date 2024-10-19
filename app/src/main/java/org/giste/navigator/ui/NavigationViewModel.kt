package org.giste.navigator.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    locationRepository: LocationRepository
) : ViewModel() {
    data class UiState(
        val partial: Int = 0,
        val total: Int = 0,
    )

    var uiState by mutableStateOf(UiState())
        private set

    init {
        locationRepository.listenToLocation(1_000L, 10f).onEach {
            //Log.d("NavigationViewModel", "Location(${it})")
        }.launchIn(viewModelScope)
    }

    private fun resetPartial() {
        uiState = uiState.copy(partial = 0)
    }

    private fun decreasePartial() {
        if (uiState.partial > 0) {
            uiState = uiState.copy(partial = uiState.partial - 10)
        }
    }

    private fun increasePartial() {
        if (uiState.partial < 999_990) {
            uiState = uiState.copy(partial = uiState.partial + 10)
        }
    }

    private fun resetAll() {
        uiState = uiState.copy(partial = 0, total = 0)
    }

    private fun setPartial(partial: String) {
        val meters = partial.filter { it.isDigit() }.toInt() * 10

        if (meters in 0..999_990) {
            uiState = uiState.copy(partial = meters)
        } else {
            throw IllegalArgumentException(
                "Partial must represent a number between 0 and ${"%,.2f".format(999.99f)}"
            )
        }
    }

    private fun setTotal(total: String) {
        val meters = total.filter { it.isDigit() }.toInt() * 10

        if (meters in 0..9_999_990) {
            uiState = uiState.copy(total = meters)
        } else {
            throw IllegalArgumentException(
                "Total must represent a number between 0 and ${"%,.2f".format(9999.99f)}"
            )
        }
    }

    sealed class UiEvent {
        data object DecreasePartial: UiEvent()
        data object ResetPartial: UiEvent()
        data object IncreasePartial: UiEvent()
        data object ResetAll: UiEvent()
        data class SetPartial(val partial: String): UiEvent()
        data class SetTotal(val total: String): UiEvent()
    }

    fun onEvent(event: UiEvent) {
        when(event){
            is UiEvent.DecreasePartial -> { decreasePartial() }
            is UiEvent.ResetPartial -> { resetPartial() }
            is UiEvent.IncreasePartial -> { increasePartial() }
            is UiEvent.ResetAll -> { resetAll() }
            is UiEvent.SetPartial -> { setPartial(event.partial)}
            is UiEvent.SetTotal -> { setTotal(event.total) }
        }
    }
}

