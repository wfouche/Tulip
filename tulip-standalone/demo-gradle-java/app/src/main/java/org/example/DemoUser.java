package org.example;

import io.github.wfouche.tulip.api.TulipConsole;
import io.github.wfouche.tulip.api.TulipUser;
import io.github.wfouche.tulip.api.TulipUtils;

public class DemoUser extends TulipUser {

    public DemoUser(int userId, int threadId) {
        super(userId, threadId);
    }

    public boolean onStart() {
        TulipConsole.put("JavaDemoUser " + getUserId());
        return true;
    }

    public boolean action1() {
        TulipUtils.delayMillisFixed(10);
        return true;
    }

    public boolean action2() {
        TulipUtils.delayMillisFixed(20);
        return true;
    }

    public boolean action3() {
        return true;
    }

    public boolean onStop() {
        return true;
    }

}
