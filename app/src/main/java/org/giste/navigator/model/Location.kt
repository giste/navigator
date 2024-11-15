package org.giste.navigator.model

import android.util.Log
import androidx.annotation.FloatRange
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val CLASS_NAME = "Location"

data class Location(
    @FloatRange(from = -90.0, to = 90.0)
    val latitude: Double,
    @FloatRange(from = -180.0, to = 180.0)
    val longitude: Double,
    val altitude: Double = 0.0,
    val bearing: Float = 0.0f,
    val speed: Float = 0.0f,
) {
    fun distanceTo(otherLocation: Location): Double {
        val earthRadius = 6367.45
        val lat1 = toRad(this.latitude)
        val lat2 = toRad(otherLocation.latitude)
        val lon1 = toRad(this.longitude)
        val lon2 = toRad(otherLocation.longitude)
        val distance =
            earthRadius * acos(sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2) * cos(lon2 - lon1)) * 1000

        if (this.altitude > 0 && otherLocation.altitude > 0) {
            val distanceWithAltitude = sqrt(
                (this.altitude - otherLocation.altitude)
                    .pow(2)
                    .plus(distance.pow(2))
            )

            Log.d(CLASS_NAME, "Distance with altitude: $distanceWithAltitude; Distance: $distance")

            return distanceWithAltitude
        }

        return distance
    }

    private fun toRad(degrees: Double): Double = degrees * PI / 180
}