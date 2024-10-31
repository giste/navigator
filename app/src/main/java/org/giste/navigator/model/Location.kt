package org.giste.navigator.model

import androidx.annotation.FloatRange
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

data class Location(
    @FloatRange(from = -90.0, to = 90.0)
    val latitude: Double,
    @FloatRange(from = -180.0, to = 180.0)
    val longitude: Double,
    val altitude: Double = 0.0,
) {
    fun distanceTo(otherLocation: Location): Double {
        val earthRadius = 6367.45
        val lat1 = toRad(this.latitude)
        val lat2 = toRad(otherLocation.latitude)
        val lon1 = toRad(this.longitude)
        val lon2 = toRad(otherLocation.longitude)
        val distance = earthRadius * acos(sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2) * cos(lon2 - lon1))

        return distance * 1000
    }

    private fun toRad(degrees: Double): Double = degrees * PI/180
}