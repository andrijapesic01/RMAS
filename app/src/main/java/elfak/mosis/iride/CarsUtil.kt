    package elfak.mosis.iride

    import android.app.Dialog
    import android.content.Context
    import android.content.Intent
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
    import androidx.core.content.ContextCompat.startActivity
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

    object CarUtils {

        private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
        private val storage: FirebaseStorage = FirebaseStorage.getInstance()
        private val storageReference: StorageReference = storage.reference.child("brands_models")

        fun rateCar(car: Car, rating: Float) {
            val usersRef = databaseReference.child("users")

            usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (userSnapshot in dataSnapshot.children) {
                        val userId = userSnapshot.key

                        if (userId != null) {
                            val carsRef = databaseReference.child("users").child(userId).child("cars")

                            carsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(carsSnapshot: DataSnapshot) {
                                    for (carSnapshot in carsSnapshot.children) {
                                        val carMap = carSnapshot.getValue(object : GenericTypeIndicator<HashMap<String, Any>>() {})
                                        if (carMap != null) {
                                            val rented = carMap["rented"] as? Boolean ?: false

                                            if (rented) {
                                                val carObject = Car(
                                                    userId,
                                                    carMap["brand"] as? String ?: "",
                                                    carMap["model"] as? String ?: "",
                                                    carMap["fuel"] as? String ?: "",
                                                    carMap["category"] as? String ?: "",
                                                    (carMap["year"] as? String)?.toIntOrNull() ?: 0,
                                                    carMap["transmission"] as? String ?: "",
                                                    carMap["latitude"] as? Number ?: 0,
                                                    carMap["longitude"] as? Number ?: 0,
                                                    carMap["carImage"] as? String ?: "",
                                                    carMap["rented"] as? Boolean ?: false,
                                                    carMap["openKey"] as? String ?: "",
                                                    carMap["rating"] as? Float ?: 0f,
                                                    carMap["numOfRatings"] as? Int ?: 0
                                                )

                                                if (carObject == car) {
                                                    val carToUpdateRef = carSnapshot.ref

                                                    val currentRating = carObject.rating
                                                    val numOfRatings = carObject.numOfRatings

                                                    val newNumOfRatings = numOfRatings + 1
                                                    val newRating = ((currentRating * numOfRatings) + rating) / newNumOfRatings

                                                    carToUpdateRef.child("rating").setValue(newRating)
                                                        .addOnSuccessListener {
                                                            carToUpdateRef.child("numOfRatings").setValue(newNumOfRatings)
                                                                .addOnSuccessListener {
                                                                    println("Rating and numOfRatings updated successfully.")
                                                                }
                                                                .addOnFailureListener { e ->
                                                                    println("Error updating numOfRatings: $e")
                                                                }
                                                        }
                                                        .addOnFailureListener { e ->
                                                            println("Error updating rating: $e")
                                                        }

                                                    return
                                                }
                                            }
                                        }
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    Log.e("Database Error", databaseError.toString())
                                }
                            })
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Database Error", databaseError.toString())
                }
            })
        }

        fun retrieveAllCars(callback: (List<Car>) -> Unit) {
            val carList = mutableListOf<Car>()

            val usersRef = databaseReference.child("users")

            usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    //val carList = mutableListOf<Car>()
                    //carList.clear()

                    for (userSnapshot in dataSnapshot.children) {
                        val userId = userSnapshot.key

                        if (userId != null) {
                            val carsRef = databaseReference.child("users").child(userId).child("cars")

                            carsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(carsSnapshot: DataSnapshot) {
                                    for (carSnapshot in carsSnapshot.children) {
                                        val carMap = carSnapshot.getValue(object : GenericTypeIndicator<HashMap<String, Any>>() {})
                                        if (carMap != null) {
                                            val rented = carMap["rented"] as? Boolean ?: false

                                            if (!rented) {
                                                val carObject = Car(
                                                    userId,
                                                    carMap["brand"] as? String ?: "",
                                                    carMap["model"] as? String ?: "",
                                                    carMap["fuel"] as? String ?: "",
                                                    carMap["category"] as? String ?: "",
                                                    (carMap["year"] as? String)?.toIntOrNull() ?: 0,
                                                    carMap["transmission"] as? String ?: "",
                                                    carMap["latitude"] as? Number ?: 0,
                                                    carMap["longitude"] as? Number ?: 0,
                                                    carMap["carImage"] as? String ?: "",
                                                    carMap["rented"] as? Boolean ?: false,
                                                    carMap["openKey"] as? String ?: "",
                                                    carMap["rating"] as? Float ?: 0f,
                                                    carMap["numOfRatings"] as? Int ?: 0
                                                )
                                                //Log.d("CarData", "Car Object: $carObject")
                                                carList.add(carObject)
                                            }
                                        }
                                    }
                                    callback(carList)
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    Log.e("Database Error", databaseError.toString())
                                }
                            })
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Database Error", databaseError.toString())
                }
            })
        }

        fun retrieveBrands(callback: (List<String>) -> Unit) {
            val brandsRef = storageReference.child("brands.txt")
            brandsRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                val brandsText = String(bytes, Charset.forName("UTF-8"))

                val brandList = brandsText.split("\n")
                val brandListWithNull = listOf("All brands") + brandList
                callback(brandListWithNull)
            }.addOnFailureListener { exception ->
                Log.e("Exception", exception.toString())
            }
        }

        fun retrieveBrandsDialog(dialog: Dialog, spinner: Spinner) {
            retrieveBrands { brandList->

                val brandAdapter = ArrayAdapter(
                    dialog.context, android.R.layout.simple_spinner_item, brandList
                )
                brandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = brandAdapter
            }
        }

        fun retrieveModels(brand: String, callback: (List<String>) -> Unit) {
            val lowercaseBrand = brand.trim().lowercase()
            val brandModelsRef = storageReference.child("$lowercaseBrand.txt")
            brandModelsRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                val modelsText = String(bytes, Charset.forName("UTF-8"))
                val modelList = modelsText.split("\n")
                val modelListWithNull = listOf("All models") + modelList
                callback(modelListWithNull)

            }.addOnFailureListener { exception ->
                Log.e("Exception", exception.toString())
            }
        }

        fun retrieveModelsDialog(dialog: Dialog, brand: String, spinner: Spinner) {

            retrieveModels(brand) { modelListWithNull->
                val modelAdapter = ArrayAdapter(
                    dialog.context, android.R.layout.simple_spinner_item, modelListWithNull
                )
                modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                val modelSpinner = spinner
                modelSpinner.adapter = modelAdapter
            }

        }

        fun showCarDialog(context: Context, car: Car) {
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.car_dialog)

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

            /*rentButton.setOnClickListener{
                val intentRentCar = Intent(context, CarRentActivity::class.java)
                intentRentCar.putExtra("car", car)
                startActivity(intentRentCar)
                finish()
            }*/

            dialog.show()
        }

        fun showFilterDialog(context: Context, carList: MutableList<Car>, callback: (MutableList<Car>) -> Unit) {
            val dialog = Dialog(context)
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

            retrieveBrandsDialog(dialog, brandSpinner)

            val categoryListWithNull = listOf("All categories") + categories
            val categoryAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, categoryListWithNull)
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = categoryAdapter

            val fuelTypeListWithNull = listOf("All fuel types") + fuelTypes
            val fuelTypeAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, fuelTypeListWithNull)
            fuelTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            fuelTypeSpinner.adapter = fuelTypeAdapter

            val transmissionTypesWithNull = listOf("All transmission types", "Manual", "Automatic")
            val transmissionTypeAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, transmissionTypesWithNull)
            transmissionTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            transmissionSpinner.adapter = transmissionTypeAdapter

            //On items selected
            brandSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val brand = if (position == 0) null else parent?.getItemAtPosition(position) as String
                    selectedBrand = brand
                    selectedBrand?.let { retrieveModelsDialog(dialog, it, modelSpinner) }
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
                //filterByKeyword(keyword)
            }

            applyButton.setOnClickListener {
                selectedYearFrom = yearFrom.text.toString().toIntOrNull()
                selectedYearTo = yearTo.text.toString().toIntOrNull()

                Toast.makeText(context, "Filter applied", Toast.LENGTH_SHORT).show()
                dialog.dismiss()

                filter(context, carList, selectedBrand, selectedModel, selectedCategory, selectedFuelType, selectedTransmissionType, selectedYearFrom, selectedYearTo) { filteredCars->
                    callback(filteredCars)
                }
            }

            cancelButton.setOnClickListener {
                dialog.dismiss()
                retrieveAllCars { newCarList->
                    callback(newCarList as MutableList<Car>)
                }
            }

            dialog.show()
        }

        fun filter(context: Context, carList: MutableList<Car>, selectedBrand: String?, selectedModel: String?, selectedCategory: String?, selectedFuelType: String?, selectedTransmissionType: String?, selectedYearFrom: Number?, selectedYearTo: Number?, callback: (MutableList<Car>)-> Unit) {
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
                val alertDialog = AlertDialog.Builder(context)
                    .setTitle("No Cars Found")
                    .setMessage("No cars match the selected filter criteria.")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .create()

                alertDialog.show()
            } else {
                callback(filteredCars)
            }
        }

        fun sortByRatingAscending(carList: MutableList<Car>, callback: (MutableList<Car>) -> Unit) {
            carList.sortBy { it.rating }
            callback(carList)
        }

        fun sortByRatingDescending(carList: MutableList<Car>, callback: (MutableList<Car>) -> Unit) {
            carList.sortByDescending { it.rating }
            callback(carList)
        }

    }
