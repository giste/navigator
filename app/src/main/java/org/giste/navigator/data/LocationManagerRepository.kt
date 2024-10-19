package org.giste.navigator.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.content.ContextCompat.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.giste.navigator.ui.LocationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationManagerRepository @Inject constructor(
    @ApplicationContext val context: Context
) : LocationRepository {
    private val locationManager by lazy {
        getSystemService(context, LocationManager::class.java) as LocationManager
    }

    @SuppressLint("MissingPermission")
    override fun listenToLocation(minTime: Long, minDistance: Float): Flow<Location> {
        return callbackFlow {
            //TODO: LocationPermissionException
            if(!hasLocationPermission()) throw Exception()
            val locationCallback = LocationListener { location ->
                location.let {
                    launch { send(it) }
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