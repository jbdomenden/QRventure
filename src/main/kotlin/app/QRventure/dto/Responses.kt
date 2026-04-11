package app.QRventure.dto

import app.QRventure.models.Attraction
import app.QRventure.models.DiningPlace
import app.QRventure.models.LocalService
import kotlinx.serialization.Serializable

@Serializable
data class FeaturedResponse(
    val attractions: List<Attraction>,
    val dining: List<DiningPlace>,
    val services: List<LocalService>
)

@Serializable
data class SearchResponse(
    val query: String,
    val attractions: List<Attraction>,
    val dining: List<DiningPlace>,
    val services: List<LocalService>
)

@Serializable
data class ErrorResponse(val message: String)
