package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import io.ktor.server.util.getValue

import kotlinx.serialization.Serializable

@Serializable
data class User(val status: String = "OK")

val user: User = User("OK")

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json()
        }
        routing {
            route("/posts") {
                get {
                    call.respond(HttpStatusCode.OK, user)
                }
                get("{id}") {
                    call.respond(HttpStatusCode.OK, user)
                }

                post {
                    call.respond(HttpStatusCode.Created,user)
                }
            }
        }
    }.start(wait = true)
}
