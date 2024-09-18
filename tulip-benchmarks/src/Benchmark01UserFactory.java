import io.github.wfouche.tulip.api.TulipUserFactory;
import io.github.wfouche.tulip.api.TulipUser;

public class Benchmark01UserFactory extends TulipUserFactory {
    @Override
    public TulipUser getUser(int userId, String className, int threadId) {
        return switch (className) {
            case "user.http.HttpUser" -> new user.http.HttpUser(userId, threadId);
            case "user.http.HttpUser2" -> new user.http.HttpUser2(userId, threadId);
            default -> null;
        };
    }
}
