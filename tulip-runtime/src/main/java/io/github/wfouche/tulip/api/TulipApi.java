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
                // Actions
                "actions": {
                    "description": "Spring RestClient Benchmark",
                    "output_filename": "benchmark_output.json",
                    "report_filename": "benchmark_report.html",
                    "user_class": "HttpUser",
                    "user_params": {
                        "baseURI": "https://jsonplaceholder.typicode.com"
                    },
                    "user_actions": {
                        "0": "onStart",  // Init
                        "1": "GET:posts",
                        "2": "GET:comments",
                        "3": "GET:todos",
                        "99": "onStop"   // Shutdown
                    }
                },
                // Workflows using Markov chains
                "workflows": {
                    "api-user": {
                        "-": {
                            "1": 0.40,
                            "3": 0.60
                        },
                        "1": {
                            "2": 1.0
                        },
                        "2": {
                            "-": 1.0
                        },
                        "3": {
                            "-": 1.0
                        }
                    }
                },
                // Benchmarks
                "benchmarks": {
                    "onStart": {
                        "scenario_actions": [ {"id": 0} ]
                    },
                     "REST1": {
                        "enabled": true,
                        "time": {
                            "pre_warmup_duration": 0,
                            "warmup_duration": 10,
                            "benchmark_duration": 30,
                            "benchmark_repeat_count": 3
                        },
                        "throughput_rate": 10.0,
                        "scenario_actions": [
                            {
                                "id": 1
                            }
                        ]
                    },
                    "REST2": {
                        "enabled": true,
                        "time": {
                            "pre_warmup_duration": 0,
                            "warmup_duration": 10,
                            "benchmark_duration": 30,
                            "benchmark_repeat_count": 3
                        },
                        "throughput_rate": 10.0,
                        "scenario_actions": [
                            {
                                "id": 1, "weight": 10
                            },
                            {
                                "id": 2, "weight": 40
                            },
                            {
                                "id": 3, "weight": 50
                            }
                        ]
                    },
                    "REST3": {
                        "enabled": true,
                        "time": {
                            "pre_warmup_duration": 0,
                            "warmup_duration": 10,
                            "benchmark_duration": 30,
                            "benchmark_repeat_count": 3
                        },
                        "throughput_rate": 10.0,
                        "scenario_workflow": "api-user"
                    },
                    "onStop": {
                        "scenario_actions": [
                            {
                                "id": 99
                            }
                        ]
                    }
                },
                // Contexts
                "contexts": {
                    "Context-1": {
                        "enabled": true,
                        "num_users": 10,
                        "num_threads": 2
                    }
                }
            }
            """;

    private static String javaApp = """
            ///usr/bin/env jbang "$0" "$@" ; exit $?
            //DEPS io.github.wfouche.tulip:tulip-runtime:__TULIP_VERSION__
            //DEPS org.springframework.boot:spring-boot-starter-web:3.4.1
            //SOURCES HttpUser.java
            //JAVA 21
            
            import io.github.wfouche.tulip.api.TulipApi;
            
            public class App {
               public static void main(String[] args) {
                  TulipApi.runTulip("benchmark_config.jsonc");
               }
            }
            """;

    private static String kotlinApp = """
            ///usr/bin/env jbang "$0" "$@" ; exit $?
            //DEPS io.github.wfouche.tulip:tulip-runtime:__TULIP_VERSION__
            //DEPS io.rest-assured:rest-assured:5.5.0
            //SOURCES HttpUser.kt
            //JAVA 21
            
            import io.github.wfouche.tulip.api.TulipApi
            
            fun main(args: Array<String>) {
                TulipApi.runTulip("benchmark_config.jsonc")
            }
            """;

    private static String javaUser = """
            import io.github.wfouche.tulip.api.*;
            
            import org.springframework.web.client.RestClient;
            import org.springframework.web.client.RestClientException;
            
            public class HttpUser extends TulipUser {
            
                public HttpUser(int userId, int threadId) {
                    super(userId, threadId);
                }
            
                public boolean onStart() {
                    // Initialize the shared restClient instance only once
                    if (getUserId() == 0) {
                        restClient = RestClient.builder()
                                        .baseUrl(getUserParamValue("baseURI"))
                                        .build();
                    }
                    return true;
                }
            
                // Action 1: GET /posts/{id}
                public boolean action1() {
            
                    boolean rc = true;
                    try {
                        String response = restClient.get()
                          .uri("/posts/{id}", getUserId()+1)
                          .retrieve()
                          .body(String.class);
                        //System.out.println(response);
                    } catch (RestClientException e) {
                       rc = false;
                    }
                    return rc;       \s
                }
            
                // Action 2: GET /comments/{id}
                public boolean action2() {
            
                    boolean rc = true;
                    try {
                        String response = restClient.get()
                            .uri("/comments/{id}", getUserId()+1)
                            .retrieve()
                            .body(String.class);
                        //System.out.println(response);
                    } catch (RestClientException e) {
                        rc = false;
                    }
                    return rc;       \s
                }
            
                // Action 3: GET /todos/{id}
                public boolean action3() {
            
                    boolean rc = true;
                    try {
                        String response = restClient.get()
                            .uri("/todos/{id}", getUserId()+1)
                            .retrieve()
                            .body(String.class);
                        //System.out.println(response);
                    } catch (RestClientException e) {
                        rc = false;
                    }
                    return rc;       \s
                }
            
                public boolean onStop() {
                    return true;
                }
            
                // Action 1 support data and methods
                private static RestClient restClient;
            
            }
            """;

    private static String kotlinUser = """
            import io.github.wfouche.tulip.api.TulipUser
            import io.restassured.RestAssured.baseURI
            import io.restassured.RestAssured.given
            //import io.restassured.RestAssured.`when`
            import io.restassured.specification.RequestSpecification
            
            import java.net.URI
            import java.net.http.HttpClient
            import java.net.http.HttpRequest
            import java.net.http.HttpResponse
            
            fun RequestSpecification.When(): RequestSpecification {
                return this.`when`()
            }
            
            class HttpUser(userId: Int, threadId: Int) : TulipUser(userId, threadId) {
            
                init {
                    if (userId == 0) {
                        baseURI = getUserParamValue("baseURI")
                    }
                }
            
                override fun onStart(): Boolean {
                    return true
                }
            
                // Action 1: delay 10ms
                override fun action1(): Boolean {
                    Thread.sleep(10)
                    return true
                }
            
                // Action 2: delay 20ms
                override fun action2(): Boolean {
                    Thread.sleep(20)
                    return true
                }
            
                // Action 3: No-op
                override fun action3(): Boolean {
                    return true
                }
            
                // Action 4: GET /posts/{id} using Rest-Assured
                override fun action4(): Boolean {
                    return try {
                        given()
                            .When()
                                .get("/posts/${userId + 1}")
                            .then()
                                .statusCode(200)
                        true
                    } catch (e: AssertionError) {
                        false
                    }
                }
            
                // Action 5: GET /posts/{id} using java.net.http.HttpClient
                override fun action5(): Boolean {
                    return try {
                        val response = client.send(httpRequestPosts, HttpResponse.BodyHandlers.ofString())
                        response.statusCode() == 200
                    } catch (e: Exception) {
                        false
                    }
                }
            
                override fun onStop(): Boolean {
                    return true
                }
            
                // Action 5 support data and methods
                companion object {
                    private val client = HttpClient.newHttpClient()
                }
            
                private val httpRequestPosts = createHttpRequest("posts")
            
                private fun createHttpRequest(name: String): HttpRequest {
                    return try {
                        val id = userId + 1
                        val url = this.getUserParamValue("baseURI")
                        HttpRequest.newBuilder()
                            .uri(URI("$url/$name/$id"))
                            .GET()
                            .build()
                    } catch (e: Exception) {
                        throw RuntimeException(e)
                    }
                }
            }
            """;


    private static String runBenchShJava = """
            #!/bin/bash
            rm -f benchmark_report.html
            export JBANG_JAVA_OPTIONS="-server -Xmx1024m -XX:+UseZGC -XX:+ZGenerational"
            jbang run App.java
            echo ""
            w3m -dump -cols 205 benchmark_report.html
            #lynx -dump -width 205 benchmark_report.html
            #jbang run https://gist.github.com/wfouche/70738de122128bbc19ea888799151699 benchmark_config.adoc
            """;

    private static String runBenchCmdJava = """
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

    private static String runBenchShKotlin = """
            #!/bin/bash
            rm -f benchmark_report.html
            export JBANG_JAVA_OPTIONS="-server -Xmx1024m -XX:+UseZGC -XX:+ZGenerational"
            jbang run App.kt
            echo ""
            w3m -dump -cols 205 benchmark_report.html
            #lynx -dump -width 205 benchmark_report.html
            #jbang run https://gist.github.com/wfouche/70738de122128bbc19ea888799151699 benchmark_config.adoc
            """;

    private static String runBenchCmdKotlin = """
            del benchmark_report.html
            set JBANG_JAVA_OPTIONS=-server -Xmx1024m -XX:+UseZGC -XX:+ZGenerational
            call jbang run App.kt
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
        String lang = "Java";
        if (args.length > 0) {
            lang = args[0];
        }
        var list = new java.util.ArrayList<String>();
        list.add("Java");
        list.add("Kotlin");
        if (list.contains(lang)) {
            lang = lang;
        } else {
            lang = "Java";
        }

        System.out.println("Tulip: creating a " + lang + " benchmark with JBang support");

        if (lang.equals("Java")) {
            writeToFile("benchmark_config.jsonc", benchmarkConfig.stripLeading(), false);
            writeToFile("App.java", javaApp.stripLeading().replace("__TULIP_VERSION__", VERSION), false);
            writeToFile("HttpUser.java", javaUser.stripLeading(), false);
            writeToFile("run_bench.sh", runBenchShJava.stripLeading(), false);
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
            writeToFile("run_bench.cmd", runBenchCmdJava.stripLeading(), false);
        }

        if (lang.equals("Kotlin")) {
            writeToFile("benchmark_config.jsonc", benchmarkConfig.stripLeading(), false);
            writeToFile("App.kt", kotlinApp.stripLeading().replace("__TULIP_VERSION__", VERSION), false);
            writeToFile("HttpUser.kt", kotlinUser.stripLeading(), false);
            writeToFile("run_bench.sh", runBenchShKotlin.stripLeading(), false);
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
            writeToFile("run_bench.cmd", runBenchCmdKotlin.stripLeading(), false);
        }
    }
}