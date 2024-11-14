package org.giste.navigator.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.giste.navigator.model.Trip
import org.giste.navigator.model.TripRepository

class TripFakeRepository : TripRepository {
    private var trip = MutableStateFlow(Trip())

    override fun get(): StateFlow<Trip> = trip.asStateFlow()

    override suspend fun incrementPartial() {
        trip.update { it.copy(partial = getSafePartial(it.partial + 10)) }
    }

    override suspend fun decrementPartial() {
        trip.update { it.copy(partial = getSafePartial(it.partial - 10)) }
    }

    override suspend fun resetPartial() {
        trip.update { it.copy(partial = 0) }
    }

    override suspend fun resetTrip() {
        trip.update { Trip() }
    }

    override suspend fun addDistance(distance: Int) {
        trip.update {
            Trip(
                partial = getSafePartial(it.partial + distance),
                total = getSafeTotal(it.total + distance)
            )
        }
    }

    override suspend fun setPartial(partial: Int) {
        trip.update { it.copy(partial = getSafePartial(partial)) }
    }

    override suspend fun setTotal(total: Int) {
        trip.update { it.copy(total = getSafeTotal(total)) }
    }

    private fun getSafePartial(partial: Int): Int{
        return partial.coerceAtLeast(0).coerceAtMost(999990)
    }

    private fun getSafeTotal(total: Int): Int{
        return total.coerceAtLeast(0).coerceAtMost(9999990)
    }
}