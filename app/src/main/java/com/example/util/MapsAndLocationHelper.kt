package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MapsAndLocationHelper {

    /**
     * Opens the lesson or student location in the user's default maps application.
     * Prioritizes coordinates, then maps link, then address/area name.
     */
    fun openInMaps(
        context: Context,
        latitude: Double? = null,
        longitude: Double? = null,
        addressText: String = "",
        areaName: String = "",
        mapsLink: String = "",
        label: String = "Lesson Location"
    ) {
        try {
            val intent = when {
                latitude != null && longitude != null && latitude != 0.0 && longitude != 0.0 -> {
                    val encodedLabel = Uri.encode(label.ifBlank { "Location" })
                    val geoUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($encodedLabel)")
                    Intent(Intent.ACTION_VIEW, geoUri)
                }
                mapsLink.isNotBlank() && (mapsLink.startsWith("http://") || mapsLink.startsWith("https://")) -> {
                    Intent(Intent.ACTION_VIEW, Uri.parse(mapsLink))
                }
                addressText.isNotBlank() || areaName.isNotBlank() -> {
                    val query = listOf(addressText, areaName).filter { it.isNotBlank() }.joinToString(", ")
                    val geoUri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
                    Intent(Intent.ACTION_VIEW, geoUri)
                }
                else -> null
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "No location or address specified", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            // Fallback: search in web browser maps
            val query = listOf(addressText, areaName).filter { it.isNotBlank() }.joinToString(", ")
            if (query.isNotBlank()) {
                try {
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=${Uri.encode(query)}")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(webIntent)
                } catch (ex: Exception) {
                    Toast.makeText(context, "Could not open map", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Could not open map", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Launches turn-by-turn navigation in the user's default navigation application.
     */
    fun navigate(
        context: Context,
        latitude: Double? = null,
        longitude: Double? = null,
        addressText: String = "",
        areaName: String = "",
        label: String = "Destination"
    ) {
        try {
            val intent = when {
                latitude != null && longitude != null && latitude != 0.0 && longitude != 0.0 -> {
                    val navUri = Uri.parse("google.navigation:q=$latitude,$longitude")
                    Intent(Intent.ACTION_VIEW, navUri)
                }
                addressText.isNotBlank() || areaName.isNotBlank() -> {
                    val query = listOf(addressText, areaName).filter { it.isNotBlank() }.joinToString(", ")
                    val navUri = Uri.parse("google.navigation:q=${Uri.encode(query)}")
                    Intent(Intent.ACTION_VIEW, navUri)
                }
                else -> null
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                openInMaps(context, latitude, longitude, addressText, areaName, label = label)
            }
        } catch (e: Exception) {
            // Fallback to standard open in maps
            openInMaps(context, latitude, longitude, addressText, areaName, label = label)
        }
    }

    /**
     * Opens an online meeting link (e.g. Google Meet, Zoom, MS Teams).
     */
    fun openMeetingUrl(context: Context, url: String) {
        if (url.isBlank()) {
            Toast.makeText(context, "No meeting URL provided", Toast.LENGTH_SHORT).show()
            return
        }
        val cleanUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cleanUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open meeting link", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Foreground single-location capture ONLY when explicitly triggered by tutor action.
     * No background tracking.
     */
    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(
        context: Context,
        onLocationResult: (Double, Double) -> Unit,
        onError: (String) -> Unit
    ) {
        val finePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarsePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (finePerm != PackageManager.PERMISSION_GRANTED && coarsePerm != PackageManager.PERMISSION_GRANTED) {
            onError("Location permission not granted.")
            return
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            onError("Location service unavailable.")
            return
        }

        try {
            // Check last known first for fast response
            val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            var bestLoc: Location? = null
            for (p in providers) {
                if (locationManager.isProviderEnabled(p)) {
                    val loc = locationManager.getLastKnownLocation(p)
                    if (loc != null && (bestLoc == null || loc.accuracy < bestLoc.accuracy)) {
                        bestLoc = loc
                    }
                }
            }

            if (bestLoc != null) {
                onLocationResult(bestLoc.latitude, bestLoc.longitude)
                return
            }

            // Request single update if no cached location
            val provider = if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                LocationManager.NETWORK_PROVIDER
            } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                LocationManager.GPS_PROVIDER
            } else null

            if (provider == null) {
                onError("Please enable GPS or Location on your device.")
                return
            }

            val listener = object : LocationListener {
                override fun onLocationChanged(loc: Location) {
                    locationManager.removeUpdates(this)
                    onLocationResult(loc.latitude, loc.longitude)
                }
                override fun onStatusChanged(p: String?, s: Int, e: Bundle?) {}
                override fun onProviderEnabled(p: String) {}
                override fun onProviderDisabled(p: String) {}
            }

            locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
        } catch (e: Exception) {
            onError("Error accessing location: ${e.localizedMessage}")
        }
    }

    /**
     * Calculates suggested departure time formatted (e.g., "Leave by 4:25 PM").
     */
    fun formatDepartureSuggestion(
        startEpochMillis: Long,
        travelTimeMinutes: Int?,
        bufferMinutes: Int = 10
    ): String? {
        if (travelTimeMinutes == null || travelTimeMinutes <= 0) return null
        val totalLeadMillis = (travelTimeMinutes + bufferMinutes) * 60 * 1000L
        val departureEpoch = startEpochMillis - totalLeadMillis
        val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
        return "Leave by ${timeFormat.format(Date(departureEpoch))}"
    }
}
