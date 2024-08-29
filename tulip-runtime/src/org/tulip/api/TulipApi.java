package org.tulip.api;

import org.tulip.core.TulipKt;
import org.tulip.report.TulipReportKt;

public class TulipApi {

    public static final String VERSION_STRING = "2.0.0-beta6";

    public static final int NUM_ACTIONS = 100;

    public static String runTulip(String configFilename, TulipUserFactory userFactory) {
        String outputFilename = TulipKt.initConfig(configFilename);
        TulipKt.runTests(userFactory);
        TulipReportKt.createHtmlReport(outputFilename);
    }
}
