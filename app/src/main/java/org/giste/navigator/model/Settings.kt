package org.giste.navigator.model

data class Settings(
    val locationMinTime: Long = 1_000L,
    val locationMinDistance: Int = 10,
    val distanceUseAltitude: Boolean = true,
)
