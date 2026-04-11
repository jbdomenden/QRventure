package app.QRventure.models

import kotlinx.serialization.Serializable

@Serializable
data class Admin(
    val id: Int,
    val email: String,
    val passwordHash: String,
    val role: String,
    val createdAt: String
)

@Serializable
data class Attraction(
    val id: Int,
    val slug: String,
    val name: String,
    val shortDescription: String,
    val fullDescription: String,
    val category: String,
    val historicalPeriod: String,
    val locationText: String,
    val openingHours: String,
    val entranceFee: String,
    val contactDetails: String,
    val visitorTips: String,
    val bestTimeToVisit: String,
    val latitude: Double,
    val longitude: Double,
    val imagePath: String,
    val imageUrls: List<String> = emptyList(),
    val isFeatured: Boolean,
    val status: String,
    val sortOrder: Int
)

@Serializable
data class DiningPlace(
    val id: Int,
    val slug: String,
    val name: String,
    val shortDescription: String,
    val fullDescription: String,
    val diningType: String,
    val cuisine: String,
    val locationText: String,
    val openingHours: String,
    val priceRange: String,
    val contactDetails: String,
    val visitorNotes: String,
    val latitude: Double,
    val longitude: Double,
    val imagePath: String,
    val imageUrls: List<String> = emptyList(),
    val isFeatured: Boolean,
    val status: String,
    val sortOrder: Int
)

@Serializable
data class LocalService(
    val id: Int,
    val slug: String,
    val name: String,
    val shortDescription: String,
    val fullDescription: String,
    val serviceType: String,
    val locationText: String,
    val hours: String,
    val contactDetails: String,
    val visitorNotes: String,
    val latitude: Double,
    val longitude: Double,
    val imagePath: String,
    val imageUrls: List<String> = emptyList(),
    val status: String,
    val sortOrder: Int
)

@Serializable
data class TourRoute(
    val id: Int,
    val slug: String,
    val name: String,
    val shortDescription: String,
    val fullDescription: String,
    val routeType: String,
    val startingPoint: String,
    val estimatedDuration: String,
    val travelTips: String,
    val distanceText: String,
    val mapLink: String,
    val isFeatured: Boolean,
    val status: String,
    val sortOrder: Int
)
