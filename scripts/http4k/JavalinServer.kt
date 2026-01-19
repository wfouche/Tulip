//DEPS org.http4k:http4k-core:6.26.0.0
//DEPS org.http4k:http4k-server-netty:6.26.0.0
//DEPS org.slf4j:slf4j-simple:2.0.17
//KOTLIN 2.3.0
//JAVA 25
//RUNTIME_OPTIONS -Xms1g -Xmx1g -XX:+UseZGC

import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Method.PUT
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.server.Netty
import org.http4k.server.asServer
import kotlin.system.exitProcess

fun main() {
    // Reusable response logic
    val okResponse = { _: Request -> 
        Response(OK)
            .header("Content-Type", "application/json")
            .body("""{"code": "OK"}""") 
    }

    val app = routes(
        "/" bind GET to okResponse,
        
        "/exit" bind GET to {
            println("Exit.")
            exitProcess(0)
        },

        "/posts" bind routes(
            "/" bind POST to okResponse,
            "/{id}" bind GET to okResponse,
            "/{id}" bind PUT to okResponse,
            "/{id}" bind DELETE to okResponse
        ),

        "/comments/{id}" bind GET to okResponse,
        "/albums/{id}" bind GET to okResponse,
        "/photos/{id}" bind GET to okResponse,
        "/todos/{id}" bind GET to okResponse,

        "/rendezvous" bind GET to { req: Request ->
            println(req.bodyString())
            okResponse(req)
        }
    )

    println("Starting server on port 7070...")
    app.asServer(Netty(7070)).start()
}
