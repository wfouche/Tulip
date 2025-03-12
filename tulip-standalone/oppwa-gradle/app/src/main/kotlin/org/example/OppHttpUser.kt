package org.example

/*-------------------------------------------------------------------------*/

import io.github.wfouche.tulip.api.TulipUser
import io.github.wfouche.tulip.user.HttpUser
import io.github.wfouche.tulip.core.Console
import io.github.wfouche.tulip.core.delayMillisRandom
//import java.net.URI
//import java.net.http.HttpClient
//import java.net.http.HttpRequest
//import java.net.http.HttpResponse
import io.github.wfouche.tulip.pfsm.Edge
import io.github.wfouche.tulip.pfsm.MarkovChain
import java.lang.Exception
import kotlinx.serialization.*
import kotlinx.serialization.json.*

/*-------------------------------------------------------------------------*/

//private val client = HttpClient.newHttpClient()

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

class OppHttpUser(userId: Int, threadId: Int) : HttpUser(userId, threadId) {

    // ----------------------------------------------------------------- //

    private var id: String = ""

    // ----------------------------------------------------------------- //

    override fun onStart(): Boolean {
        if (super.onStart()) {
            if (userId == 0) {
                token = getUserParamValue("token")
                return true;
            }
        }
        return false;
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
        val bodyText: String = map.entries.joinToString("&")

        val rspText: String? = restClient().post()
            .uri("/v1/payments")
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(bodyText)
            .retrieve()
            .body(String::class.java)

        id = ""
        if (rspText != null) {
            val rsp = Json.decodeFromString<AuthResponse>(rspText!!)
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
        val bodyText: String = map.entries.joinToString("&")

        val rspText: String? = restClient().post()
            .uri("/v1/payments/${id}")
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(bodyText)
            .retrieve()
            .body(String::class.java)

        if (rspText != null) {
            val rsp = Json.decodeFromString<CompResponse>(rspText!!)
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
            "paymentBrand"      to "VISA",
            "paymentType"       to "DB",
            "card.number"       to "4200000000000000",
            "card.holder"       to "Jane Jones",
            "card.expiryMonth"  to "05",
            "card.expiryYear"   to "2034",
            "card.cvv"          to "123")
        val bodyText: String = map.entries.joinToString("&")

        val rspText: String? = restClient().post()
            .uri("/v1/payments")
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(bodyText)
            .retrieve()
            .body(String::class.java)

        id = ""
        if (rspText != null) {
            val rsp = Json.decodeFromString<AuthResponse>(rspText!!)
            if (rsp.result.code.split(".")[0] == "000") {
                id = rsp.id
                return true
            }
        }
        return false
    }

    // ----------------------------------------------------------------- //

    override fun action4(): Boolean {
        val map = mapOf(
            "entityId"          to "8a8294174b7ecb28014b9699220015ca",
            "amount"            to "92.00",
            "currency"          to "EUR",
            "paymentType"       to "RF")
        val bodyText: String = map.entries.joinToString("&")

        val rspText: String? = restClient().post()
            .uri("/v1/payments/${id}")
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(bodyText)
            .retrieve()
            .body(String::class.java)

        if (rspText != null) {
            val rsp = Json.decodeFromString<CompResponse>(rspText!!)
            if (rsp.result.code.split(".")[0] == "000") {
                return true
            }
        }
        return true
    }

    // ----------------------------------------------------------------- //

    override fun onStop(): Boolean {
        return true
    }

    // ----------------------------------------------------------------- //

    companion object {
        private var token: String = ""
    }

}

/*-------------------------------------------------------------------------*/
