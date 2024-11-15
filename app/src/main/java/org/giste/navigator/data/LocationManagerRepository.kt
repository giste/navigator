package org.giste.navigator.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.giste.navigator.model.Location
import org.giste.navigator.model.LocationPermissionException
import org.giste.navigator.model.LocationRepository
import org.giste.navigator.model.Settings
import org.giste.navigator.model.SettingsRepository
import javax.inject.Inject

private const val CLASS_NAME = "LocationManagerRepository"

class LocationManagerRepository @Inject constructor(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : LocationRepository {
    private val locationManager by lazy {
        getSystemService(context, LocationManager::class.java) as LocationManager
    }

    @SuppressLint("MissingPermission")
    override fun listenToLocation(minTime: Long, minDistance: Float): Flow<Location> {
        return callbackFlow {
            if (!hasLocationPermission()) throw LocationPermissionException()
            val locationCallback = LocationListener { location ->
                location.let {
                    Log.v(CLASS_NAME, "Location: $location")

                    launch(dispatcher) {
                        send(
                            Location(
                                latitude = it.latitude,
                                longitude = it.longitude,
                                altitude = it.altitude,
                                bearing = if (it.hasBearing()) it.bearing else 0.0f,
                                speed = if (it.hasSpeed()) it.speed else 0.0f,
                            )
                        )
                    }
                }
            }

            val settings: Settings = settingsRepository.get().first()

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                settings.locationMinTime,
                settings.locationMinDistance.toFloat(),
                locationCallback
            )

            awaitClose {
                // No one listens to flow anymore
                locationManager.removeUpdates(locationCallback)
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return context.checkSelfPermission(
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}