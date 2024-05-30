#!/usr/bin/env kscript
@file:Repository("https://repo.maven.apache.org/maven2")
@file:DependsOn("io.javalin:javalin:6.1.6")
@file:DependsOn("org.slf4j:slf4j-simple:2.0.12")

// https://javalin.io/
import io.javalin.Javalin

val app = Javalin.create(/*config*/)
    .get("/posts/{id}")    { ctx -> ctx.result("{\"code\": \"OK\"}").contentType("application/json") }
    .get("/comments/{id}") { ctx -> ctx.result("{\"code\": \"OK\"}").contentType("application/json") }
    .get("/albums/{id}")   { ctx -> ctx.result("{\"code\": \"OK\"}").contentType("application/json") }
    .get("/photos/{id}")   { ctx -> ctx.result("{\"code\": \"OK\"}").contentType("application/json") }
    .get("/todos/{id}")    { ctx -> ctx.result("{\"code\": \"OK\"}").contentType("application/json") }
    .start(7070)

// $ ./JavalinServer.kts        # or use command below
// $ kscript JavalinServer.kts