/*-------------------------------------------------------------------------*/

import io.github.wfouche.tulip.api.TulipUser;
import io.github.wfouche.tulip.api.TulipUtils;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/*-------------------------------------------------------------------------*/

public class HttpUser extends TulipUser {

    // ----------------------------------------------------------------- //

    private HttpRequest requestPosts = createRequest("posts");
    private HttpRequest requestComments = createRequest("comments");
    private HttpRequest requestAlbums = createRequest("albums");
    private HttpRequest requestPhotos = createRequest("photos");
    private HttpRequest requestTodos = createRequest("todos");

    private int userId;

    public HttpUser(int userId, int threadId) {
        super(userId, threadId);
        this.userId = userId;
    }

    public boolean onStart() {
        var actionId = 0;
        return true;
    }

    // ----------------------------------------------------------------- //

    public boolean action1() {
        // 6 ms delay (average)
        TulipUtils.delayMillisRandom(0, 12);
        return true;
    }

    public boolean action2() {
        // 14 ms delay (average)
        TulipUtils.delayMillisRandom(0, 28);
        return true;
    }

    // ----------------------------------------------------------------- //

    public boolean action3() {
        return serviceCall(requestPosts);
    }

    public boolean action4() {
        return serviceCall(requestComments);
    }

    public boolean action5() {
        return serviceCall(requestAlbums);
    }

    public boolean action6() {
        return serviceCall(requestPhotos);
    }

    public boolean action7() {
        return serviceCall(requestTodos);
    }

    // ----------------------------------------------------------------- //

    public boolean action8() {
        var actionId = 8;
        return true;
    }

    // ----------------------------------------------------------------- //

    public boolean action9()  {
        return true;
    }

    // ----------------------------------------------------------------- //

    public boolean action10() {
        TulipUtils.delayMillisFixed(10);
        return true;
    }

    // ----------------------------------------------------------------- //

    public boolean onStop() {
        //Thread.sleep(100)
        return true;
    }

    // ----------------------------------------------------------------- //

    private HttpRequest createRequest(String name) {
        try {
            var id = userId + 1;
            var url = this.getUserParamValue("url");
            var request = HttpRequest.newBuilder()
                    .uri(new URI(url + "/" + name + "/" + String.valueOf(id)))
                    .GET()
                    .build();
            return request;
        } catch (java.lang.Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static HttpClient client = HttpClient.newHttpClient();

    private static boolean serviceCall(HttpRequest request) {
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            //println(id)
            //println(name)
            //println(response.statusCode())
            //println(response.body())

            return (response.statusCode() == 200);
        } catch (java.lang.Exception e) {
            throw new RuntimeException(e);
        }
    }

}

/*-------------------------------------------------------------------------*/
