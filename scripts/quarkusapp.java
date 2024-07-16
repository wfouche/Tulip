//usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.quarkus.platform:quarkus-bom:3.12.2@pom
//DEPS io.quarkus:quarkus-rest

import io.quarkus.runtime.Quarkus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/hello")
@ApplicationScoped
public class quarkusapp {

    @GET
    public String hello() {
        return "hello";
    }

    public static void main(String[] args) {
        Quarkus.run(args);
    }

    // Posts Service
    @Inject
    PostsService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/posts/{id}")
    public String posts(Integer id) {
        return service.posts(id);
    }

    @ApplicationScoped
    static public class PostsService {

        public String posts(Integer id) {
            return "{\"id\": \"" + id.toString() + "\"}";
        }
    }
    
}
