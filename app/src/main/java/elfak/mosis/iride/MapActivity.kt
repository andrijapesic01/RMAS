package elfak.mosis.iride

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.nio.charset.Charset
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var filter: ImageButton
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var carList: ArrayList<Car>


    private val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
    private val CAMERA_POSITION_KEY = "CameraPositionKey"
    private var mapViewBundle: Bundle? = null
    private var savedCameraPosition: CameraPosition? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        //carList = intent.getSerializableExtra("carList") as? ArrayList<Car> ?: ArrayList()
        //Log.d("MapActivity", "Car List: $carList")

        filter = findViewById(R.id.filterBtn)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState?.getBundle(MAPVIEW_BUNDLE_KEY))
        mapView.getMapAsync(this)

        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference.child("brands_models")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation = locationResult.lastLocation
                val yourLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                //googleMap.addMarker(MarkerOptions().position(yourLocation).title("Your Location"))
                if(savedCameraPosition == null)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(yourLocation, 15f))
            }
        }

        filter.setOnClickListener {
            showFilterDialog()
        }

        mapView.getMapAsync(this)
    }

    private fun addMarkersToMap(carList: ArrayList<Car>) {

        for (car in carList) {
            val latitude = car.latitude.toDouble()
            val longitude = car.longitude.toDouble()
            val carLocation = LatLng(latitude, longitude)

            val markerOptions = MarkerOptions()
                .position(carLocation)
                .title(car.brand + " " + car.model + " (" + car.year + ")")

            val snippet = "Rating: ${car.rating} / 5 (${car.numOfRatings})"
            markerOptions.snippet(snippet)

            val marker = googleMap.addMarker(markerOptions)
            marker?.tag = car
            marker?.showInfoWindow()
        }

        googleMap.setOnMarkerClickListener { clickedMarker ->
            val car = clickedMarker.tag as? Car
            if (car != null) {
                CarUtils.showCarDialog(this, car)
                true
            } else {
                false
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
        outState.putParcelable(CAMERA_POSITION_KEY, savedCameraPosition)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedCameraPosition = savedInstanceState.getParcelable(CAMERA_POSITION_KEY)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()

        savedCameraPosition?.let { position ->
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(position))
            savedCameraPosition = null
        }
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()

        savedCameraPosition = googleMap.cameraPosition
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } else {
            showLocationSettingsDialog()
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun showLocationSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Location Permission Required")
            .setMessage("Please grant location permission to use this feature.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /*private fun showFilterDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.filter_dialog)

        var selectedBrand: String? = null
        var selectedModel: String? = null
        var selectedCategory: String? = null
        var selectedFuelType: String? = null
        var selectedTransmissionType: String? = null

        val applyButton: Button = dialog.findViewById(R.id.applyButton)
        val cancelButton: Button = dialog.findViewById(R.id.resetBtn)
        val categorySpinner: Spinner = dialog.findViewById(R.id.categorySpinner1)
        val fuelTypeSpinner: Spinner = dialog.findViewById(R.id.fuelTypeSpinner1)
        val transmissionSpinner: Spinner = dialog.findViewById(R.id.transmissionSpinner1)
        val brandSpinner: Spinner = dialog.findViewById(R.id.brandSpinner1)
        val modelSpinner: Spinner = dialog.findViewById(R.id.modelSpinner1)

        retrieveBrands(dialog, brandSpinner)

        val categoryListWithNull = listOf("All categories") + categories
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryListWithNull)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter

        val fuelTypeListWithNull = listOf("All fuel types") + fuelTypes
        val fuelTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, fuelTypeListWithNull)
        fuelTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fuelTypeSpinner.adapter = fuelTypeAdapter

        val transmissionTypesWithNull = listOf("All transmission types", "Manual", "Automatic")
        val transmissionTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, transmissionTypesWithNull)
        transmissionTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        transmissionSpinner.adapter = transmissionTypeAdapter

        //On items selected
        brandSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val brand = if (position == 0) null else parent?.getItemAtPosition(position) as String
                selectedBrand = brand
                selectedBrand?.let { retrieveModels(dialog, it, modelSpinner) }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedBrand = null
            }
        }

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategory = if (position == 0) null else parent?.getItemAtPosition(position) as String
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCategory = null
            }
        }

        fuelTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedFuelType = if (position == 0) null else parent?.getItemAtPosition(position) as String
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedFuelType = null
            }
        }

        transmissionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedTransmissionType = if (position == 0) null else parent?.getItemAtPosition(position) as String
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedTransmissionType = null
            }
        }

        // Set click listeners for buttons
        applyButton.setOnClickListener {
            // Handle the "Apply" button click
            Toast.makeText(this, "Filter applied", Toast.LENGTH_SHORT).show()
            dialog.dismiss() // Dismiss the dialog

            //TODO(filter here)
            //filter(selectedBrand, selectedModel, selectedCategory, selectedFuelType, selectedTransmissionType)

        }

        cancelButton.setOnClickListener {
            // Handle the "Cancel" button click
            dialog.dismiss() // Dismiss the dialog
        }

        // Show the custom dialog
        dialog.show()
    }*/

    private fun retrieveBrands(dialog: Dialog, spinner: Spinner) {
        val brandsRef = storageReference.child("brands.txt")
        brandsRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val brandsText = String(bytes, Charset.forName("UTF-8"))

            val brandList = brandsText.split("\n")
            val brandListWithNull = listOf("All brands") + brandList

            val brandAdapter = ArrayAdapter(
                dialog.context, android.R.layout.simple_spinner_item, brandListWithNull
            )
            brandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            val brandSpinner = spinner
                //dialog.findViewById<Spinner>(R.id.brandSpinner1)
            brandSpinner.adapter = brandAdapter
        }.addOnFailureListener { exception ->
            Log.e("Exception", exception.toString())
        }
    }

    private fun retrieveModels(dialog: Dialog, brand: String, spinner: Spinner) {
        val lowercaseBrand = brand.trim().lowercase()
        val brandModelsRef = storageReference.child("$lowercaseBrand.txt")
        brandModelsRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val modelsText = String(bytes, Charset.forName("UTF-8"))

            val modelList = modelsText.split("\n")
            val modelListWithNull = listOf("All models") + modelList

            val modelAdapter = ArrayAdapter(
                dialog.context, android.R.layout.simple_spinner_item, modelListWithNull
            )
            modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            val modelSpinner = spinner//= dialog.findViewById<Spinner>(R.id.modelSpinner)
            modelSpinner.adapter = modelAdapter
        }.addOnFailureListener { exception ->
            Log.e("Exception", exception.toString())
        }
    }

    private fun showFilterDialog(){
        CarUtils.showFilterDialog(this)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    /*override fun onMapReady(p0: GoogleMap) {
        p0?.let {
            googleMap = it

        }
    }*/

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        }

        CarUtils.retrieveAllCars { cars ->
            carList = ArrayList(cars)
            Log.d("MapActivity", "Car List: $carList")

            addMarkersToMap(carList)

        }
    }

}
