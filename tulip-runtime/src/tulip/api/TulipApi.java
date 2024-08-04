package tulip.api;

public class TulipApi {

    public static final String VERSION_STRING = "2.0.0-beta4";

    public static final int NUM_ACTIONS = 100;

    public static void runTulip(String configFilename, TulipUserFactory userFactory) {
        tulip.core.TulipKt.initConfig(configFilename);
        tulip.core.TulipKt.runTests(userFactory);
    }
}
