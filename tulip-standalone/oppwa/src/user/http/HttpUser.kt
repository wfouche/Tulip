package user.http

/*-------------------------------------------------------------------------*/

import org.tulip.api.TulipUser
import org.tulip.core.Console
import org.tulip.core.delayMillisRandom
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.tulip.pfsm.Edge
import org.tulip.pfsm.MarkovChain

/*-------------------------------------------------------------------------*/

private val client = HttpClient.newHttpClient()

/*-------------------------------------------------------------------------*/

// https://transform.tools/json-to-kotlin

@Serializable
data class Response11(
    val id: String,
    val paymentType: String,
    val paymentBrand: String,
    val amount: String,
    val currency: String,
    val descriptor: String,
    val result: Result,
    val resultDetails: ResultDetails,
    val card: Card,
    val risk: Risk,
    val buildNumber: String,
    val timestamp: String,
    val ndc: String,
    val source: String,
    val paymentMethod: String,
    val shortId: String,
)

@Serializable
data class Result(
    val code: String,
    val description: String,
)

@Serializable
data class ResultDetails(
    val clearingInstituteName: String,
)

@Serializable
data class Card(
    val bin: String,
    val last4Digits: String,
    val holder: String,
    val expiryMonth: String,
    val expiryYear: String,
)

@Serializable
data class Risk(
    val score: String,
)

/*-------------------------------------------------------------------------*/
@Serializable
data class Response12(
    val id: String,
    val referencedId: String,
    val paymentType: String,
    val amount: String,
    val currency: String,
    val descriptor: String,
    val result: Result12,
    val resultDetails: ResultDetails12,
    val buildNumber: String,
    val timestamp: String,
    val ndc: String,
    val source: String,
    val paymentMethod: String,
    val shortId: String,
)
@Serializable
data class Result12(
    val code: String,
    val description: String,
)
@Serializable
data class ResultDetails12(
    val clearingInstituteName: String,
)

/*-------------------------------------------------------------------------*/

class HttpUser(userId: Int, threadId: Int) : TulipUser(userId, threadId) {

    // ----------------------------------------------------------------- //

    private var cid: Int = 0

    // ----------------------------------------------------------------- //

    private lateinit var rsp11: Response11

    // ----------------------------------------------------------------- //

    override fun start(): Boolean {
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action1(): Boolean {
        val map = mapOf(
            "entityId"          to "8a8294174b7ecb28014b9699220015ca",
            "amount"            to "92.00",
            "currency"          to "EUR",
            "paymentBrand"      to "VISA",
            "paymentType"       to "PA",
            "card.number"       to "4200000000000000",
            "card.holder"       to "Jane Jones",
            "card.expiryMonth"  to "05",
            "card.expiryYear"   to "2034",
            "card.cvv"          to "123")
        val body: String = map.entries.joinToString("&")

        val token = "OGE4Mjk0MTc0YjdlY2IyODAxNGI5Njk5MjIwMDE1Y2N8c3k2S0pzVDg="

        val request:HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://eu-test.oppwa.com/v1/payments"))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        rsp11 = Json.decodeFromString<Response11>(response.body())
        if (response.statusCode() == 200) {
            if (rsp11.result.code.split(".")[0] == "000") {
                return true
            }
        }
        return false
    }

    // ----------------------------------------------------------------- //

    override fun action2(): Boolean {
        val map = mapOf(
            "entityId"          to "8a8294174b7ecb28014b9699220015ca",
            "amount"            to "92.00",
            "currency"          to "EUR",
            "paymentType"       to "CP")
        val body: String = map.entries.joinToString("&")

        val token = "OGE4Mjk0MTc0YjdlY2IyODAxNGI5Njk5MjIwMDE1Y2N8c3k2S0pzVDg="

        val request:HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://eu-test.oppwa.com/v1/payments/${rsp11.id}"))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val rsp12 = Json.decodeFromString<Response12>(response.body())
        if (response.statusCode() == 200) {
            if (rsp12.result.code.split(".")[0] == "000") {
                return true
            }
        }
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action3(): Boolean {
        val map = mapOf(
            "entityId"          to "8a8294174b7ecb28014b9699220015ca",
            "amount"            to "92.00",
            "currency"          to "EUR",
            "paymentType"       to "RF")
        val body: String = map.entries.joinToString("&")

        val token = "OGE4Mjk0MTc0YjdlY2IyODAxNGI5Njk5MjIwMDE1Y2N8c3k2S0pzVDg="

        val request:HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://eu-test.oppwa.com/v1/payments/${rsp11.id}"))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val rsp13 = Json.decodeFromString<Response12>(response.body())
        if (response.statusCode() == 200) {
            if (rsp13.result.code.split(".")[0] == "000") {
                return true
            }
        }
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action4(): Boolean {
        val map = mapOf(
            "entityId"          to "8a8294174b7ecb28014b9699220015ca",
            "amount"            to "92.00",
            "currency"          to "EUR",
            "paymentBrand"      to "VISA",
            "paymentType"       to "DB",
            "card.number"       to "4200000000000000",
            "card.holder"       to "Jane Jones",
            "card.expiryMonth"  to "05",
            "card.expiryYear"   to "2034",
            "card.cvv"          to "123")
        val body: String = map.entries.joinToString("&")

        val token = "OGE4Mjk0MTc0YjdlY2IyODAxNGI5Njk5MjIwMDE1Y2N8c3k2S0pzVDg="

        val request:HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://eu-test.oppwa.com/v1/payments"))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        rsp11 = Json.decodeFromString<Response11>(response.body())
        if (response.statusCode() == 200) {
            if (rsp11.result.code.split(".")[0] == "000") {
                return true
            }
        }
        return false
    }

    // ----------------------------------------------------------------- //

    override fun stop(): Boolean {
        return true
    }

    // ----------------------------------------------------------------- //

    override fun nextAction(): Int {
        cid = pfsm.next(cid)
        return cid
    }

    // ----------------------------------------------------------------- //

    companion object {
        val pfsm = MarkovChain()

        init {
            pfsm.apply {
                add(0, listOf(
                    // PA
                    Edge(1, 500),
                    // DB
                    Edge(4, 500)
                ))
                add(1, listOf(
                    // CP
                    Edge(2, 1000)
                ))
                add(2, listOf(
                    // RF
                    Edge(3, 200),
                    // ..
                    Edge(0, 800)
                ))
                add(4, listOf(
                    // RF
                    Edge(3, 200),
                    // ...
                    Edge(0, 800)
                ))
            }
        }
    }

    // ----------------------------------------------------------------- //

}

/*-------------------------------------------------------------------------*/