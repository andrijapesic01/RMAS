package elfak.mosis.iride

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.auth.User
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.util.*
import elfak.mosis.iride.categories
import elfak.mosis.iride.fuelTypes

class AddCarActivity : AppCompatActivity() {

    private lateinit var carPicture: ImageView
    private lateinit var brandSpinner: Spinner
    private lateinit var modelSpinner: Spinner
    private lateinit var categorySpinner: Spinner
    private lateinit var fuelTypeSpinner: Spinner
    private lateinit var radioTransmission: RadioGroup
    private lateinit var yearPicker: EditText
    private lateinit var saveBtn: Button

    private lateinit var category: String
    private lateinit var fuel: String
    private lateinit var brand: String
    private lateinit var model: String
    private lateinit var transmission: String

    private lateinit var location: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var imageReference: StorageReference
    private lateinit var auth: FirebaseAuth

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val imageBitmap = result.data?.extras?.get("data") as Bitmap
                carPicture.setImageBitmap(imageBitmap)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_car)
        val fuelTypes = fuelTypes
        val categories = categories

        auth = FirebaseAuth.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference.child("brands_models")
        imageReference = storage.reference
        databaseReference = FirebaseDatabase.getInstance().reference

        brandSpinner = findViewById(R.id.brandSpinner)
        modelSpinner = findViewById(R.id.modelSpinner)
        categorySpinner = findViewById(R.id.categorySpineer)
        fuelTypeSpinner = findViewById(R.id.fuelSpinner)
        carPicture = findViewById(R.id.carImage)
        yearPicker = findViewById(R.id.yearDP)
        saveBtn = findViewById(R.id.btnSave)

        carPicture.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureLauncher.launch(takePictureIntent)
        }
        retrieveBrands()

        brandSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                brand = parent?.getItemAtPosition(position).toString()
                retrieveModels(brand)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        modelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                model = parent?.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        val categoryAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, categories
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter

        val fuelTypeAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, fuelTypes
        )
        fuelTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fuelTypeSpinner.adapter = fuelTypeAdapter

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                category = parent.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                Toast.makeText(applicationContext, "Please select car category!", Toast.LENGTH_SHORT).show()
            }
        }

        fuelTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                fuel = parent.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                Toast.makeText(applicationContext, "Please select car category!", Toast.LENGTH_SHORT).show()
            }
        }

        radioTransmission = findViewById(R.id.radioTransmission)

        saveBtn.setOnClickListener {
            val year = yearPicker.text.toString()
            val maxYear = Calendar.getInstance().get(Calendar.YEAR)
            val carPicDrawable = carPicture.drawable as BitmapDrawable?
            val carBitmap = carPicDrawable?.bitmap

            if (year == null) {
                Toast.makeText(this, "Please add year of production!", Toast.LENGTH_SHORT).show()
            } else if (year.toInt() > maxYear || year.toInt() < 1900) {
                Toast.makeText(this, "Please enter a valid year of production!", Toast.LENGTH_SHORT).show()
            } else if (transmission.isNullOrBlank()) {
                Toast.makeText(this, "Please select the transmission type!", Toast.LENGTH_SHORT).show()
            } else
                addCar(brand, model, fuel, category, year, transmission, carBitmap)
        }
    }

    @SuppressLint("MissingPermission")
    private fun addCar(brand: String, model: String, fuel: String, category: String, year: String, transmission: String, carBitmap: Bitmap?) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val user = auth.currentUser
                    val userId = user?.uid

                    if (userId != null) {
                        val car = Car(userId, brand, model, fuel, category, year, transmission, location.latitude, location.longitude,"", false, "", "0", "0")

                        val carImageRef = imageReference.child("car_images").child("$userId-${System.currentTimeMillis()}.jpg")
                        carBitmap?.let { bitmap ->
                            val baos = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                            val carImageData = baos.toByteArray()

                            carImageRef.putBytes(carImageData)
                                .addOnSuccessListener { uploadTask ->
                                    carImageRef.downloadUrl
                                        .addOnSuccessListener { uri ->
                                            val imageUrl = uri.toString()
                                            car.carImage = imageUrl
                                            car.dateAdded = System.currentTimeMillis().toString()

                                            val carsRef = databaseReference.child("users").child(userId).child("cars")
                                            carsRef.push().setValue(car)
                                                .addOnSuccessListener {
                                                    val userRef = databaseReference.child("users").child(userId)
                                                    userRef.child("score").get().addOnSuccessListener { dataSnapshot ->
                                                        val currentScore = dataSnapshot.value as? Long ?: 0
                                                        userRef.child("score").setValue(currentScore + 50)
                                                            .addOnSuccessListener {
                                                                Toast.makeText(this, "Car added successfully", Toast.LENGTH_SHORT).show()
                                                                finish()
                                                            }
                                                            .addOnFailureListener { exception ->
                                                                Toast.makeText(this, "Failed to add car", Toast.LENGTH_SHORT).show()
                                                            }
                                                    }.addOnFailureListener { exception ->
                                                        Toast.makeText(this, "Failed to retrieve user score", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                                .addOnFailureListener { exception ->
                                                    Toast.makeText(this, "Failed to add car", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                        .addOnFailureListener { exception ->
                                            Toast.makeText(this, "Failed to get image URL", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(this, "Failed to upload car image", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                } else {
                    Log.e("Location Error", "Location not available!")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Exception", exception.toString())
            }
    }

    fun checkButton(view: View) {
        if (view is RadioButton) {
            val checked = view.isChecked
            if (checked) {
                transmission = view.text.toString()
            }
        }
    }

    private fun retrieveBrands() {
        val brandsRef = storageReference.child("brands.txt")
        brandsRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val brandsText = String(bytes, Charset.forName("UTF-8"))

            val brandList = brandsText.split("\n")
            val brandAdapter = ArrayAdapter(
                this@AddCarActivity, android.R.layout.simple_spinner_item, brandList
            )
            brandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            brandSpinner.adapter = brandAdapter
        }.addOnFailureListener { exception ->
            Log.e("Exception", exception.toString())
        }
    }

    private fun retrieveModels(brand: String) {
        val lowercaseBrand = brand.trim().lowercase()
        val brandModelsRef = storageReference.child("$lowercaseBrand.txt")
        brandModelsRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val modelsText = String(bytes, Charset.forName("UTF-8"))

            val modelList = modelsText.split("\n")
            val modelAdapter = ArrayAdapter(
                this@AddCarActivity, android.R.layout.simple_spinner_item, modelList
            )
            modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            modelSpinner.adapter = modelAdapter
        }.addOnFailureListener { exception ->
            Log.e("Exception", exception.toString())
        }
    }

    private fun requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        this.location = location
                    } else {
                        Log.e("Location Error", "Location not available!")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Exception", exception.toString())
                }
        } else {
            showLocationSettingsDialog()
        }
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

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
