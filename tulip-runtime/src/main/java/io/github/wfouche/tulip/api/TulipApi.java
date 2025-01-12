package io.github.wfouche.tulip.api;

import io.github.wfouche.tulip.core.TulipKt;
import io.github.wfouche.tulip.report.TulipReportKt;
import io.leego.banana.BananaUtils;
import io.leego.banana.Font;

import java.io.FileWriter;
import java.io.IOException;

/**
 * The TulipApi class provides the main interface for running Tulip benchmarks and generating reports.
 */
public class TulipApi {

    /**
     * Private constructor
     */
    TulipApi() { }

    /**
     * The version string of the Tulip API.
     */
    public static final String VERSION = "2.1.2-dev";

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
     * Runs the Tulip benchmarking process.
     * This method initializes the configuration, runs the benchmarks, and creates an HTML report.
     *
     * @param configFilename The name of the configuration file to be used for initialization.
     */
    public static void runTulip(String configFilename) {
        String outputFilename = TulipKt.initConfig(configFilename);
        TulipUserFactory userFactory = new TulipUserFactory();
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

    private static void writeToFile(String path, String content, boolean append) {
        try (FileWriter fileWriter = new FileWriter(path, append)) {
            fileWriter.write(content);
        } catch (IOException e) {
            // exception handling ...
        }
    }

    private static String benchmarkConfig = """
            {
                "actions": {
                    "description": "Demo Benchmark",
                    "output_filename": "benchmark_output.json",
                    "report_filename": "benchmark_report.html",
                    "user_class": "DemoUser",
                    "user_params": {
                        "url": "http://localhost:7070",
                        "tracing": false,
                        "http_port": 7070
                    },
                    "user_actions": {
                        "0": "onStart",
                        "1": "DELAY-10ms",
                        "2": "DELAY-20ms",
                        "3": "None",
                        "99": "onStop"
                    }
                },
                "contexts": {
                    "Context-1": {
                        "enabled": true,
                        "num_users": 4,
                        "num_threads": 2
                    },
                    "Context-2": {
                        "enabled": false,
                        "num_users": 4,
                        "num_threads": 4
                    }
                },
                "benchmarks": {
                    "Startup": {
                        "actions": [
                            {
                                "id": 0
                            }
                        ]
                    },
                    "Maximum Rate": {
                        "time": {
                            "pre_warmup_duration": 5,
                            "warmup_duration": 10,
                            "benchmark_duration": 30,
                            "benchmark_repeat_count": 3
                        },
                        "throughput_rate": 0.0,
                        "workflow": "random"
                    },
                    "Fixed Rate": {
                        "time": {
                            "pre_warmup_duration": 5,
                            "warmup_duration": 10,
                            "benchmark_duration": 30,
                            "benchmark_repeat_count": 3
                        },
                        "throughput_rate": 100.0,
                        "workflow": "random"
                    },
                    "Empty Action": {
                        "time": {
                            "pre_warmup_duration": 5,
                            "warmup_duration": 10,
                            "benchmark_duration": 30,
                            "benchmark_repeat_count": 3
                        },
                        "throughput_rate": 0.0,
                        "actions": [
                            {
                                "id": 3
                            }
                        ]
                    },
                    "Shutdown": {
                        "actions": [
                            {
                                "id": 99
                            }
                        ]
                    }
                },
                "workflows": {
                    "random": {
                        "-": {
                            "1": 0.10,
                            "2": 0.90
                        },
                        "1": {
                            "-": 1.0
                        },
                        "2": {
                            "-": 1.0
                        }
                    }
                }
            }
            """;

    private static String javaApp = """
            ///usr/bin/env jbang "$0" "$@" ; exit $?
            //DEPS io.github.wfouche.tulip:tulip-runtime:__TULIP_VERSION__
            //SOURCES DemoUser.java
            //JAVA 21
            //PREVIEW
            
            import io.github.wfouche.tulip.api.TulipApi;
            
            // $ jbang io.github.wfouche.tulip:tulip-runtime:__TULIP_VERSION__
            void main() {
               TulipApi.runTulip("benchmark_config.jsonc");
            }
            """;

    private static String javaUser = """
            import io.github.wfouche.tulip.api.*;
            
            public class DemoUser extends TulipUser {
            
                public DemoUser(int userId, int threadId) {
                    super(userId, threadId);
                }
            
                public boolean onStart() {
                    //TulipConsole.put("JavaDemoUser " + getUserId());
                    return true;
                }
            
                public boolean action1() {
                    try { Thread.sleep(10); } catch (InterruptedException e) { e.printStackTrace(); }
                    return true;
                }
            
                public boolean action2() {
                    try { Thread.sleep(20); } catch (InterruptedException e) { e.printStackTrace(); }
                    return true;
                }
            
                public boolean action3() {
                    return true;
                }
            
                public boolean onStop() {
                    return true;
                }
            
            }
            """;

    private static String runBenchSh = """
            #!/bin/bash
            rm -f benchmark_report.html
            export JBANG_JAVA_OPTIONS="-server -Xmx1024m -XX:+UseZGC -XX:+ZGenerational"
            jbang run App.java
            echo ""
            w3m -dump -cols 205 benchmark_report.html
            #lynx -dump -width 205 benchmark_report.html
            jbang run https://gist.github.com/wfouche/70738de122128bbc19ea888799151699 benchmark_config.adoc
            """;

    private static String runBenchCmd = """
            del benchmark_report.html
            set JBANG_JAVA_OPTIONS=-server -Xmx1024m -XX:+UseZGC -XX:+ZGenerational
            call jbang run App.java
            @echo off
            echo.
            REM call w3m.exe -dump -cols 205 benchmark_report.html
            start benchmark_report.html
            jbang run https://gist.github.com/wfouche/70738de122128bbc19ea888799151699 benchmark_config.adoc
            start benchmark_config.html
            """;

    /**
     * Create a new Tulip benchmark project with Jbang support for Java
     *
     * @param args The name of the programming language to generate a benchmark proj
     */
    public static void main(String[] args) {
        // jbang io.github.wfouche.tulip:tulip-runtime:<version>
        System.out.println("Tulip: creating a Java benchmark with JBang support");
        writeToFile("benchmark_config.jsonc", benchmarkConfig.stripLeading(), false);
        writeToFile("App.java", javaApp.stripLeading().replace("__TULIP_VERSION__", VERSION), false);
        writeToFile("DemoUser.java", javaUser.stripLeading(), false);
        writeToFile("run_bench.sh", runBenchSh.stripLeading(), false);
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            // pass
        } else {
            try {
                String[] cmdArray = {"chmod", "u+x", "run_bench.sh"};
                Runtime.getRuntime().exec(cmdArray);
            } catch (IOException e) {
                // pass
            }
        }
        writeToFile("run_bench.cmd", runBenchCmd.stripLeading(), false);
    }
}