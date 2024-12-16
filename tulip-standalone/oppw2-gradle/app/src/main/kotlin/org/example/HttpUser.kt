package org.example

/*-------------------------------------------------------------------------*/

import io.github.wfouche.tulip.api.TulipUser
import io.github.wfouche.tulip.core.Console
import io.github.wfouche.tulip.core.delayMillisRandom
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import io.github.wfouche.tulip.pfsm.Edge
import io.github.wfouche.tulip.pfsm.MarkovChain
import java.lang.Exception
import kotlinx.serialization.*
import kotlinx.serialization.json.*

/*-------------------------------------------------------------------------*/

private val client = HttpClient.newHttpClient()

/*-------------------------------------------------------------------------*/

enum class State(val state:Int) {
    IDLE(0), // Idle
    PA(1),   // Auth
    CP(2),   // Comp
    RF(3),   // Refund
    DB(4)    // Debit
}

// https://transform.tools/json-to-kotlin

@Serializable
data class AuthResponse(
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
data class CompResponse(
    val id: String,
    val referencedId: String,
    val paymentType: String,
    val amount: String,
    val currency: String,
    val descriptor: String,
    val result: Result,
    val resultDetails: ResultDetails,
    val buildNumber: String,
    val timestamp: String,
    val ndc: String,
    val source: String,
    val paymentMethod: String,
    val shortId: String,
)

/*-------------------------------------------------------------------------*/

class HttpUser(userId: Int, threadId: Int) : TulipUser(userId, threadId) {

    // ----------------------------------------------------------------- //

    private var cid: Int = 0

    private var id: String = ""

    private val token = "OGE4Mjk0MTc0YjdlY2IyODAxNGI5Njk5MjIwMDE1Y2N8ZmY0b1UhZSVlckI9YUJzQj82KyU="

    // ----------------------------------------------------------------- //

    override fun onStart(): Boolean {
        return true
    }

    // ----------------------------------------------------------------- //
    // https://docs.oppwa.com/integrations/server-to-server#syncPayment
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

        val request:HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://eu-test.oppwa.com/v1/payments"))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        id = ""
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        // Console.put(response.body())
        if (response.statusCode() == 200) {
            val rsp = Json.decodeFromString<AuthResponse>(response.body())
            if (rsp.result.code.split(".")[0] == "000") {
                id = rsp.id
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

        val request:HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://eu-test.oppwa.com/v1/payments/${id}"))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() == 200) {
            val rsp = Json.decodeFromString<CompResponse>(response.body())
            if (rsp.result.code.split(".")[0] == "000") {
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

        val request:HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://eu-test.oppwa.com/v1/payments/${id}"))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() == 200) {
            val rsp = Json.decodeFromString<CompResponse>(response.body())
            if (rsp.result.code.split(".")[0] == "000") {
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

        val request:HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://eu-test.oppwa.com/v1/payments"))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        id = ""
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() == 200) {
            val rsp = Json.decodeFromString<AuthResponse>(response.body())
            if (rsp.result.code.split(".")[0] == "000") {
                id = rsp.id
                return true
            }
        }
        return false
    }

    // ----------------------------------------------------------------- //

    override fun onStop(): Boolean {
        return true
    }

    // ----------------------------------------------------------------- //

    override fun nextAction(workflowId: Int): Int {
        // 0 => {1:PA or 4:DB}
        // 1:PA => {2:CP}
        // 2:CP => {3:RF or 0}
        // 3:RF => {0}
        // 4:DB => {3:RF, or 0}

        val nid = workflow.next(cid)
        // PA(1) -> CP(2)
        if (State.PA.equals(cid) && State.CP.equals(nid) && id == "") {
            // Skip CP(2), if PA(1) failed; id == ""
            cid = workflow.next(State.IDLE.ordinal)
        // DB(4) -> RF(3)
        } else if (State.DB.equals(cid) && State.RF.equals(nid) && id == "") {
            cid = workflow.next(State.IDLE.ordinal)
        } else {
            cid = nid
        }
        return cid
    }

    // ----------------------------------------------------------------- //

    companion object {
        val workflow = MarkovChain()

        init {
            workflow.apply {
                add(State.IDLE.ordinal, listOf(
                    // PA - Credit Card
                    Edge(State.PA.ordinal, 500),
                    // DB - Debit Card
                    Edge(State.DB.ordinal, 500)
                ))
                add(State.PA.ordinal, listOf(
                    // CP - Credit Card
                    Edge(State.CP.ordinal, 1000)
                ))
                add(State.CP.ordinal, listOf(
                    // RF - Credit Card
                    Edge(State.RF.ordinal, 200),
                    // ..
                    Edge(State.IDLE.ordinal, 800)
                ))
                add(State.RF.ordinal, listOf(
                    // Debit or Credit Card
                    Edge(State.IDLE.ordinal, 1000)
                ))
                add(State.DB.ordinal, listOf(
                    // RF - Debit Card
                    Edge(State.RF.ordinal, 200),
                    // ...
                    Edge(State.IDLE.ordinal, 800)
                ))
            }
        }
    }

    // ----------------------------------------------------------------- //

}

/*-------------------------------------------------------------------------*/
