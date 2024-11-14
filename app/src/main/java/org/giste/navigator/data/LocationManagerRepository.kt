package org.giste.navigator.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.content.ContextCompat.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.giste.navigator.model.Location
import org.giste.navigator.model.LocationPermissionException
import org.giste.navigator.model.LocationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationManagerRepository @Inject constructor(
    @ApplicationContext val context: Context,
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

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTime,
                minDistance,
                locationCallback
            )

            awaitClose {
                // No one listens to flow anymore
                locationManager.removeUpdates(locationCallback)
            }
        }
    }

    override fun hasLocationPermission(): Boolean {
        return context.checkSelfPermission(
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}