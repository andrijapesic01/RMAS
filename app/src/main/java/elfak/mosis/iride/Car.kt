package elfak.mosis.iride

import android.location.Location
import java.io.Serializable
import java.security.Timestamp
import java.util.Date


data class Car (
    val userId: String = "",
    val brand: String = "",
    val model: String = "",
    val fuel: String = "",
    val category: String = "",
    val year: Int = 0,
    val transmission: String = "",
    //val location: Location = Location(""),
    val latitude: Number = 0,
    val longitude: Number = 0,
    var carImage: String = "",
    val rented: Boolean = false,
    val openKey: String = "",
    val rating: Float = 0f,
    val numOfRatings: Int = 0,
    //val date: Date
) : Serializable
