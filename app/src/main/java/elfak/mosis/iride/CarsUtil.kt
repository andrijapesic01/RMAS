    package elfak.mosis.iride

    import android.app.Dialog
    import android.content.Context
    import android.location.Location
    import android.view.View
    import android.widget.AdapterView
    import android.widget.ArrayAdapter
    import android.widget.Button
    import android.widget.EditText
    import android.widget.Spinner
    import android.widget.TextView
    import android.widget.Toast
    import com.google.firebase.database.DataSnapshot
    import com.google.firebase.database.DatabaseError
    import com.google.firebase.database.DatabaseReference
    import com.google.firebase.database.FirebaseDatabase
    import com.google.firebase.database.GenericTypeIndicator
    import com.google.firebase.database.ValueEventListener

    object CarUtils {

        private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference

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

                                                if (carObject == car) {
                                                    val carToUpdateRef = carSnapshot.ref

                                                    val currentRating = carObject.rating
                                                    val numOfRatings = carObject.numOfRatings

                                                    val newNumOfRatings = numOfRatings + 1
                                                    val newRating = ((currentRating * numOfRatings) + rating) / newNumOfRatings

                                                    // Update the rating and numOfRatings fields
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
        }

        fun retrieveAllCars(callback: (List<Car>) -> Unit) {
            val carList = mutableListOf<Car>()

            val usersRef = databaseReference.child("users")

            usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    //val carList = mutableListOf<Car>()
                    //carList.clear()

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

                                                // You can also log the car object as a whole
                                                //Log.d("CarData", "Car Object: $carObject")

                                                // Add the car to the list
                                                carList.add(carObject)
                                            }
                                        }
                                    }
                                    callback(carList)
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
        }

        fun retrieveBrands(databaseReference: DatabaseReference, callback: (List<String>) -> Unit) {
            val brandList = mutableListOf<String>()
            databaseReference.child("brands").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (brandSnapshot in snapshot.children) {
                            val brand = brandSnapshot.getValue(String::class.java)
                            brand?.let { brandList.add(it) }
                        }
                    }
                    callback(brandList)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(brandList)
                }
            })
        }

        fun retrieveModels(databaseReference: DatabaseReference, brand: String, callback: (List<String>) -> Unit) {
            val modelList = mutableListOf<String>()
            databaseReference.child("models").child(brand).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (modelSnapshot in snapshot.children) {
                            val model = modelSnapshot.getValue(String::class.java)
                            model?.let { modelList.add(it) }
                        }
                    }
                    callback(modelList)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(modelList)
                }
            })
        }

        fun showCarDialog(context: Context, car: Car) {
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.car_dialog)

            val brandTextView: TextView = dialog.findViewById(R.id.cdBrand)
            val modelTextView: TextView = dialog.findViewById(R.id.cdModel)
            val yearTextView: TextView = dialog.findViewById(R.id.cdYear)
            val categoryTextView: TextView = dialog.findViewById(R.id.cdCategory)
            val fuelTypeTextView: TextView = dialog.findViewById(R.id.cdFuel)
            val transmissionTextView: TextView = dialog.findViewById(R.id.cdTransmission)
            val rentButton: Button = dialog.findViewById(R.id.btnRentCar)

            brandTextView.text = car.brand
            modelTextView.text = car.model
            yearTextView.text = car.year.toString()
            categoryTextView.text = car.category
            fuelTypeTextView.text = car.fuel
            transmissionTextView.text = car.transmission

            dialog.show()
        }

        fun showFilterDialog(context: Context) {
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.filter_dialog)

            val filterBtn: Button = dialog.findViewById(R.id.applyButton)
            val closeBtn: Button = dialog.findViewById(R.id.resetBtn)
            val brandSpinner: Spinner = dialog.findViewById(R.id.brandSpinner1)
            val modelSpinner: Spinner = dialog.findViewById(R.id.modelSpinner1)
            val yearEditText: EditText = dialog.findViewById(R.id.yearFromDP)
            val yearToEditText: EditText = dialog.findViewById(R.id.yearToDP)
            val fuelTypeSpinner: Spinner = dialog.findViewById(R.id.fuelTypeSpinner1)
            val transmissionSpinner: Spinner = dialog.findViewById(R.id.transmissionSpinner1)
            val categorySpinner: Spinner = dialog.findViewById(R.id.categorySpinner1)

            retrieveBrands(FirebaseDatabase.getInstance().reference) { brands ->
                val brandAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, brands)
                brandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                brandSpinner.adapter = brandAdapter

                brandSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val selectedBrand = parent?.getItemAtPosition(position).toString()
                        retrieveModels(FirebaseDatabase.getInstance().reference, selectedBrand) { models ->
                            val modelAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, models)
                            modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            modelSpinner.adapter = modelAdapter
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Do nothing
                    }
                }
            }

            filterBtn.setOnClickListener {
                val selectedBrand = brandSpinner.selectedItem.toString()
                val selectedModel = modelSpinner.selectedItem.toString()
                val enteredYearFrom = yearEditText.text.toString().toIntOrNull()
                val enteredYearTo = yearToEditText.text.toString().toIntOrNull()
                val selectedFuelType = fuelTypeSpinner.selectedItem.toString()
                val selectedTransmission = transmissionSpinner.selectedItem.toString()
                val selectedCategory = categorySpinner.selectedItem.toString()

                // Apply filter logic
                // ...

                dialog.dismiss()
            }

            closeBtn.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

    }
