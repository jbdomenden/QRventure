package app.QRventure.dto

import app.QRventure.models.Attraction
import app.QRventure.models.DiningPlace
import app.QRventure.models.LocalService
import app.QRventure.models.TourRoute
import kotlinx.serialization.Serializable

@Serializable
data class FeaturedResponse(
    val attractions: List<Attraction>,
    val dining: List<DiningPlace>,
    val services: List<LocalService>,
    val routes: List<TourRoute>
)

@Serializable
data class SearchResponse(
    val query: String,
    val attractions: List<Attraction>,
    val dining: List<DiningPlace>,
    val services: List<LocalService>,
    val routes: List<TourRoute>
)

@Serializable
data class ErrorResponse(val message: String)
