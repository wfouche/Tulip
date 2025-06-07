package io.github.wfouche.tulip.user;

/** HttpUserTest - experimental class */
public class HttpUserTest extends HttpUser {

    /** HttpUserTest() constructor */
    public HttpUserTest() {
        super(0, 0);
    }

    /**
     * init - set base class parameter values
     *
     * @param userId
     * @param threadId
     */
    public void init(int userId, int threadId) {
        setUserId(userId);
        setThreadId(threadId);
    }
}
