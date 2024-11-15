package org.giste.navigator.model

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun get(): Flow<Settings>
    suspend fun save(settings: Settings)
}