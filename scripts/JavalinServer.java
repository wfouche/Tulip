///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.javalin:javalin:6.7.0
//DEPS org.slf4j:slf4j-simple:2.0.17

// https://javalin.io/
import io.javalin.Javalin;
import java.lang.System;

public class JavalinServer {
    public static void main(String[] args) {
        var app = Javalin.create(config -> config.useVirtualThreads=true)
            .get("/", ctx -> {ctx.status(200); ctx.result("{\"code\": \"OK\"}").contentType("application/json");} )
            .get("/exit", ctx -> {System.out.println("Exit."); System.exit(0); })
            .post("/posts", ctx -> {ctx.status(201); ctx.result("{\"code\": \"OK\"}").contentType("application/json");} )
            .get("/posts", ctx -> {ctx.status(200); ctx.result("{\"code\": \"OK\"}").contentType("application/json");} )
            .get("/posts/{id}", ctx -> {ctx.status(200); ctx.result("{\"code\": \"OK\"}").contentType("application/json");} )
            .put("/posts/{id}", ctx -> {ctx.status(200); ctx.result("{\"code\": \"OK\"}").contentType("application/json");} )
            .delete("/posts/{id}", ctx -> {ctx.status(200); ctx.result("{\"code\": \"OK\"}").contentType("application/json");} )
            .get("/comments/{id}", ctx -> {ctx.status(200); ctx.result("{\"code\": \"OK\"}").contentType("application/json");} )
            .get("/albums/{id}", ctx -> {ctx.status(200); ctx.result("{\"code\": \"OK\"}").contentType("application/json");} )
            .get("/photos/{id}", ctx -> {ctx.status(200); ctx.result("{\"code\": \"OK\"}").contentType("application/json");} )
            .get("/todos/{id}", ctx -> {ctx.status(200); ctx.result("{\"code\": \"OK\"}").contentType("application/json");} )
            // curl -X GET http://localhost:7070:/rendezvous/ -H 'Content-Type: application/json' -d '{"login":"my_login","password":"my_password"}'
            .get("/rendezvous", ctx -> {
                System.out.println(ctx.body());
                ctx.result("{\"code\": \"OK\"}").contentType("application/json");
            } )
            .start(7070);
    }
}
