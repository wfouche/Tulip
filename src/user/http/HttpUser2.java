package user.http;

import tulip.user.TulipUser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.URI;
import java.net.http.HttpResponse;

import static tulip.core.TulipKt.delayMillisRandom;

public class HttpUser2 extends TulipUser {

    private static final HttpClient client = HttpClient.newHttpClient();

    private static boolean serviceCall (TulipUser user, String resource, int userId) {
        // https://www.baeldung.com/java-httpclient-connection-management
        var id = userId + 1;
        HttpRequest request = null;
        try {
            String url = user.getUserParamValue("url");
            request = HttpRequest.newBuilder()
                    .uri(new URI(url + "/" + resource + "/" + Integer.valueOf(userId).toString()))
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
        return serviceCall(this,"posts", this.getUserId());
    }

    @Override
    public boolean action4() {
        return serviceCall(this,"comments", this.getUserId());
    }

    @Override
    public boolean action5() {
        return serviceCall(this,"albums", this.getUserId());
    }

    @Override
    public boolean action6() {
        return serviceCall(this,"photos", this.getUserId());
    }

    @Override
    public boolean action7() {
        return serviceCall(this,"todos", this.getUserId());
    }

    @Override
    public boolean action8() {
        return serviceCall(this,"posts", this.getUserId());
    }

    @Override
    public boolean action9() {
        return true;
    }

    @Override
    public boolean action10() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean stop() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    static {
        System.out.println("Loading .... Java class ... HttpUser2");
    }
}
