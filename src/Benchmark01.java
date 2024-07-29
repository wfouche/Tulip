import tulip.api.TulipApi;
import tulip.api.TulipUserFactory;
import tulip.user.TulipUser;

public class Benchmark01 {

    public static void main(String[] args) {
        TulipApi.runTulip("./src/user/http/config.json", new Benchmark01UserFactory());
    }

}
