package app.QRventure

import app.QRventure.models.Attraction
import app.QRventure.models.DiningPlace
import app.QRventure.models.LocalService
import app.QRventure.models.TourRoute

object TestTourismData {
    val attractions = listOf(
        Attraction(
            id = 1,
            slug = "fort-santiago",
            name = "Fort Santiago",
            alternateNames = listOf("Fuerza de Santiago"),
            shortDescription = "Historic citadel and Rizal memorial destination.",
            fullDescription = "Fort Santiago is a major Intramuros landmark used in tests.",
            category = "Fortification",
            historicalPeriod = "Spanish Colonial",
            locationText = "Santa Clara Street, Intramuros",
            openingHours = "8:00 AM - 11:00 PM",
            entranceFee = "PHP 75 regular / PHP 50 student",
            contactDetails = "(02) 8527 2961",
            visitorTips = "Bring water.",
            bestTimeToVisit = "Early morning",
            latitude = 14.5953,
            longitude = 120.9701,
            imageUrl = "https://example.com/fort-santiago.jpg",
            imagePath = "https://example.com/fort-santiago.jpg",
            imageUrls = listOf("https://example.com/fort-santiago.jpg"),
            isFeatured = true,
            status = "open",
            sortOrder = 1
        )
    )

    val dining = listOf(
        DiningPlace(
            id = 1,
            slug = "barbaras",
            name = "Barbara's Heritage Restaurant",
            shortDescription = "Heritage dining with cultural performances.",
            fullDescription = "Featured dining content for tests.",
            diningType = "Restaurant",
            cuisine = "Filipino",
            locationText = "General Luna Street, Intramuros",
            openingHours = "10:00 AM - 9:00 PM",
            priceRange = "PHP 500-1,200",
            contactDetails = "(02) 8527 3893",
            visitorNotes = "Reserve ahead.",
            latitude = 14.5894,
            longitude = 120.9750,
            imageUrl = "https://example.com/barbaras.jpg",
            imagePath = "https://example.com/barbaras.jpg",
            imageUrls = listOf("https://example.com/barbaras.jpg"),
            isFeatured = true,
            status = "open",
            sortOrder = 1
        )
    )

    val services = listOf(
        LocalService(
            id = 1,
            slug = "visitor-center",
            name = "Intramuros Visitor Information Center",
            shortDescription = "Visitor orientation and planning help.",
            fullDescription = "Service content for tests.",
            serviceType = "Information",
            locationText = "General Luna Street, Intramuros",
            hours = "8:00 AM - 5:00 PM",
            contactDetails = "Intramuros Administration",
            visitorNotes = "Drop by before starting your route.",
            latitude = 14.5896,
            longitude = 120.9744,
            imageUrl = "https://example.com/visitor-center.jpg",
            imagePath = "https://example.com/visitor-center.jpg",
            imageUrls = listOf("https://example.com/visitor-center.jpg"),
            status = "open",
            sortOrder = 1
        )
    )

    val routes = listOf(
        TourRoute(
            id = 1,
            slug = "historic-core-loop",
            name = "Historic Core Loop",
            shortDescription = "A complete first-time Intramuros walking circuit.",
            fullDescription = "Route content for tests.",
            routeType = "Heritage Walk",
            startingPoint = "Plaza Roma",
            estimatedDuration = "2.5 hours",
            travelTips = "Start early.",
            distanceText = "2.6 km",
            mapLink = "https://maps.google.com/?q=14.5906,120.9734",
            imageUrl = "https://example.com/historic-core-loop.jpg",
            imageUrls = listOf("https://example.com/historic-core-loop.jpg"),
            isFeatured = true,
            status = "open",
            sortOrder = 1
        )
    )
}
