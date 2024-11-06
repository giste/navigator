package org.giste.navigator.model

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface StateRepository {
    fun getState(): Flow<State>
    fun getPartial(): Flow<Int>
    fun getTotal(): Flow<Int>
    fun getRoadbookUri(): Flow<Uri>
    suspend fun setPartial(partial: Int)
    suspend fun setTotal(total: Int)
    suspend fun setRoadbookUri(roadbookUri: Uri)
}