///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.springframework.boot:spring-boot-starter-web:3.4.1

import static java.lang.System.*;
import org.springframework.web.client.RestClient;


// https://dev.to/janux_de/getting-started-with-spring-restclient-5962

public class spring_restclient {

    public static void main(String... args) {
        var restClient = RestClient.create();

        String response = restClient.get()
            .uri("https://jsonplaceholder.typicode.com/posts/1")
            .retrieve()
            .body(String.class);

        System.out.println(response);

        var restClient2 = RestClient.builder()
            .baseUrl("https://jsonplaceholder.typicode.com")
            .build();

        String response2 = restClient2.get()
            .uri("/posts/1")
            .retrieve()
            .body(String.class);

        System.out.println(response2);

    }
}
