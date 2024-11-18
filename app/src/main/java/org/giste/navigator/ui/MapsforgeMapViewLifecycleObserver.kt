package org.giste.navigator.ui

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import org.mapsforge.map.android.view.MapView

class MapsforgeMapViewLifecycleObserver(private val mapView: MapView) : DefaultLifecycleObserver {
    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        mapView.destroyAll()
    }
}