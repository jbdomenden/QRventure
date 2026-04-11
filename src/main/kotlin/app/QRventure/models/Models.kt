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
    val locationText: String,
    val openingHours: String,
    val entranceFee: String,
    val contactDetails: String,
    val latitude: Double,
    val longitude: Double,
    val imagePath: String,
    val status: String,
    val isFeatured: Boolean
)

@Serializable
data class DiningPlace(
    val id: Int,
    val slug: String,
    val name: String,
    val description: String,
    val cuisineOrType: String,
    val locationText: String,
    val openingHours: String,
    val priceRange: String,
    val contactDetails: String,
    val latitude: Double,
    val longitude: Double,
    val imagePath: String,
    val isFeatured: Boolean
)

@Serializable
data class LocalService(
    val id: Int,
    val slug: String,
    val name: String,
    val description: String,
    val serviceType: String,
    val locationText: String,
    val operatingHours: String,
    val contactDetails: String,
    val latitude: Double,
    val longitude: Double,
    val nearbyLandmarkNotes: String,
    val travelTips: String,
    val imagePath: String,
    val isFeatured: Boolean
)

@Serializable
data class TourRoute(
    val id: Int,
    val slug: String,
    val name: String,
    val durationText: String,
    val startPoint: String,
    val routeDescription: String,
    val distanceKm: Double,
    val highlights: String,
    val isFeatured: Boolean
)
