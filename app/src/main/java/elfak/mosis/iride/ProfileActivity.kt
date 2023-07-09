package elfak.mosis.iride

import CarListAdapter
import android.graphics.Color
import android.graphics.Typeface
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private lateinit var profilePicture: ImageView
    private lateinit var fullName: TextView
    private lateinit var carsRecyclerView: RecyclerView
    private lateinit var carListAdapter: CarListAdapter
    private lateinit var leaderboardBtn: ImageButton

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profilePicture = findViewById(R.id.profilePic)
        fullName = findViewById(R.id.textViewImePrezime)
        leaderboardBtn = findViewById(R.id.btnLeaderboard)

        carsRecyclerView = findViewById(R.id.carListRecyclerView1)
        carsRecyclerView.layoutManager = LinearLayoutManager(this)

        carListAdapter = CarListAdapter(emptyList())
        carsRecyclerView.adapter = carListAdapter
        carsRecyclerView.layoutManager = GridLayoutManager(this, 2)

        initView()
        retrieveAllUserCars()

        leaderboardBtn.setOnClickListener {
            showLeaderboardDialog(currentUser)
        }
    }

    private fun initView() {
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser
        database = FirebaseDatabase.getInstance().reference.child("users").child(currentUser?.uid ?: "")

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val userData = dataSnapshot.value as? Map<String, Any>
                    userData?.let {
                        val profilePicUrl = userData["profilePicUrl"] as? String
                        val firstName = userData["firstName"] as? String
                        val lastName = userData["lastName"] as? String

                        profilePicUrl?.let {
                            Glide.with(this@ProfileActivity)
                                .load(it)
                                .into(profilePicture)
                        }

                        val fullNameText = "$firstName $lastName"
                        fullName.text = fullNameText
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Database Error: ${databaseError.message}")
            }
        })
    }

    private fun retrieveAllUserCars() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            val usersRef = FirebaseDatabase.getInstance().getReference("users")
            val carsRef = usersRef.child(userId).child("cars")

            carsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(carsSnapshot: DataSnapshot) {
                    val carList = mutableListOf<Car>()

                    for (carSnapshot in carsSnapshot.children) {
                        val carMap = carSnapshot.getValue(object : GenericTypeIndicator<HashMap<String, Any>>() {})
                        if (carMap != null) {

                            val carObject = Car(
                                userId,
                                carMap["brand"] as? String ?: "",
                                carMap["model"] as? String ?: "",
                                carMap["fuel"] as? String ?: "",
                                carMap["category"] as? String ?: "",
                                carMap["year"] as? Int ?: 0,
                                carMap["transmission"] as? String ?: "",
                                carMap["latitude"] as? Double ?: 0.0,
                                carMap["longitude"] as? Double ?: 0.0,
                                carMap["carImage"] as? String ?: "",
                                carMap["rented"] as? Boolean ?: false,
                                carMap["openKey"] as? String ?: ""
                            )

                            carList.add(carObject)
                        }
                    }
                    Log.d(TAG, "Car List: $carList") // Log the carList contents here
                    carListAdapter.setCars(carList)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(TAG, "Database Error: ${databaseError.message}")
                }
            })
        }
    }

    companion object {
        private const val TAG = "ProfileActivity"
    }

    private fun showLeaderboardDialog(currentUser: FirebaseUser?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.leaderboard_dialog, null)
        val tableLayout = dialogView.findViewById<TableLayout>(R.id.leaderboardTable)

        // Retrieve user data from Firebase and rank them based on score
        val leaderboardData = ArrayList<Pair<String, Int>>()

        // Assuming you have a Firebase reference to the users collection
        val usersRef = FirebaseDatabase.getInstance().reference.child("users")

        usersRef.orderByChild("score").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Clear previous data
                leaderboardData.clear()

                // Iterate through the dataSnapshot to retrieve user data and scores
                for (userSnapshot in dataSnapshot.children) {
                    val firstName = userSnapshot.child("firstName").value as String
                    val lastName = userSnapshot.child("lastName").value as String
                    val score = userSnapshot.child("score").value as Long

                    val fullName = "$firstName $lastName"
                    leaderboardData.add(Pair(fullName, score.toInt()))
                }

                // Sort the leaderboard data based on score in descending order
                leaderboardData.sortByDescending { it.second }

                // Display the leaderboard data in the table
                for (i in leaderboardData.indices) {
                    val entry = leaderboardData[i]
                    val row = TableRow(this@ProfileActivity)

                    val rankTextView = TextView(this@ProfileActivity)
                    val scoreTextView = TextView(this@ProfileActivity)
                    val nameTextView = TextView(this@ProfileActivity)

                    rankTextView.text = (i + 1).toString()
                    scoreTextView.text = entry.second.toString()
                    nameTextView.text = entry.first

                    // Set layout weight to distribute the columns evenly
                    rankTextView.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                    scoreTextView.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                    nameTextView.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)

                    rankTextView.gravity = Gravity.CENTER
                    scoreTextView.gravity = Gravity.CENTER
                    nameTextView.gravity = Gravity.CENTER

                    // Highlight the current user's row
                    if (currentUser != null && entry.first == currentUser.uid) {
                        rankTextView.setTextColor(Color.RED)
                        scoreTextView.setTextColor(Color.RED)
                        nameTextView.setTextColor(Color.RED)

                        // Set the typeface to bold
                        rankTextView.setTypeface(null, Typeface.BOLD)
                        scoreTextView.setTypeface(null, Typeface.BOLD)
                        nameTextView.setTypeface(null, Typeface.BOLD)
                    }
                    row.addView(rankTextView)
                    row.addView(scoreTextView)
                    row.addView(nameTextView)

                    tableLayout.addView(row)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error if fetching data from Firebase fails
            }
        })

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Leaderboard")
            .setPositiveButton("Close", null)

        val dialog = dialogBuilder.create()

        dialog.setOnShowListener {
            val closeButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val closeButtonLayoutParams = closeButton.layoutParams as LinearLayout.LayoutParams
            closeButtonLayoutParams.gravity = Gravity.CENTER
            closeButton.layoutParams = closeButtonLayoutParams
        }

        dialog.show()
    }


}