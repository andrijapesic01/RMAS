   package elfak.mosis.iride

    import CarListAdapter
    import android.app.Dialog
    import android.content.Intent
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
    import com.google.firebase.database.DatabaseReference
    import com.google.firebase.database.FirebaseDatabase
    import com.google.firebase.storage.FirebaseStorage
    import com.google.firebase.storage.StorageReference
    import java.nio.charset.Charset

   class MainActivity : AppCompatActivity(), CarListAdapter.OnCarClickListener {

        private lateinit var btnMap: ImageButton
        private lateinit var addCar: ImageButton
        private lateinit var filterBtn: ImageButton
        private lateinit var sortBtn: ImageButton
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
            sortBtn = findViewById(R.id.sortBtn)
            profileBtn = findViewById(R.id.btnProfile)

            btnMap.setOnClickListener{
                val intentMap = Intent(this, MapActivity::class.java)
                intentMap.putExtra("carList", ArrayList(carList))
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

            sortBtn.setOnClickListener {
                showSortDialog()
            }
        }

       private fun showFilterDialog() {
           CarUtils.showFilterDialog(this, carList) { filteredCars ->
               carListAdapter.setCars(filteredCars)
               carListAdapter.notifyDataSetChanged()
           }
       }

       private fun showSortDialog() {
           val options = arrayOf("Rating Ascending", "Rating Descending", "Older", "Newer")

           val builder = AlertDialog.Builder(this)
           builder.setTitle("Sort Options")
           builder.setItems(options) { dialog, which ->
               when (which) {
                   0 -> {
                       // Rating Ascending
                       CarUtils.sortByRatingAscending(carList) { updatedCarList ->
                           Log.d("CARS:", "$updatedCarList")
                           carList.clear()
                           carList.addAll(updatedCarList)
                           carListAdapter.notifyDataSetChanged()
                       }
                   }
                   1 -> {
                       // Rating Descending
                       CarUtils.sortByRatingDescending(carList){ updatedCarList ->
                           carList.clear()
                           carList.addAll(updatedCarList)
                           carListAdapter.notifyDataSetChanged()
                       }
                   }
                   2 -> {
                       // Older
                       //CarUtils.sortOlder(carList)
                       carListAdapter.notifyDataSetChanged()
                   }
                   3 -> {
                       // Newer
                       //CarUtils.sortNewer(carList)
                       carListAdapter.notifyDataSetChanged()
                   }
               }
               dialog.dismiss()
           }
           builder.create().show()
       }

       override fun onResume() {
            super.onResume()
            retrieveAllCars()
       }

       private fun initView() {
            carListRecyclerView = findViewById(R.id.carListRecyclerView)
            carListRecyclerView.layoutManager = GridLayoutManager(this, 2)
            carListAdapter = CarListAdapter(emptyList())
            carListRecyclerView.adapter = carListAdapter
       }

        private fun retrieveAllCars() {
            CarUtils.retrieveAllCars() { cars ->
                carList = ArrayList(cars)
                Log.d("Car List:","$carList")

                carListAdapter.setOnCarClickListener(this@MainActivity)
                carListAdapter.setCars(carList)
            }
        }

        private fun filterByKeyword(keyword: EditText) {
            //Delete
        }

       override fun onCarClick(car: Car) {
           showCarDialog(car)
       }

       //Add rating
       private fun showCarDialog(car: Car) {
            val dialog = Dialog(this)
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

            rentButton.setOnClickListener{
                val intentRentCar = Intent(this, CarRentActivity::class.java)
                intentRentCar.putExtra("car", car)
                startActivity(intentRentCar)
                finish()
            }

            dialog.show()
        }

    }
