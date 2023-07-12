package elfak.mosis.iride

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

/*object MapUtils {
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private const val LOCATION_PERMISSION_REQUEST_CODE = 1001

    @RequiresApi(Build.VERSION_CODES.O)
    fun initLocationUpdates(
        context: Context,
        onMapReadyCallback: (GoogleMap) -> Unit
    ) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        createLocationRequest()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation = locationResult.lastLocation
                val yourLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                if (googleMap.cameraPosition == null) {
                    val cameraPosition = CameraUpdateFactory.newLatLngZoom(yourLocation, 15f)
                    googleMap.moveCamera(cameraPosition)
                }
            }
        }

        val mapView = MapView(context)
        val callback = object : OnMapReadyCallback {
            override fun onMapReady(map: GoogleMap) {
                googleMap = map
                onMapReadyCallback(map)
            }
        }
        mapView.onCreate(Bundle())
        mapView.getMapAsync(callback)
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }
    }

    private fun requestLocationUpdates(activity: AppCompatActivity) {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }


    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun checkLocationPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestLocationPermissions(activity: AppCompatActivity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun showLocationSettingsDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Location Permission Required")
            .setMessage("Please enable location services to use this feature.")
            .setPositiveButton("OK") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
                val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                context.startActivity(settingsIntent)
            }
            .setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            .show()
    }

    fun addMarkersToMap(googleMap: GoogleMap, carList: List<Car>) {
        for (car in carList) {
            val latitude = car.latitude.toDouble()
            val longitude = car.longitude.toDouble()
            val carLocation = LatLng(latitude, longitude)

            val markerOptions = MarkerOptions()
                .position(carLocation)
                .title("${car.brand} ${car.model} (${car.year})")

            val snippet = "Rating: ${car.rating} / 5 (${car.numOfRatings})"
            markerOptions.snippet(snippet)

            googleMap.addMarker(markerOptions)
        }
    }
}*/