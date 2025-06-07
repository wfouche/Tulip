package io.github.wfouche.tulip.api;

/** TulipUtils is a utility class. */
public class TulipUtils {

    /** Private constructor */
    TulipUtils() {}

    /**
     * Introduces a random delay between the specified minimum and maximum durations.
     *
     * @param delayFrom The minimum duration of the delay in milliseconds.
     * @param delayTo The maximum duration of the delay in milliseconds.
     */
    public static void delayMillisRandom(long delayFrom, long delayTo) {
        io.github.wfouche.tulip.core.TulipKt.delayMillisRandom(delayFrom, delayTo);
    }

    /**
     * Introduces a fixed delay for the specified duration.
     *
     * @param delay The duration of the delay in milliseconds.
     * @throws RuntimeException If the thread is interrupted while sleeping.
     */
    public static void delayMillisFixed(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
