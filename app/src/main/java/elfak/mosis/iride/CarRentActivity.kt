package elfak.mosis.iride

import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide

class CarRentActivity : AppCompatActivity() {

    private lateinit var brandModelYear: TextView
    private lateinit var tvRating: TextView
    private lateinit var carPic: ImageView
    private lateinit var openKey: TextView
    private lateinit var duration: TextView
    private lateinit var distance: TextView
    private lateinit var stopBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_rent)

        brandModelYear = findViewById(R.id.tvBMY)
        tvRating = findViewById(R.id.tvRating)
        carPic = findViewById(R.id.carPicRent)
        openKey = findViewById(R.id.tvOpenKey)
        duration = findViewById(R.id.tvDuration)
        distance = findViewById(R.id.tvDistance)
        stopBtn = findViewById(R.id.StopRentBtn)

        val car: Car? = intent.getSerializableExtra("car") as? Car

        if (car != null) {

            val latitude = car.latitude.toDouble()
            val longitude = car.longitude.toDouble()
            val location = Location("carLocation")
            location.latitude = latitude
            location.longitude = longitude

            openKey.text = "Open key: " + car.openKey
            brandModelYear.text = car.brand + "" + car.model + " (" + car.year + ")"
            tvRating.text = String.format("(%d) %.1f/5", car.numOfRatings, car.rating)

            Glide.with(this)
                .load(car.carImage)
                .into(carPic)

            distance.text = "1000"
            duration.text = "0000"
        } else {
            Log.e("Error: ", "Car is null")
        }

        stopBtn.setOnClickListener{
            if (car != null) {
                showRateDialog(car)
            }
        }

    }

    private fun showRateDialog(car: Car) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.rate_dialog, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        val btnRate = dialogView.findViewById<Button>(R.id.btnRate)

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Please rate this car")

        val alertDialog = dialogBuilder.create()

        btnRate.setOnClickListener {
            val rating = ratingBar.rating

            Log.d("Rating test", "$rating")

            CarUtils.rateCar(car, rating)

            alertDialog.dismiss()
            val intentMain = Intent(this, MainActivity::class.java)
            startActivity(intentMain)
            finish()
        }

        alertDialog.show()
    }

}