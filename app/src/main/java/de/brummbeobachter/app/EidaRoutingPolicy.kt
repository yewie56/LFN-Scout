package de.brummbeobachter.app

object EidaRoutingPolicy {
    fun parseGetResponse(text: String): List<String> =
        text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .filter { it.startsWith("https://", ignoreCase = true) || it.startsWith("http://", ignoreCase = true) }
            .distinct()
            .toList()

    fun endpointOnly(url: String): String =
        url.substringBefore('?').trimEnd('/')

    fun probeEndpoints(
        routes: List<String>,
        fallbackEndpoint: String,
        maxRoutes: Int
    ): List<String> {
        if (routes.isEmpty()) return emptyList()
        return (routes.take(maxRoutes) + fallbackEndpoint)
            .map { endpointOnly(it) }
            .distinct()
    }

    fun httpsPreferred(url: String): String =
        if (url.startsWith("http://", ignoreCase = true)) {
            "https://${url.substring(7)}"
        } else {
            url
        }
}
