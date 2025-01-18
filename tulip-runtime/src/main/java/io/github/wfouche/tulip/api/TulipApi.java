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
    public static final String VERSION = "2.1.3-dev";

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
                    "description": "Spring RestClient Benchmark [__TULIP_LANG__]",
                    "output_filename": "benchmark_output.json",
                    "report_filename": "benchmark_report.html",
                    "user_class": "HttpUser",
                    "user_params": {
                        "baseURI": "https://jsonplaceholder.typicode.com",
                        "connectTimeoutMillis": 500,
                        "readTimeoutMillis": 2000,
                        "debug": false
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
                        "throughput_rate": 10.0,
                        "scenario_actions": [
                            {
                                "id": 1
                            }
                        ],
                        "time": {
                            "pre_warmup_duration": 30,
                            "warmup_duration": 10,
                            "benchmark_duration": 30,
                            "benchmark_repeat_count": 3
                        }
                    },
                    "REST2": {
                        "enabled": true,
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
                        ],
                        "time": {
                            "pre_warmup_duration": 30,
                            "warmup_duration": 10,
                            "benchmark_duration": 30,
                            "benchmark_repeat_count": 3
                        }
                    },
                    "REST3": {
                        "enabled": true,
                        "throughput_rate": 10.0,
                        "scenario_workflow": "api-user",
                        "time": {
                            "pre_warmup_duration": 30,
                            "warmup_duration": 10,
                            "benchmark_duration": 30,
                            "benchmark_repeat_count": 3
                        }
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
                        "num_users": 128,
                        "num_threads": 2
                    }
                }
            }
            """;

    private static String javaApp = """
            ///usr/bin/env jbang "$0" "$@" ; exit $?
            //DEPS io.github.wfouche.tulip:tulip-runtime:__TULIP_VERSION__
            //DEPS org.springframework.boot:spring-boot-starter-web:3.4.1
            //DEPS org.slf4j:slf4j-api:2.0.16
            //DEPS ch.qos.logback:logback-core:1.5.16
            //DEPS ch.qos.logback:logback-classic:1.5.16
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
            //DEPS org.springframework.boot:spring-boot-starter-web:3.4.1
            //DEPS org.slf4j:slf4j-api:2.0.16
            //DEPS ch.qos.logback:logback-core:1.5.16
            //DEPS ch.qos.logback:logback-classic:1.5.16
            //SOURCES HttpUser.kt
            //JAVA 21
            //KOTLIN 2.0.21
            
            import io.github.wfouche.tulip.api.TulipApi
            
            fun main(args: Array<String>) {
                TulipApi.runTulip("benchmark_config.jsonc")
            }
            """;

    private static String groovyApp = """
            ///usr/bin/env jbang "$0" "$@" ; exit $?
            //DEPS io.github.wfouche.tulip:tulip-runtime:__TULIP_VERSION__
            //DEPS org.springframework.boot:spring-boot-starter-web:3.4.1
            //DEPS org.slf4j:slf4j-api:2.0.16
            //DEPS ch.qos.logback:logback-core:1.5.16
            //DEPS ch.qos.logback:logback-classic:1.5.16
            //SOURCES HttpUser.groovy
            //JAVA 21
            //GROOVY 4.0.24
            
            import io.github.wfouche.tulip.api.TulipApi
            
            class App {
                static void main(String[] args) {
                    TulipApi.runTulip("benchmark_config.jsonc")
                }
            }
            """;

    private static String scalaApp = """
            //> using jvm 21
            //> using dep io.github.wfouche.tulip:tulip-runtime:__TULIP_VERSION__
            //> using dep org.springframework.boot:spring-boot-starter-web:3.4.1
            //> using dep org.slf4j:slf4j-api:2.0.16
            //> using dep ch.qos.logback:logback-core:1.5.16
            //> using dep ch.qos.logback:logback-classic:1.5.16
            //> using javaOpt -server, -Xmx1024m, -XX:+UseZGC, -XX:+ZGenerational
            //> using repositories m2local
            
            // https://yadukrishnan.live/developing-java-applications-with-scala-cli
            // https://www.baeldung.com/scala/scala-cli-intro
            
            import io.github.wfouche.tulip.api.TulipApi
            
            object App {
              def main(args: Array[String]): Unit = {
                TulipApi.runTulip("benchmark_config.jsonc")
              }
            }
            """;

    private static String javaUser = """
            import io.github.wfouche.tulip.api.*;
            import java.util.concurrent.ThreadLocalRandom;
            import org.springframework.web.client.RestClient;
            import org.springframework.web.client.RestClientException;
            import org.springframework.http.client.SimpleClientHttpRequestFactory;
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;
            
            public class HttpUser extends TulipUser {
            
                public HttpUser(int userId, int threadId) {
                    super(userId, threadId);
                }
            
                public boolean onStart() {
                    // Initialize the shared RestClient object only once
                    if (getUserId() == 0) {
                        logger.info("Initializing static data");
                        var connectTimeout = Integer.valueOf(getUserParamValue("connectTimeoutMillis"));
                        var readTimeout = Integer.valueOf(getUserParamValue("readTimeoutMillis"));
                        var factory = new SimpleClientHttpRequestFactory();
                        factory.setConnectTimeout(connectTimeout);
                        factory.setReadTimeout(readTimeout);
                        restClient = RestClient.builder()
                            .requestFactory(factory)
                            .baseUrl(getUserParamValue("baseURI"))
                            .build();
                        debug = Boolean.valueOf(getUserParamValue("debug"));
                        logger.info("debug = " + debug);
                    }
                    return true;
                }
            
                // Action 1: GET /posts/{id}
                public boolean action1() {
                    boolean rc;
                    try {
                        int id = debug ? 1 : ThreadLocalRandom.current().nextInt(100)+1;
                        String rsp = restClient.get()
                          .uri("/posts/{id}", id)
                          .retrieve()
                          .body(String.class);
                        rc = (rsp != null && rsp.length() > 2);
                    } catch (RestClientException e) {
                       rc = false;
                    }
                    return rc;
                }
            
                // Action 2: GET /comments/{id}
                public boolean action2() {
                    boolean rc;
                    try {
                        int id = debug ? 1 : ThreadLocalRandom.current().nextInt(500)+1;
                        String rsp = restClient.get()
                            .uri("/comments/{id}", id)
                            .retrieve()
                            .body(String.class);
                        rc = (rsp != null && rsp.length() > 2);
                    } catch (RestClientException e) {
                        rc = false;
                    }
                    return rc;
                }
            
                // Action 3: GET /todos/{id}
                public boolean action3() {
                    boolean rc;
                    try {
                        int id = debug ? 1 : ThreadLocalRandom.current().nextInt(200)+1;
                        String rsp = restClient.get()
                            .uri("/todos/{id}", id)
                            .retrieve()
                            .body(String.class);
                        rc = (rsp != null && rsp.length() > 2);
                    } catch (RestClientException e) {
                        rc = false;
                    }
                    return rc;
                }
            
                // Action 99
                public boolean onStop() {
                    return true;
                }
            
                // RestClient object
                private static RestClient restClient;
            
                // Debug flag
                private static boolean debug = false;
            
                /// Logger
                private static final Logger logger = LoggerFactory.getLogger(HttpUser.class);
            
            }
            """;

    private static String kotlinUser = """
            import io.github.wfouche.tulip.api.*
            import java.util.concurrent.ThreadLocalRandom
            import org.springframework.web.client.RestClient
            import org.springframework.web.client.RestClientException
            import org.springframework.http.client.SimpleClientHttpRequestFactory
            import org.slf4j.Logger
            import org.slf4j.LoggerFactory
            
            class HttpUser(userId: Int, threadId: Int) : TulipUser(userId, threadId) {
            
                // Action 0
                override fun onStart(): Boolean {
                    // Initialize the shared RestClient object only once
                    if (userId == 0) {
                        logger.info("Initializing static data")
                        val connectTimeout = getUserParamValue("connectTimeoutMillis").toInt()
                        val readTimeout = getUserParamValue("readTimeoutMillis").toInt()
                        val factory = SimpleClientHttpRequestFactory().apply {
                            setConnectTimeout(connectTimeout)
                            setReadTimeout(readTimeout)
                        }
                        restClient = RestClient.builder()
                            .requestFactory(factory)
                            .baseUrl(getUserParamValue("baseURI"))
                            .build()
                        debug = getUserParamValue("debug").toBoolean()
                        logger.info("debug = " + debug)
                    }
                    return true
                }
            
                // Action 1: GET /posts/{id}
                override fun action1(): Boolean {
                    val id: Int = if (debug) 1 else ThreadLocalRandom.current().nextInt(100)+1
                    return try {
                        val rsp: String? = restClient.get()
                            .uri("/posts/${id}")
                            .retrieve()
                            .body(String::class.java)
                        //Postcondition
                        (rsp != null && rsp.length > 2)
                    } catch (e: RestClientException) {
                        false
                    }
                }
            
                // Action 2: GET /comments/{id}
                override fun action2(): Boolean {
                    val id: Int = if (debug) 1 else ThreadLocalRandom.current().nextInt(500)+1
                    return try {
                        val rsp: String? = restClient.get()
                            .uri("/comments/${id}")
                            .retrieve()
                            .body(String::class.java)
                        //Postcondition
                        (rsp != null && rsp.length > 2)
                    } catch (e: RestClientException) {
                        false
                    }
                }
            
                // Action 3: GET /todos/{id}
                override fun action3(): Boolean {
                    val id: Int = if (debug) 1 else ThreadLocalRandom.current().nextInt(200)+1
                    return try {
                        val rsp: String? = restClient.get()
                            .uri("/todos/${id}")
                            .retrieve()
                            .body(String::class.java)
                        //Postcondition
                        (rsp != null && rsp.length > 2)
                    } catch (e: RestClientException) {
                        false
                    }
                }
            
                // Action 99
                override fun onStop(): Boolean {
                    return true
                }
            
                // RestClient object
                companion object {
                    private lateinit var restClient: RestClient
                    private var debug: Boolean = false
                    private val logger = LoggerFactory.getLogger(HttpUser::class.java)
                }
            }
            """;

    private static String groovyUser = """
            import io.github.wfouche.tulip.api.*
            import org.springframework.web.client.RestClient
            import org.springframework.web.client.RestClientException
            import org.springframework.http.client.SimpleClientHttpRequestFactory
            import java.util.concurrent.ThreadLocalRandom
            import org.slf4j.Logger
            import org.slf4j.LoggerFactory
            
            class HttpUser extends TulipUser {
            
                HttpUser(int userId, int threadId) {
                    super(userId, threadId)
                }
            
                boolean onStart() {
                    // Initialize the shared RestClient object only once
                    if (userId == 0) {
                        logger.info("Initializing static data")
                        def connectTimeout = getUserParamValue("connectTimeoutMillis") as Integer
                        def readTimeout = getUserParamValue("readTimeoutMillis") as Integer
                        def factory = new SimpleClientHttpRequestFactory()
                        factory.setConnectTimeout(connectTimeout)
                        factory.setReadTimeout(readTimeout)
                        restClient = RestClient.builder()
                            .requestFactory(factory)
                            .baseUrl(getUserParamValue("baseURI"))
                            .build()
                        def debug = Boolean.valueOf(getUserParamValue("debug"))
                        logger.info("debug = " + debug)
                    }
                    return true
                }
            
                // Action 1: GET /posts/{id}
                boolean action1() {
                    boolean rc
                    try {
                        int id = debug ? 1 : ThreadLocalRandom.current().nextInt(100) + 1
                        String rsp = restClient.get()
                            .uri("/posts/${id}")
                            .retrieve()
                            .body(String.class)
                        rc = (rsp != null && rsp.length() > 2)
                    } catch (RestClientException e) {
                        rc = false
                    }
                    return rc
                }
            
                // Action 2: GET /comments/{id}
                boolean action2() {
                    boolean rc
                    try {
                        int id = debug ? 1 : ThreadLocalRandom.current().nextInt(500) + 1
                        String rsp = restClient.get()
                            .uri("/comments/${id}")
                            .retrieve()
                            .body(String.class)
                        rc = (rsp != null && rsp.length() > 2)
                    } catch (RestClientException e) {
                        rc = false
                    }
                    return rc
                }
            
                // Action 3: GET /todos/{id}
                boolean action3() {
                    boolean rc
                    try {
                        int id = debug ? 1 : ThreadLocalRandom.current().nextInt(200) + 1
                        String rsp = restClient.get()
                            .uri("/todos/${id}")
                            .retrieve()
                            .body(String.class)
                        rc = (rsp != null && rsp.length() > 2)
                    } catch (RestClientException e) {
                        rc = false
                    }
                    return rc
                }
            
                // Action 99
                boolean onStop() {
                    return true
                }
            
                // RestClient object
                static RestClient restClient
            
                // Debug flag
                static boolean debug = false
            
                // Logger
                static Logger logger = LoggerFactory.getLogger(HttpUser.class)
            
            }
            """;

    private static String scalaUser = """
            import io.github.wfouche.tulip.api.TulipUser
            import io.github.wfouche.tulip.api.TulipConsole

            class HttpUser(val userId: Int, val threadId: Int) extends TulipUser(userId, threadId) {

              override def onStart(): Boolean = {
                TulipConsole.put("ScalaDemoUser " + userId)
                true
              }

              override def action1(): Boolean = {
                Thread.sleep(10)
                true
              }

              override def action2(): Boolean = {
                Thread.sleep(20)
                true
              }

              override def action3(): Boolean = true

              override def onStop(): Boolean = true

            }
            """;

    private static String runBenchShJava = """
            #!/bin/bash
            # jbang io.github.wfouche.tulip:tulip-runtime:__TULIP_VERSION__ Java
            rm -f benchmark_report.html
            export JBANG_JAVA_OPTIONS="-server -Xmx1024m -XX:+UseZGC -XX:+ZGenerational"
            jbang run App.java
            echo ""
            #w3m -dump -cols 205 benchmark_report.html
            lynx -dump -width 205 benchmark_report.html
            #jbang run https://gist.github.com/wfouche/70738de122128bbc19ea888799151699 benchmark_config.adoc
            """;

    private static String runBenchCmdJava = """
            REM jbang io.github.wfouche.tulip:tulip-runtime:__TULIP_VERSION__ Java
            del benchmark_report.html
            set JBANG_JAVA_OPTIONS=-server -Xmx1024m -XX:+UseZGC -XX:+ZGenerational
            call jbang run App.java
            @echo off
            echo.
            REM w3m.exe -dump -cols 205 benchmark_report.html
            REM lynx.exe -dump -width 205 benchmark_report.html
            start benchmark_report.html
            REM jbang run https://gist.github.com/wfouche/70738de122128bbc19ea888799151699 benchmark_config.adoc
            REM start benchmark_config.html
            """;

    private static String runBenchShKotlin = """
            #!/bin/bash
            # jbang io.github.wfouche.tulip:tulip-runtime:__TULIP_VERSION__ Kotlin
            rm -f benchmark_report.html
            export JBANG_JAVA_OPTIONS="-server -Xmx1024m -XX:+UseZGC -XX:+ZGenerational"
            jbang run App.kt
            echo ""
            #w3m -dump -cols 205 benchmark_report.html
            lynx -dump -width 205 benchmark_report.html
            #jbang run https://gist.github.com/wfouche/70738de122128bbc19ea888799151699 benchmark_config.adoc
            """;

    private static String runBenchCmdKotlin = """
            REM jbang io.github.wfouche.tulip:tulip-runtime:__TULIP_VERSION__ Kotlin
            del benchmark_report.html
            set JBANG_JAVA_OPTIONS=-server -Xmx1024m -XX:+UseZGC -XX:+ZGenerational
            call jbang run App.kt
            @echo off
            echo.
            REM call w3m.exe -dump -cols 205 benchmark_report.html
            REM lynx.exe -dump -width 205 benchmark_report.html
            start benchmark_report.html
            REM jbang run https://gist.github.com/wfouche/70738de122128bbc19ea888799151699 benchmark_config.adoc
            REM start benchmark_config.html
            """;

    private static String runBenchShGroovy = """
            #!/bin/bash
            # jbang io.github.wfouche.tulip:tulip-runtime:__TULIP_VERSION__ Groovy
            rm -f benchmark_report.html
            export JBANG_JAVA_OPTIONS="-server -Xmx1024m -XX:+UseZGC -XX:+ZGenerational"
            jbang run App.groovy
            echo ""
            #w3m -dump -cols 205 benchmark_report.html
            lynx -dump -width 205 benchmark_report.html
            #jbang run https://gist.github.com/wfouche/70738de122128bbc19ea888799151699 benchmark_config.adoc
            """;

    private static String runBenchCmdGroovy = """
            REM jbang io.github.wfouche.tulip:tulip-runtime:__TULIP_VERSION__ Groovy
            del benchmark_report.html
            set JBANG_JAVA_OPTIONS=-server -Xmx1024m -XX:+UseZGC -XX:+ZGenerational
            call jbang run App.groovy
            @echo off
            echo.
            REM call w3m.exe -dump -cols 205 benchmark_report.html
            REM lynx.exe -dump -width 205 benchmark_report.html
            start benchmark_report.html
            REM jbang run https://gist.github.com/wfouche/70738de122128bbc19ea888799151699 benchmark_config.adoc
            REM start benchmark_config.html
            """;

    private static String runBenchShScala = """
            #!/bin/bash
            # jbang io.github.wfouche.tulip:tulip-runtime:__TULIP_VERSION__ Scala
            rm -f benchmark_report.html
            scala-cli App.scala HttpUser.scala
            echo ""
            #w3m -dump -cols 205 benchmark_report.html
            lynx -dump -width 205 benchmark_report.html
            #jbang run https://gist.github.com/wfouche/70738de122128bbc19ea888799151699 benchmark_config.adoc
            """;

    private static String runBenchCmdScala = """
            REM jbang io.github.wfouche.tulip:tulip-runtime:__TULIP_VERSION__ Scala
            del benchmark_report.html
            scala-cli App.scala HttpUser.scala
            @echo off
            echo.
            REM call w3m.exe -dump -cols 205 benchmark_report.html
            REM lynx.exe -dump -width 205 benchmark_report.html
            start benchmark_report.html
            REM jbang run https://gist.github.com/wfouche/70738de122128bbc19ea888799151699 benchmark_config.adoc
            REM start benchmark_config.html
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
        list.add("Groovy");
        list.add("Scala");
        if (list.contains(lang)) {
            lang = lang;
        } else {
            lang = "Java";
        }

        if (lang.equals("Scala")) {
            System.out.println("Tulip: creating a " + lang + " benchmark with Scala-CLI support");
        } else {
            System.out.println("Tulip: creating a " + lang + " benchmark with JBang support");
        }

        if (lang.equals("Java")) {
            writeToFile("benchmark_config.jsonc", benchmarkConfig.stripLeading().replace("__TULIP_LANG__", lang), false);
            writeToFile("App.java", javaApp.stripLeading().replace("__TULIP_VERSION__", VERSION), false);
            writeToFile("HttpUser.java", javaUser.stripLeading(), false);
            writeToFile("run_bench.sh", runBenchShJava.stripLeading().replace("__TULIP_VERSION__", VERSION), false);
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
            writeToFile("run_bench.cmd", runBenchCmdJava.stripLeading().replace("__TULIP_VERSION__", VERSION), false);
        }

        if (lang.equals("Kotlin")) {
            writeToFile("benchmark_config.jsonc", benchmarkConfig.stripLeading().replace("__TULIP_LANG__", lang), false);
            writeToFile("App.kt", kotlinApp.stripLeading().replace("__TULIP_VERSION__", VERSION), false);
            writeToFile("HttpUser.kt", kotlinUser.stripLeading(), false);
            writeToFile("run_bench.sh", runBenchShKotlin.stripLeading().replace("__TULIP_VERSION__", VERSION), false);
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
            writeToFile("run_bench.cmd", runBenchCmdKotlin.stripLeading().replace("__TULIP_VERSION__", VERSION), false);
        }

        if (lang.equals("Groovy")) {
            writeToFile("benchmark_config.jsonc", benchmarkConfig.stripLeading().replace("__TULIP_LANG__", lang), false);
            writeToFile("App.groovy", groovyApp.stripLeading().replace("__TULIP_VERSION__", VERSION), false);
            writeToFile("HttpUser.groovy", groovyUser.stripLeading(), false);
            writeToFile("run_bench.sh", runBenchShGroovy.stripLeading().replace("__TULIP_VERSION__", VERSION), false);
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
            writeToFile("run_bench.cmd", runBenchCmdGroovy.stripLeading().replace("__TULIP_VERSION__", VERSION), false);
        }

        if (lang.equals("Scala")) {
            writeToFile("benchmark_config.jsonc", benchmarkConfig.stripLeading().replace("__TULIP_LANG__", lang), false);
            writeToFile("App.scala", scalaApp.stripLeading().replace("__TULIP_VERSION__", VERSION), false);
            writeToFile("HttpUser.scala", scalaUser.stripLeading(), false);
            writeToFile("run_bench.sh", runBenchShScala.stripLeading().replace("__TULIP_VERSION__", VERSION), false);
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
            writeToFile("run_bench.cmd", runBenchCmdScala.stripLeading().replace("__TULIP_VERSION__", VERSION), false);
        }

    }
}