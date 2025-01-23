///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.github.ajalt.clikt:clikt-jvm:5.0.1
//DEPS io.github.wfouche.tulip:tulip-runtime:2.1.4
//DEPS org.springframework.boot:spring-boot-starter-web:3.4.1
//DEPS org.slf4j:slf4j-api:2.0.16
//DEPS ch.qos.logback:logback-core:1.5.16
//DEPS ch.qos.logback:logback-classic:1.5.16
//JAVA 21
//KOTLIN 2.0.21

import io.github.wfouche.tulip.api.TulipApi
import io.github.wfouche.tulip.api.TulipUser
import java.util.concurrent.ThreadLocalRandom
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option

val benchmarkConfig:String = """
{
    // Actions
    "actions": {
        "description": "kwrk",
        "output_filename": "benchmark_output.json",
        "report_filename": "benchmark_report.html",
        "user_class": "HttpUser",
        "user_params": {
            "baseURI": "__P_URL__",
            "connectTimeoutMillis": 500,
            "readTimeoutMillis": 2000,
            "debug": false
        },
        "user_actions": {
            "0": "onStart",  // Init
            "1": "GET:url",
            "99": "onStop"   // Shutdown
        }
    },
    // Benchmarks
    "benchmarks": {
        "onStart": {
            "save_stats": false,
            "scenario_actions": [ {"id": 0} ]
        },
         "HTTP": {
            "enabled": true,
            "aps_rate": __P_RATE__,
            "scenario_actions": [
                {
                    "id": 1
                }
            ],
            "time": {
                "pre_warmup_duration": 10,
                "warmup_duration": 5,
                "benchmark_duration": __P_DURATION__,
                "benchmark_repeat_count": __P_REPEAT__
            }
        },
        "onStop": {
            "save_stats": false,
            "scenario_actions": [ {"id": 99} ]
        }
    },
    // Contexts
    "contexts": {
        "Context-1": {
            "enabled": true,
            "num_users": __P_THREADS__,
            "num_threads": __P_THREADS__
        }
    }
}
""".trim()

class HttpUser(userId: Int, threadId: Int) : TulipUser(userId, threadId) {

    // Action 0
    override fun onStart(): Boolean {
        // Initialize the shared RestClient object only once
        if (userId == 0) {
            //logger.info(getUserParamValue("baseURI"))
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
            //debug = getUserParamValue("debug").toBoolean()
            //logger.info("debug = " + debug)
        }
        return true
    }

    // Action 1: GET ${url}
    override fun action1(): Boolean {
        return try {
            val rsp: String? = restClient.get()
                .uri("")
                .retrieve()
                .body(String::class.java)
            //Postcondition
            (rsp != null && rsp.length > 0)
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
        private val logger = LoggerFactory.getLogger(HttpUser::class.java)
    }
}

class KwrkCli : CliktCommand() {
    private val p_rate by option("--rate").default("5.0")
    private val p_threads by option("--threads").default("2")
    private val p_duration by option("--duration").default("30")
    private val p_repeat by option("--repeat").default("3")
    private val p_url by option("--url").default("http://localhost:7070")
    override fun run() {
        var jsonc = benchmarkConfig

        jsonc = jsonc.replace("__P_RATE__", p_rate)
        jsonc = jsonc.replace("__P_THREADS__", p_threads)
        jsonc = jsonc.replace("__P_DURATION__", p_duration)
        jsonc = jsonc.replace("__P_REPEAT__", p_repeat)
        jsonc = jsonc.replace("__P_URL__", p_url)

        println("kwrk arguments:")
        println("  --rate ${p_rate}")
        println("  --threads ${p_threads}")
        println("  --duration ${p_duration}")
        println("  --repeat ${p_repeat}")
        println("  --url ${p_url}")

        TulipApi.runTulip(jsonc)
    }
}

fun main(args: Array<String>) = KwrkCli().main(args)