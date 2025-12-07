package ch.rmy.android.statusbar_tacho.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.SystemClock
import androidx.core.content.getSystemService
import androidx.core.location.LocationListenerCompat
import ch.rmy.android.statusbar_tacho.utils.Destroyable
import ch.rmy.android.statusbar_tacho.utils.PermissionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SpeedWatcher(context: Context) : Destroyable {

    private val locationManager: LocationManager =
        context.getSystemService()!!

    private val _speedState = MutableStateFlow<SpeedState>(SpeedState.Disabled)
    val speedState = _speedState.asStateFlow()

    private val permissionManager = PermissionManager(context)

    private var currentSpeed: Float? = null
        set(value) {
            field = value
            updateSpeedState()
        }
    private var lastGpsUpdate = 0L

    // Keep the last received location and its timestamp so we can compute
    // speed when the Location doesn't provide it via hasSpeed()/getSpeed().
    private var lastLocation: Location? = null
    private var lastLocationElapsedRealtime: Long = 0L

    private var enabled: Boolean = false
        set(value) {
            field = value
            updateSpeedState()
        }

    private var isGPSEnabled = false
        set(value) {
            field = value
            updateSpeedState()
        }

    private val gpsLocationListener = object : LocationListenerCompat {
        override fun onLocationChanged(location: Location) {
            // Prefer the provider's speed value when available
            val speedFromLocation: Float? = if (location.hasSpeed()) {
                location.speed
            } else {
                // Fallback: compute speed from last location and elapsed realtime
                val last = lastLocation
                val deltaMillis = SystemClock.elapsedRealtime() - lastLocationElapsedRealtime
                if (last != null && deltaMillis > 0) {
                    val distanceMeters = last.distanceTo(location)
                    distanceMeters / (deltaMillis / 1000f)
                } else {
                    null
                }
            }

            currentSpeed = speedFromLocation

            // Update last location/timestamp for future fallback computations
            lastLocation = Location(location)
            lastLocationElapsedRealtime = SystemClock.elapsedRealtime()

            // Mark that GPS provided a recent update
            lastGpsUpdate = SystemClock.elapsedRealtime()
        }

        override fun onProviderEnabled(provider: String) {
            isGPSEnabled = true
        }

        override fun onProviderDisabled(provider: String) {
            isGPSEnabled = false
            currentSpeed = null
        }
    }

    private val fusedLocationListener = LocationListenerCompat { location ->
        val now = SystemClock.elapsedRealtime()
        if (currentSpeed == null || now - lastGpsUpdate > 10000) {
            val speedFromLocation: Float? = if (location.hasSpeed()) {
                location.speed
            } else {
                val last = lastLocation
                val deltaMillis = now - lastLocationElapsedRealtime
                if (last != null && deltaMillis > 0) {
                    val distanceMeters = last.distanceTo(location)
                    distanceMeters / (deltaMillis / 1000f)
                } else {
                    null
                }
            }
            if (speedFromLocation != null) {
                currentSpeed = speedFromLocation
            }
            lastLocation = Location(location)
            lastLocationElapsedRealtime = now
        }
    }

    init {
        updateGPSState()
    }

    private fun updateGPSState() {
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun toggle(state: Boolean) {
        if (state) {
            enable()
        } else {
            disable()
        }
    }

    @SuppressLint("MissingPermission")
    fun enable() {
        if (enabled) {
            return
        }
        updateGPSState()

        if (permissionManager.hasPermission()) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 800, 0f, gpsLocationListener)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                locationManager.requestLocationUpdates(LocationManager.FUSED_PROVIDER, 800, 0f, fusedLocationListener)
            }
        }
        enabled = true
    }

    fun disable() {
        if (!enabled) {
            return
        }
        enabled = false
        updateGPSState()
        locationManager.removeUpdates(gpsLocationListener)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            locationManager.removeUpdates(fusedLocationListener)
        }
    }

    private fun updateSpeedState() {
        val speed = currentSpeed
        _speedState.value = when {
            !enabled -> SpeedState.Disabled
            speed != null -> SpeedState.SpeedChanged(speed)
            !isGPSEnabled -> SpeedState.GPSDisabled
            else -> SpeedState.SpeedUnavailable
        }
    }

    override fun destroy() {
        disable()
    }
}
