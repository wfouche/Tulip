///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.javalin:javalin:6.4.0
//DEPS org.slf4j:slf4j-simple:2.0.12

// https://javalin.io/
import io.javalin.Javalin

public fun main() {
    val app = Javalin.create(/*config*/)
        .get("/") { ctx -> ctx.result("{\"code\": \"OK\"}").contentType("application/json") }
        .get("/posts/{id}") { ctx -> ctx.result("{\"code\": \"OK\"}").contentType("application/json") }
        .get("/comments/{id}") { ctx -> ctx.result("{\"code\": \"OK\"}").contentType("application/json") }
        .get("/albums/{id}") { ctx -> ctx.result("{\"code\": \"OK\"}").contentType("application/json") }
        .get("/photos/{id}") { ctx -> ctx.result("{\"code\": \"OK\"}").contentType("application/json") }
        .get("/todos/{id}") { ctx -> ctx.result("{\"code\": \"OK\"}").contentType("application/json") }
        .start(7070)
}
// $ ./JavalinServer.kt        # or use command below
// $ jbang JavalinServer.kt
