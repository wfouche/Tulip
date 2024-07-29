package tulip.api;

public class TulipApi {

    public static void runTulip(String configFilename, TulipUserFactory userFactory) {
        tulip.core.TulipKt.initConfig(configFilename);
        tulip.core.TulipKt.runTests(userFactory);
    }
}
