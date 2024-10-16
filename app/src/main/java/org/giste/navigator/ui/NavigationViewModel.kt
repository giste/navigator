package org.giste.navigator.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor() : ViewModel() {
    data class UiState(
        val partial: Int = 0,
        val total: Int = 0,
    )

    var uiState by mutableStateOf(UiState())
        private set

    fun resetPartial() {
        uiState = uiState.copy(partial = 0)
    }

    fun decreasePartial() {
        if (uiState.partial > 0) {
            uiState = uiState.copy(partial = uiState.partial - 10)
        }
    }

    fun increasePartial() {
        if (uiState.partial < 999_990) {
            uiState = uiState.copy(partial = uiState.partial + 10)
        }
    }

    fun resetAll() {
        uiState = uiState.copy(partial = 0, total = 0)
    }

    fun setPartial(partial: String) {
        val meters = partial.filter { it.isDigit() }.toInt() * 10

        if (meters in 0..999_990) {
            uiState = uiState.copy(partial = meters)
        } else {
            throw IllegalArgumentException(
                "Partial must represent a number between 0 and ${"%,.2f".format(999.99f)}"
            )
        }
    }
}