package app.QRventure.dto

import app.QRventure.model.Attraction
import app.QRventure.model.DiningPlace
import app.QRventure.model.LocalService
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
