import io.github.wfouche.tulip.api.TulipUser

class DemoUser(userId: Int, threadId: Int) : TulipUser(userId, threadId) {

    override fun onStart(): Boolean {
        return true
    }

    override fun action1(): Boolean {
        Thread.sleep(10)
        return true
    }

    override fun action2(): Boolean {
        Thread.sleep(20)
        return true
    }

    override fun action3(): Boolean {
        return true
    }

    override fun onStop(): Boolean {
        return true
    }
}
