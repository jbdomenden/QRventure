package app.QRventure.services

import app.QRventure.dto.FeaturedResponse
import app.QRventure.dto.SearchResponse
import app.QRventure.models.Attraction
import app.QRventure.models.DiningPlace
import app.QRventure.models.LocalService
import app.QRventure.models.TourRoute
import app.QRventure.repositories.TourismRepository

class TourismService(private val repository: TourismRepository) {

    suspend fun featured(): FeaturedResponse = FeaturedResponse(
        attractions = attractions().filter { it.isFeatured }.take(6),
        dining = dining().filter { it.isFeatured }.take(6),
        services = services().take(6),
        routes = tourRoutes().filter { it.isFeatured }.take(6)
    )

    suspend fun search(query: String): SearchResponse {
        val q = query.trim().lowercase()
        return SearchResponse(
            query = query.trim(),
            attractions = attractions(search = q),
            dining = dining(search = q),
            services = services(search = q),
            routes = tourRoutes(search = q)
        )
    }

    suspend fun attractions(search: String? = null, category: String? = null): List<Attraction> {
        val q = search?.trim()?.lowercase().orEmpty()
        val normalizedCategory = category?.trim()?.lowercase().orEmpty()
        return repository.attractions().filter { item ->
            val matchesSearch = q.isBlank() || containsAny(q, listOf(item.name, item.shortDescription, item.fullDescription, item.category, item.historicalPeriod) + item.alternateNames)
            val matchesCategory = normalizedCategory.isBlank() || item.category.lowercase() == normalizedCategory
            matchesSearch && matchesCategory
        }
    }

    suspend fun attractionBySlugOrId(key: String): Attraction? = repository.attractions().firstOrNull { matchesKey(key, it.slug, it.id) }

    suspend fun dining(type: String? = null, search: String? = null): List<DiningPlace> {
        val normalizedType = type?.trim()?.lowercase().orEmpty()
        val q = search?.trim()?.lowercase().orEmpty()
        return repository.dining().filter { item ->
            val matchesType = normalizedType.isBlank() || item.diningType.lowercase().contains(normalizedType)
            val matchesSearch = q.isBlank() || containsAny(q, listOf(item.name, item.shortDescription, item.fullDescription, item.cuisine, item.diningType))
            matchesType && matchesSearch
        }
    }

    suspend fun diningBySlugOrId(key: String): DiningPlace? = repository.dining().firstOrNull { matchesKey(key, it.slug, it.id) }

    suspend fun services(type: String? = null, search: String? = null): List<LocalService> {
        val normalizedType = type?.trim()?.lowercase().orEmpty()
        val q = search?.trim()?.lowercase().orEmpty()
        return repository.services().filter { item ->
            val matchesType = normalizedType.isBlank() || item.serviceType.lowercase().contains(normalizedType)
            val matchesSearch = q.isBlank() || containsAny(q, listOf(item.name, item.shortDescription, item.fullDescription, item.serviceType, item.visitorNotes))
            matchesType && matchesSearch
        }
    }

    suspend fun serviceBySlugOrId(key: String): LocalService? = repository.services().firstOrNull { matchesKey(key, it.slug, it.id) }

    suspend fun tourRoutes(search: String? = null): List<TourRoute> {
        val q = search?.trim()?.lowercase().orEmpty()
        return repository.tourRoutes().filter { item ->
            q.isBlank() || containsAny(q, listOf(item.name, item.shortDescription, item.fullDescription, item.routeType, item.startingPoint))
        }
    }

    suspend fun tourRouteBySlugOrId(key: String): TourRoute? = repository.tourRoutes().firstOrNull { matchesKey(key, it.slug, it.id) }
}

private fun matchesKey(key: String, slug: String, id: Int): Boolean {
    return slug == key || id.toString() == key
}

private fun containsAny(query: String, values: List<String>): Boolean {
    return values.any { it.lowercase().contains(query) }
}
