///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.javalin:javalin:6.5.0
//DEPS io.javalin.community.ssl:ssl-plugin:6.5.0
//DEPS org.slf4j:slf4j-simple:2.0.17

// https://javalin.io/
import io.javalin.Javalin;
import io.javalin.community.ssl.SslPlugin;
import java.lang.System;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

public class JavalinServer {
    public static void main(String[] args) {

        SslPlugin plugin = new SslPlugin(conf -> {
            conf.pemFromPath("./cert.pem", "./key.pem", "12345");
            conf.sniHostCheck = false;
        });

        var app = Javalin.create(
            config -> {
                config.registerPlugin(plugin);
                config.useVirtualThreads=false;
                config.jetty.defaultHost="wfouche-e6540.local";
                })

            .get("/posts/{id}",
                ctx ->
                    ctx.result(
                        "{\"code\": \"OK\"}")
                            .contentType("application/json") )

            .start(7070);
    }
}
