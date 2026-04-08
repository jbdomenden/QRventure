package app.QRventure.dto

import kotlinx.serialization.Serializable

@Serializable
data class AttractionUpsertDto(
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
data class DiningUpsertDto(
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
data class ServiceUpsertDto(
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
data class TourRouteUpsertDto(
    val slug: String,
    val name: String,
    val durationText: String,
    val startPoint: String,
    val routeDescription: String,
    val distanceKm: Double,
    val highlights: String,
    val isFeatured: Boolean
)
