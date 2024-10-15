package org.giste.navigator.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor() : ViewModel() {
    private var _partial by mutableIntStateOf(0)
    val partial: String
        get() = "%,.2f".format(_partial.toFloat()/1000)

    private var _total by mutableIntStateOf(0)
    val total: String
        get() = "%,.2f".format(_total.toFloat()/1000)

    fun resetPartial() {
        _partial = 0
    }

    fun decreasePartial() {
        if (_partial >= 10) {
            _partial -= 10
        }
    }

    fun increasePartial() {
        if (_partial < 999990) {
            _partial += 10
        }
    }

    fun resetAll() {
        _partial = 0
        _total = 0
    }

    fun setPartial(partial: String) {
        val meters = partial.split(Regex("\\D+")).map { it.toInt() }

        _partial = meters[0] * 1000 + meters[1] * 10
    }
}