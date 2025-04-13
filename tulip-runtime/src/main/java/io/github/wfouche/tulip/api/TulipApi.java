package io.github.wfouche.tulip.api;

import io.github.wfouche.tulip.core.TulipKt;
import io.github.wfouche.tulip.report.TulipReportKt;
import io.leego.banana.BananaUtils;
import io.leego.banana.Font;

/**
 * The TulipApi class provides the main interface for running Tulip benchmarks and generating
 * reports.
 */
public class TulipApi {

  /** Private constructor */
  TulipApi() {}

  /** The version string of the Tulip API. */
  public static final String VERSION = "2.1.8-dev";

  /**
   * A banner displaying the Tulip logo in ASCII art.
   *
   * @return The multi-line banner string
   */
  public static String getVersionBanner() {
    int idx = VERSION.lastIndexOf(".");
    String text = "Tulip " + VERSION.substring(0, idx);
    return BananaUtils.bananaify(text, Font.STANDARD);
  }

  /** The number of unique actions available in the benchmarking process. */
  public static final int NUM_ACTIONS = 101;

  /**
   * Runs the Tulip benchmarking process. This method initializes the configuration, runs the
   * benchmarks, and creates an HTML report.
   *
   * @param text The name of the JSONC benchmark configuration file, or a JSONC string starting with
   *     {.
   * @param userFactory A TulipUserFactory object responsible for creating Tulip users.
   */
  public static void runTulip(String text, TulipUserFactory userFactory) {
    String outputFilename = TulipKt.initConfig(text);
    TulipKt.runBenchmarks(userFactory);
    createHtmlReport(outputFilename, text);
  }

  /**
   * Runs the Tulip benchmarking process. This method initializes the configuration, runs the
   * benchmarks, and creates an HTML report.
   *
   * @param text The name of the JSONC benchmark configuration file, or a JSONC string starting with
   *     {.
   */
  public static void runTulip(String text) {
    String outputFilename = TulipKt.initConfig(text);
    TulipUserFactory userFactory = new TulipUserFactory();
    TulipKt.runBenchmarks(userFactory);
    createHtmlReport(outputFilename, text);
  }

  /**
   * Creates an HTML report from the benchmarking output.
   *
   * @param outputFilename The name of the output file containing the benchmarking results.
   * @param text The name of the JSONC benchmark configuration file, or a JSONC string starting with
   *     {.
   */
  public static void createHtmlReport(String outputFilename, String text) {
    TulipReportKt.createHtmlReport(outputFilename, text);
  }

  /**
   * The JAR has a main method
   *
   * @param args The command-line arguments variable.
   */
  public static void main(String[] args) {}
}
