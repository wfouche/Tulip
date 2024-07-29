import tulip.api.TulipUserFactory;
import tulip.user.TulipUser;

public class Benchmark01UserFactory extends TulipUserFactory {
    @Override
    public TulipUser getUser(int userId, String className) {
        return switch (className) {
            case "user.http.HttpUser" -> new user.http.HttpUser(userId);
            case "user.http.HttpUser2" -> new user.http.HttpUser2(userId);
            default -> null;
        };
    }
}
