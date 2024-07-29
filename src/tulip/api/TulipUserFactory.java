package tulip.api;

import tulip.user.VirtualUser;

public abstract class TulipUserFactory {

    public VirtualUser getUser(int userId, String className) {
        return null;
    }

}

