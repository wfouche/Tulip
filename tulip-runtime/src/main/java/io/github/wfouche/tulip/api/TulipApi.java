package io.github.wfouche.tulip.api;

import io.github.wfouche.tulip.core.TulipKt;
import io.github.wfouche.tulip.report.TulipReportKt;

/**
 * The TulipApi class provides the main interface for running Tulip benchmarks and generating reports.
 */
public class TulipApi {

    /**
     * The version string of the Tulip API.
     */
    public static final String VERSION_STRING = "0.1.1";

    /**
     * A banner displaying the Tulip logo in ASCII art.
     */
    public static final String VERSION_BANNER = """                                       
  _____      _ _          ___   _   ___ \s
 |_   _|   _| (_)_ __    / _ \\ / | / _ \\\s
   | || | | | | | '_ \\  | | | || || | | |
   | || |_| | | | |_) | | |_| || || |_| |
   |_| \\__,_|_|_| .__/   \\___(_)_(_)___/\s
                |_|                     \s
""";
    // https://devops.datenkollektiv.de/banner.txt/index.html
    // <standard>

    /**
     * The number of unique actions available in the benchmarking process.
     */
    public static final int NUM_ACTIONS = 100;

    /**
     * Runs the Tulip benchmarking process.
     * This method initializes the configuration, runs the benchmarks, and creates an HTML report.
     *
     * @param configFilename The name of the configuration file to be used for initialization.
     * @param userFactory    A TulipUserFactory object responsible for creating Tulip users.
     */
    public static void runTulip(String configFilename, TulipUserFactory userFactory) {
        String outputFilename = TulipKt.initConfig(configFilename);
        TulipKt.runBenchmarks(userFactory);
        createHtmlReport(outputFilename);
    }

    /**
     * Creates an HTML report from the benchmarking output.
     *
     * @param outputFilename The name of the output file containing the benchmarking results.
     */
    public static void createHtmlReport(String outputFilename) {
        TulipReportKt.createHtmlReport(outputFilename);
    }
}