package org.giste.navigator.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.giste.navigator.model.TripRepository

class TripFakeRepository : TripRepository {
    private var _partial = MutableStateFlow(0)
    private var _total = MutableStateFlow(0)

    override fun getPartial() = _partial.asStateFlow()

    override fun getTotal() = _total.asStateFlow()

    override suspend fun incrementPartial() {
        _partial.update { getSafePartial(it + 10) }
    }

    override suspend fun decrementPartial() {
        _partial.update { getSafePartial(it - 10) }
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