package app.QRventure.repositories

import app.QRventure.models.Attraction
import app.QRventure.models.DiningPlace
import app.QRventure.models.LocalService
import app.QRventure.models.TourRoute

interface TourismRepository {
    suspend fun attractions(): List<Attraction>
    suspend fun dining(): List<DiningPlace>
    suspend fun services(): List<LocalService>
    suspend fun tourRoutes(): List<TourRoute>
}
