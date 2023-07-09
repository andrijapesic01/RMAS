   package elfak.mosis.iride

    import CarListAdapter
    import android.app.Dialog
    import android.content.Intent
    import android.location.Location
    import androidx.appcompat.app.AppCompatActivity
    import android.os.Bundle
    import android.util.Log
    import android.view.View
    import android.widget.AdapterView
    import android.widget.ArrayAdapter
    import android.widget.Button
    import android.widget.EditText
    import android.widget.ImageButton
    import android.widget.ImageView
    import android.widget.Spinner
    import android.widget.TextView
    import android.widget.Toast
    import androidx.appcompat.app.AlertDialog
    import androidx.recyclerview.widget.GridLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import com.bumptech.glide.Glide
    import com.google.firebase.database.DataSnapshot
    import com.google.firebase.database.DatabaseError
    import com.google.firebase.database.DatabaseReference
    import com.google.firebase.database.FirebaseDatabase
    import com.google.firebase.database.GenericTypeIndicator
    import com.google.firebase.database.ValueEventListener
    import com.google.firebase.storage.FirebaseStorage
    import com.google.firebase.storage.StorageReference
    import java.nio.charset.Charset

   class MainActivity : AppCompatActivity(), CarListAdapter.OnCarClickListener {

        private lateinit var btnMap: ImageButton
        private lateinit var addCar: ImageButton
        private lateinit var filterBtn: ImageButton
        private lateinit var profileBtn: ImageButton
        private lateinit var databaseReference: DatabaseReference
        private lateinit var carListRecyclerView: RecyclerView
        private lateinit var carListAdapter: CarListAdapter
        private lateinit var storage: FirebaseStorage
        private lateinit var storageReference: StorageReference
        private lateinit var carList: MutableList<Car>

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            storage = FirebaseStorage.getInstance()
            storageReference = storage.reference.child("brands_models")
            databaseReference = FirebaseDatabase.getInstance().reference

            carList = mutableListOf()

            initView()
            //retrieveAllCars()

            addCar = findViewById(R.id.btnAddCar)
            btnMap = findViewById(R.id.btnMap)
            filterBtn = findViewById(R.id.btnFilterMain)
            profileBtn = findViewById(R.id.btnProfile)

            btnMap.setOnClickListener{
                val intentMap = Intent(this, MapActivity::class.java)
                //intentMap.putExtra("carList", ArrayList(carList))
                startActivity(intentMap)
            }

            addCar.setOnClickListener{
                val intentAddCar = Intent(this, AddCarActivity::class.java)
                startActivity(intentAddCar)
            }

            filterBtn.setOnClickListener{
                showFilterDialog()
            }

            profileBtn.setOnClickListener{
                val intentProfile = Intent(this, ProfileActivity::class.java)
                startActivity(intentProfile)
            }

        }

        override fun onResume() {
            super.onResume()
            retrieveAllCars()
        }

        private fun initView() {
            // Find the car list RecyclerView from the layout
            carListRecyclerView = findViewById(R.id.carListRecyclerView)

            // Set up the RecyclerView with a layout manager
            carListRecyclerView.layoutManager = GridLayoutManager(this, 2)

            // Create an instance of the CarListAdapter
            carListAdapter = CarListAdapter(emptyList())

            // Set the adapter on the RecyclerView
            carListRecyclerView.adapter = carListAdapter
        }

       /* private fun retrieveAllCars() {
            val usersRef = databaseReference.child("users")

            usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    //val carList = mutableListOf<Car>()
                    carList.clear()

                    // Iterate through all users
                    for (userSnapshot in dataSnapshot.children) {
                        val userId = userSnapshot.key

                        if (userId != null) {
                            val carsRef = databaseReference.child("users").child(userId).child("cars")

                            carsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(carsSnapshot: DataSnapshot) {
                                    // Retrieve all cars for the current user
                                    for (carSnapshot in carsSnapshot.children) {
                                        // Manually deserialize the Car object
                                        val carMap = carSnapshot.getValue(object : GenericTypeIndicator<HashMap<String, Any>>() {})
                                        if (carMap != null) {
                                            val rented = carMap["rented"] as? Boolean ?: false

                                            if (!rented) {
                                                // Convert latitude and longitude to Location object
                                                val latitude = carMap["latitude"] as? Double ?: 0.0
                                                val longitude = carMap["longitude"] as? Double ?: 0.0
                                                val location = Location("CarLocation").apply {
                                                    this.latitude = latitude
                                                    this.longitude = longitude
                                                }

                                                // Create a Car object with the deserialized values
                                                val carObject = Car(
                                                    userId,
                                                    carMap["brand"] as? String ?: "",
                                                    carMap["model"] as? String ?: "",
                                                    carMap["fuel"] as? String ?: "",
                                                    carMap["category"] as? String ?: "",
                                                    carMap["year"] as? Int ?: 0,
                                                    carMap["transmission"] as? String ?: "",
                                                    location,
                                                    carMap["carImage"] as? String ?: "",
                                                    carMap["rented"] as? Boolean ?: false,
                                                    carMap["openKey"] as? String ?: ""
                                                )

                                                // You can also log the car object as a whole
                                                //Log.d("CarData", "Car Object: $carObject")

                                                // Add the car to the list
                                                carList.add(carObject)
                                            }
                                        }
                                    }

                                    carListAdapter.setOnCarClickListener(this@MainActivity)

                                    // Update the adapter with the new car list
                                    carListAdapter.setCars(carList)
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // Handle any errors that occur during the retrieval
                                }
                            })
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle any errors that occur during the retrieval
                }
            })
        }*/
        private fun retrieveAllCars() {
           CarUtils.retrieveAllCars() { cars ->
               carList = ArrayList(cars)
               Log.d("Car List:","$carList")

               carListAdapter.setOnCarClickListener(this@MainActivity)
               // Update the adapter with the new car list
               carListAdapter.setCars(carList)
           }
        }

        private fun showFilterDialog() {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.filter_dialog)

            var selectedBrand: String? = null
            var selectedModel: String? = null
            var selectedCategory: String? = null
            var selectedFuelType: String? = null
            var selectedTransmissionType: String? = null
            var selectedYearFrom: Number? = null
            var selectedYearTo: Number? = null

            val keyword: EditText = dialog.findViewById(R.id.keywordInput)
            val keywordSearchButton: ImageButton = dialog.findViewById(R.id.keyWordSearch)
            val applyButton: Button = dialog.findViewById(R.id.applyButton)
            val cancelButton: Button = dialog.findViewById(R.id.resetBtn)
            val categorySpinner: Spinner = dialog.findViewById(R.id.categorySpinner1)
            val fuelTypeSpinner: Spinner = dialog.findViewById(R.id.fuelTypeSpinner1)
            val transmissionSpinner: Spinner = dialog.findViewById(R.id.transmissionSpinner1)
            val brandSpinner: Spinner = dialog.findViewById(R.id.brandSpinner1)
            val modelSpinner: Spinner = dialog.findViewById(R.id.modelSpinner1)
            val yearFrom : EditText = dialog.findViewById(R.id.yearFromDP)
            val yearTo : EditText = dialog.findViewById(R.id.yearToDP)

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

            keywordSearchButton.setOnClickListener{
                //Toast.makeText(this, "Results for: $keyword", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                filterByKeyword(keyword)
            }

            // Set click listeners for buttons
            applyButton.setOnClickListener {
                // Handle the "Apply" button click
                selectedYearFrom = yearFrom.text.toString().toIntOrNull()
                selectedYearTo = yearTo.text.toString().toIntOrNull()

                Toast.makeText(this, "Filter applied", Toast.LENGTH_SHORT).show()
                dialog.dismiss() // Dismiss the dialog

                filter(selectedBrand, selectedModel, selectedCategory, selectedFuelType, selectedTransmissionType, selectedYearFrom, selectedYearTo)


                // Use the selectedCategory and selectedFuelType for filtering
                // Add your filtering logic here
            }

            cancelButton.setOnClickListener {
                // Handle the "Cancel" button click
                dialog.dismiss() // Dismiss the dialog
                retrieveAllCars()
            }

            // Show the custom dialog
            dialog.show()
        }

        private fun filterByKeyword(keyword: EditText) {
            //Delete
        }

        private fun filter(selectedBrand: String?, selectedModel: String?, selectedCategory: String?, selectedFuelType: String?, selectedTransmissionType: String?, selectedYearFrom: Number?, selectedYearTo: Number?) {
            val filteredCars = mutableListOf<Car>()

            for (car in carList) {
                val matchesBrand = selectedBrand == null || car.brand == selectedBrand
                val matchesModel = selectedModel == null || car.model == selectedModel
                val matchesCategory = selectedCategory == null || car.category == selectedCategory
                val matchesFuelType = selectedFuelType == null || car.fuel == selectedFuelType
                val matchesTransmissionType = selectedTransmissionType == null || car.transmission == selectedTransmissionType
                val matchesYearFrom = selectedYearFrom == null || car.year >= selectedYearFrom.toInt()
                val matchesYearTo = selectedYearTo == null || car.year <= selectedYearTo.toInt()

                if (matchesBrand && matchesModel && matchesCategory && matchesFuelType && matchesTransmissionType && matchesYearFrom && matchesYearTo) {
                    filteredCars.add(car)
                }
            }

            if (filteredCars.isEmpty()) {
                val alertDialog = AlertDialog.Builder(this)
                    .setTitle("No Cars Found")
                    .setMessage("No cars match the selected filter criteria.")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .create()

                alertDialog.show()
            } else {
                carListAdapter.setCars(filteredCars)
                carListAdapter.notifyDataSetChanged()
            }
        }

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
                // Handle failure to retrieve brands
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
                // Handle failure to retrieve models
            }
        }

        override fun onCarClick(car: Car) {
            showCarDialog(car)
        }

        //Add rating
        private fun showCarDialog(car: Car) {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.car_dialog)

            // Initialize and set up the dialog content with car details
            val dialogCarPic: ImageView = dialog.findViewById(R.id.dialogCarPic)
            val textViewCarMake: TextView = dialog.findViewById(R.id.cdBrand)
            val textViewCarModel: TextView = dialog.findViewById(R.id.cdModel)
            val textViewCarYear: TextView = dialog.findViewById(R.id.cdYear)
            val textViewCarCategory: TextView = dialog.findViewById(R.id.cdCategory)
            val textViewCarFuelType: TextView = dialog.findViewById(R.id.cdFuel)
            val textViewCarTransmission: TextView = dialog.findViewById(R.id.cdTransmission)
            val textViewCarRating: TextView = dialog.findViewById(R.id.cdRating)
            val rentButton: Button = dialog.findViewById(R.id.btnRentCar)

            val ratingText = String.format("(%d) %.1f/5", car.numOfRatings, car.rating)
            // Set car data to the views
            Glide.with(dialog.context)
                .load(car.carImage)
                .placeholder(R.drawable.car_placeholder)
                .into(dialogCarPic)

            textViewCarMake.text = car.brand
            textViewCarModel.text = car.model
            textViewCarYear.text = car.year.toString()
            textViewCarCategory.text = car.category
            textViewCarFuelType.text = car.fuel
            textViewCarTransmission.text = car.transmission
            textViewCarRating.text = ratingText

            rentButton.setOnClickListener{
                val intentRentCar = Intent(this, CarRentActivity::class.java)
                intentRentCar.putExtra("car", car)
                startActivity(intentRentCar)
                finish()
            }

            dialog.show()
        }

    }

