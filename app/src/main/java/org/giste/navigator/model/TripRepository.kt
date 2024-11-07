package org.giste.navigator.model

import androidx.annotation.IntRange
import kotlinx.coroutines.flow.Flow

interface TripRepository {
    fun getPartial(): Flow<Int>
    fun getTotal(): Flow<Int>
    suspend fun incrementPartial()
    suspend fun decrementPartial()
    suspend fun resetPartial()
    suspend fun resetTrip()
    suspend fun addDistance(@IntRange(from = 0, to = 99999) distance: Int)
    suspend fun setPartial(@IntRange(from = 0, to = 99999) partial: Int)
    suspend fun setTotal(@IntRange(from = 0, to = 999999) total: Int)
}