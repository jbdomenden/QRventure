package app.QRventure.dto

import kotlinx.serialization.Serializable

@Serializable
data class AttractionUpsertDto(
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
    val isFeatured: Boolean,
    val status: String,
    val sortOrder: Int
)

@Serializable
data class DiningUpsertDto(
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
    val isFeatured: Boolean,
    val status: String,
    val sortOrder: Int
)

@Serializable
data class ServiceUpsertDto(
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
    val status: String,
    val sortOrder: Int
)

@Serializable
data class TourRouteUpsertDto(
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
