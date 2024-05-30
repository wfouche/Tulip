#!/usr/bin/env kscript
//DEPS io.javalin:javalin:6.1.6
//DEPS org.slf4j:slf4j-simple:2.0.12

// https://javalin.io/
import io.javalin.Javalin

fun main() {
    val app = Javalin.create(/*config*/)
        .get("/") { ctx -> ctx.result("Hello World") }
        .start(7070)
}

// $ kscript JavalinServer.kt