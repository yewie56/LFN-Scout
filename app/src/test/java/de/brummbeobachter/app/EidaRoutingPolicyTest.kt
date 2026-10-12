package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Test

class EidaRoutingPolicyTest {
    @Test
    fun get_response_keeps_only_unique_http_routes() {
        val text = """
            # comment
            https://geofon.gfz.de/fdsnws/dataselect/1/query?net=GE&sta=STU
            not-a-url
            https://geofon.gfz.de/fdsnws/dataselect/1/query?net=GE&sta=STU
            http://example.org/fdsnws/dataselect/1/query?net=XX
        """.trimIndent()

        assertEquals(
            listOf(
                "https://geofon.gfz.de/fdsnws/dataselect/1/query?net=GE&sta=STU",
                "http://example.org/fdsnws/dataselect/1/query?net=XX"
            ),
            EidaRoutingPolicy.parseGetResponse(text)
        )
    }

    @Test
    fun cleartext_route_is_preferentially_upgraded_to_https() {
        assertEquals(
            "https://example.org/fdsnws/dataselect/1/query",
            EidaRoutingPolicy.httpsPreferred("http://example.org/fdsnws/dataselect/1/query")
        )
    }

    @Test
    fun no_route_means_no_probe_endpoint() {
        assertEquals(
            emptyList<String>(),
            EidaRoutingPolicy.probeEndpoints(
                routes = emptyList(),
                fallbackEndpoint = "https://federator.orfeus-eu.org/fdsnws/dataselect/1/query",
                maxRoutes = 2
            )
        )
    }

    @Test
    fun routed_stream_keeps_route_and_federator_fallback() {
        assertEquals(
            listOf(
                "https://geofon.gfz.de/fdsnws/dataselect/1/query",
                "https://federator.orfeus-eu.org/fdsnws/dataselect/1/query"
            ),
            EidaRoutingPolicy.probeEndpoints(
                routes = listOf("https://geofon.gfz.de/fdsnws/dataselect/1/query?net=GE&sta=STU"),
                fallbackEndpoint = "https://federator.orfeus-eu.org/fdsnws/dataselect/1/query",
                maxRoutes = 2
            )
        )
    }

    @Test
    fun route_url_can_be_reduced_to_endpoint() {
        assertEquals(
            "https://geofon.gfz.de/fdsnws/dataselect/1/query",
            EidaRoutingPolicy.endpointOnly(
                "https://geofon.gfz.de/fdsnws/dataselect/1/query?net=GE&sta=STU"
            )
        )
    }
}