/*
package elfak.mosis.iride

import CarListAdapter
import android.app.Dialog
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.nio.charset.Charset

class MainActivity : AppCompatActivity(), CarListAdapter.OnCarClickListener {

    private lateinit var btnMap: ImageButton
    private lateinit var addCar: ImageButton
    private lateinit var filterBtn: ImageButton
    private lateinit var profileBtn: ImageButton
    private lateinit var databaseReference: DatabaseReference
    private lateinit var carListRecyclerView: RecyclerView
    private lateinit var carListAdapter: CarListAdapter
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference.child("brands_models")
        databaseReference = FirebaseDatabase.getInstance().reference

        retrieveAllCars()
        initView()


        addCar = findViewById(R.id.btnAddCar)
        btnMap = findViewById(R.id.btnMap)
        filterBtn = findViewById(R.id.btnFilterMain)
        profileBtn = findViewById(R.id.btnProfile)

        btnMap.setOnClickListener{
            val intentRentCar = Intent(this, MapActivity::class.java)
            startActivity(intentRentCar)
        }

        addCar.setOnClickListener{
            val intentAddCar = Intent(this, AddCarActivity::class.java)
            startActivity(intentAddCar)
        }

        filterBtn.setOnClickListener{
            CarUtils.showFilterDialog(this)
        }

        profileBtn.setOnClickListener{
            val intentProfile = Intent(this, ProfileActivity::class.java)
            startActivity(intentProfile)
        }

    }

    override fun onResume() {
        super.onResume()
        retrieveAllCars()
    }

    private fun initView() {
        // Find the car list RecyclerView from the layout
        carListRecyclerView = findViewById(R.id.carListRecyclerView)

        // Set up the RecyclerView with a layout manager
        carListRecyclerView.layoutManager = GridLayoutManager(this, 2)

        // Create an instance of the CarListAdapter
        carListAdapter = CarListAdapter(emptyList())

        // Set the adapter on the RecyclerView
        carListRecyclerView.adapter = carListAdapter
    }

    */
/*private fun retrieveAllCars() {
        CarUtils.retrieveAllCars(databaseReference, this) { carList ->
            carListAdapter.setOnCarClickListener(this)

            // Update the adapter with the new car list
            carListAdapter.setCars(carList)
        }
    }*//*


    private fun retrieveAllCars() {
        CarUtils.retrieveAllCars(databaseReference, this) { carList ->
            carListAdapter.setOnCarClickListener(this)

            // Update the adapter with the new car list
            carListAdapter.setCars(carList)

            // Notify the adapter that the data set has changed
            carListAdapter.notifyDataSetChanged()
        }
    }


    override fun onCarClick(car: Car) {
        CarUtils.showCarDialog(this, car)
    }
}

*/
