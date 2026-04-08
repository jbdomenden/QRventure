package app.QRventure.dto

private val slugRegex = Regex("^[a-z0-9]+(?:-[a-z0-9]+)*$")
private val allowedAttractionStatus = setOf("open", "closed", "maintenance")

fun validateAttraction(dto: AttractionUpsertDto): String? {
    validateSlug(dto.slug)?.let { return it }
    validateRequired("name", dto.name)?.let { return it }
    validateRequired("shortDescription", dto.shortDescription, 20)?.let { return it }
    validateRequired("fullDescription", dto.fullDescription, 40)?.let { return it }
    validateRequired("category", dto.category)?.let { return it }
    validateRequired("locationText", dto.locationText)?.let { return it }
    validateRequired("openingHours", dto.openingHours)?.let { return it }
    validateRequired("entranceFee", dto.entranceFee)?.let { return it }
    validateRequired("contactDetails", dto.contactDetails)?.let { return it }
    validateCoordinates(dto.latitude, dto.longitude)?.let { return it }
    validateImagePath(dto.imagePath)?.let { return it }
    if (dto.status.lowercase() !in allowedAttractionStatus) return "status must be one of: open, closed, maintenance."
    return null
}

fun validateDining(dto: DiningUpsertDto): String? {
    validateSlug(dto.slug)?.let { return it }
    validateRequired("name", dto.name)?.let { return it }
    validateRequired("description", dto.description, 20)?.let { return it }
    validateRequired("cuisineOrType", dto.cuisineOrType)?.let { return it }
    validateRequired("locationText", dto.locationText)?.let { return it }
    validateRequired("openingHours", dto.openingHours)?.let { return it }
    validateRequired("priceRange", dto.priceRange)?.let { return it }
    validateRequired("contactDetails", dto.contactDetails)?.let { return it }
    validateCoordinates(dto.latitude, dto.longitude)?.let { return it }
    validateImagePath(dto.imagePath)?.let { return it }
    return null
}

fun validateService(dto: ServiceUpsertDto): String? {
    validateSlug(dto.slug)?.let { return it }
    validateRequired("name", dto.name)?.let { return it }
    validateRequired("description", dto.description, 20)?.let { return it }
    validateRequired("serviceType", dto.serviceType)?.let { return it }
    validateRequired("locationText", dto.locationText)?.let { return it }
    validateRequired("operatingHours", dto.operatingHours)?.let { return it }
    validateRequired("contactDetails", dto.contactDetails)?.let { return it }
    validateCoordinates(dto.latitude, dto.longitude)?.let { return it }
    validateRequired("nearbyLandmarkNotes", dto.nearbyLandmarkNotes)?.let { return it }
    validateRequired("travelTips", dto.travelTips, 10)?.let { return it }
    validateImagePath(dto.imagePath)?.let { return it }
    return null
}

fun validateTourRoute(dto: TourRouteUpsertDto): String? {
    validateSlug(dto.slug)?.let { return it }
    validateRequired("name", dto.name)?.let { return it }
    validateRequired("durationText", dto.durationText)?.let { return it }
    validateRequired("startPoint", dto.startPoint)?.let { return it }
    validateRequired("routeDescription", dto.routeDescription, 20)?.let { return it }
    if (dto.distanceKm <= 0.0 || dto.distanceKm > 100.0) return "distanceKm must be greater than 0 and less than or equal to 100."
    validateRequired("highlights", dto.highlights, 10)?.let { return it }
    return null
}

private fun validateSlug(slug: String): String? {
    if (!slugRegex.matches(slug)) {
        return "slug must be lowercase alphanumeric words separated by hyphens."
    }
    return null
}

private fun validateRequired(field: String, value: String, minLen: Int = 1): String? {
    if (value.trim().length < minLen) return "$field is required${if (minLen > 1) " (min $minLen chars)" else ""}."
    return null
}

private fun validateCoordinates(latitude: Double, longitude: Double): String? {
    if (latitude !in -90.0..90.0) return "latitude must be between -90 and 90."
    if (longitude !in -180.0..180.0) return "longitude must be between -180 and 180."
    return null
}

private fun validateImagePath(path: String): String? {
    if (!path.startsWith("/qrventure/uploads/") && !path.startsWith("/qrventure/images/")) {
        return "imagePath must point to /qrventure/uploads/ or /qrventure/images/."
    }
    return null
}
