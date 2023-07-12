   package elfak.mosis.iride

    import CarListAdapter
    import android.annotation.SuppressLint
    import android.content.Intent
    import androidx.appcompat.app.AppCompatActivity
    import android.os.Bundle
    import android.util.Log
    import android.widget.ImageButton
    import androidx.appcompat.app.AlertDialog
    import androidx.recyclerview.widget.GridLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import com.google.firebase.database.DatabaseReference
    import com.google.firebase.database.FirebaseDatabase
    import com.google.firebase.storage.FirebaseStorage
    import com.google.firebase.storage.StorageReference
    import java.text.SimpleDateFormat
    import java.util.Date
    import java.util.Locale

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

       @SuppressLint("NotifyDataSetChanged")
       private fun showFilterDialog() {
           CarUtils.showFilterDialog(this, carList) { filteredCars ->
               carListAdapter.setCars(filteredCars)
               carListAdapter.notifyDataSetChanged()
           }
       }

       @SuppressLint("NotifyDataSetChanged")
       private fun showSortDialog() {
           val options = arrayOf("Rating Ascending", "Rating Descending", "Date added: Older", "Date added: Newer")

           val builder = AlertDialog.Builder(this)
           builder.setTitle("Sort Options")
           builder.setItems(options) { dialog, which ->
               when (which) {
                   0 -> {
                       carList.sortBy { it.rating.toFloat() }
                       carListAdapter.notifyDataSetChanged()
                   }
                   1 -> {
                       carList.sortByDescending { it.rating.toFloat() }
                       carListAdapter.notifyDataSetChanged()

                   }
                   2 -> {
                       Log.d("pre", "$carList")
                       carList.sortBy { formatDate(it.dateAdded.toLong()) }
                       Log.d("posle", "$carList")
                       carListAdapter.notifyDataSetChanged()
                   }
                   3 -> {
                       carList.sortByDescending { formatDate(it.dateAdded.toLong()) }
                       carListAdapter.notifyDataSetChanged()
                   }
               }
               dialog.dismiss()
           }
           builder.create().show()
       }

       private fun formatDate(dateInMillis: Long): String {
           val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
           val date = Date(dateInMillis)
           return dateFormat.format(date)
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
            CarUtils.retrieveAllCars { cars ->
                carList = ArrayList(cars)
                Log.d("Car List:","$carList")

                carListAdapter.setOnCarClickListener(this@MainActivity)
                carListAdapter.setCars(carList)
            }
        }

       override fun onCarClick(car: Car) {
           showCarDialog(car)
       }

       private fun showCarDialog(car: Car) {
           CarUtils.showCarDialog(this, car)
       }
    }
