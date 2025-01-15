///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.springframework.boot:spring-boot-starter-web:3.4.1

import static java.lang.System.*;
import org.springframework.web.client.RestClient;

public class spring_restclient {

    public static void main(String... args) {
        var restClient = RestClient.create();

        String response = restClient.get()
            .uri("https://jsonplaceholder.typicode.com/posts/1")
            .retrieve()
            .body(String.class);

        System.out.println(response);
        
        out.println("Hello World");
    }
}
