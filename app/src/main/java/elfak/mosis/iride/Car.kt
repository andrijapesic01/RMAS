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
    val year: String = "",
    val transmission: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var carImage: String = "",
    val rented: Boolean = false,
    val openKey: String = "",
    val rating: String = "",
    val numOfRatings: String = "",
    var dateAdded: String = "0"
) : Serializable
