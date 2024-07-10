package user.http;

import tulip.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.URI;
import java.net.http.HttpResponse;

import static tulip.Tulip_utilsKt.delayMillisRandom;

public class HttpUser2 extends VirtualUser {

    private static final HttpClient client = HttpClient.newHttpClient();

    private static boolean serviceCall (String resource, int userId) {
        // https://www.baeldung.com/java-httpclient-connection-management
        var id = userId + 1;
        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:7070" + "/" + resource + "/" + Integer.valueOf(userId).toString()))
                    .GET()
                    .build();
        } catch (URISyntaxException e) {
            return false;
        }

        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            return false;
        }

        //println(id)
        //println(name)
        //println(response.statusCode())
        //println(response.body())

        return (response.statusCode() == 200);
    }

    public HttpUser2(int userId) {
        super(userId);
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean action1() {
        delayMillisRandom(0, 12);
        return true;
    }

    @Override
    public boolean action2() {
        delayMillisRandom(0, 28);
        return true;
    }

    @Override
    public boolean action3() {
        return serviceCall("posts", this.getUserId());
    }

    @Override
    public boolean action4() {
        return serviceCall("comments", this.getUserId());
    }

    @Override
    public boolean action5() {
        return serviceCall("albums", this.getUserId());
    }

    @Override
    public boolean action6() {
        return serviceCall("photos", this.getUserId());
    }

    @Override
    public boolean action7() {
        return serviceCall("todos", this.getUserId());
    }

    @Override
    public boolean action8() {
        return serviceCall("posts", this.getUserId());
    }

    @Override
    public boolean action9() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    static {
        System.out.println("Loading .... Java class ... HttpUser2");
    }
}
