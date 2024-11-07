package org.giste.navigator.model

import kotlinx.coroutines.flow.Flow

interface StateRepository {
    fun getState(): Flow<State>
    fun getPartial(): Flow<Int>
    fun getTotal(): Flow<Int>
    fun getRoadbookUri(): Flow<String>
    suspend fun setPartial(partial: Int)
    suspend fun setTotal(total: Int)
    suspend fun setRoadbookUri(roadbookUri: String)
}