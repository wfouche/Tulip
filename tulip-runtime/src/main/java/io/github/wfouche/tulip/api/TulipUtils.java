package io.github.wfouche.tulip.api;

public class TulipUtils {

    public static void delayMillisRandom(long delayFrom, long delayTo) {
        io.github.wfouche.tulip.core.TulipKt.delayMillisRandom(delayFrom, delayTo);
    }

    public static void delayMillisFixed(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
