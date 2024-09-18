package io.github.wfouche.tulip.api;

import io.github.wfouche.tulip.core.TulipKt;
import io.github.wfouche.tulip.report.TulipReportKt;

public class TulipApi {

    public static final String VERSION_STRING = "0.1.0-beta10";
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
        createHtmlReport(outputFilename);
    }

    public static void createHtmlReport(String outputFilename) {
        TulipReportKt.createHtmlReport(outputFilename);
    }
}
