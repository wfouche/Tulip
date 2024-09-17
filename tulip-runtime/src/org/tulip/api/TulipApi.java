package org.tulip.api;

import org.tulip.core.TulipKt;
import org.tulip.report.TulipReportKt;

public class TulipApi {

    public static final String VERSION_STRING = "1.0.0-beta9";
    public static final String VERSION_BANNER = """                                       
  _____      _ _         _   ___  
 |_   _|   _| (_)_ __   / | / _ \\ 
   | || | | | | | '_ \\  | || | | |
   | || |_| | | | |_) | | || |_| |
   |_| \\__,_|_|_| .__/  |_(_)___/ 
                |_|               
""";
// https://devops.datenkollektiv.de/banner.txt/index.html
// <standard>

    public static final int NUM_ACTIONS = 100;

    public static void runTulip(String configFilename, TulipUserFactory userFactory) {
        String outputFilename = TulipKt.initConfig(configFilename);
        TulipKt.runTests(userFactory);
        TulipReportKt.createHtmlReport(outputFilename);
    }
}
