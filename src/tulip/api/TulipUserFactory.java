package tulip.api;

import tulip.user.TulipUser;

public abstract class TulipUserFactory {

    public TulipUser getUser(int userId, String className) {
        // TODO - implement dynamic class loading (not urgent)
        return null;
    }

}

