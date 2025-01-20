///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.github.wfouche.tulip:tulip-runtime:2.1.4-dev
//DEPS org.springframework.boot:spring-boot-starter-web:3.4.1
//DEPS org.slf4j:slf4j-api:2.0.16
//DEPS ch.qos.logback:logback-core:1.5.16
//DEPS ch.qos.logback:logback-classic:1.5.16
//JAVA 21
//KOTLIN 2.1.0

import io.github.wfouche.tulip.api.TulipApi
import io.github.wfouche.tulip.api.TulipUser
import java.util.concurrent.ThreadLocalRandom
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val benchmarkConfig:String = """
{
    // Actions
    "actions": {
        "description": "Spring RestClient Benchmark [Kotlin]",
        "output_filename": "benchmark_output.json",
        "report_filename": "benchmark_report.html",
        "user_class": "HttpUser",
        "user_params": {
            //"baseURI": "http://localhost:7070",
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
            "aps_rate": 10000.0,
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
            "aps_rate": 10000.0,
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
            "aps_rate": 10000.0,
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
""".trim()

class HttpUser(userId: Int, threadId: Int) : TulipUser(userId, threadId) {

    // Action 0
    override fun onStart(): Boolean {
        // Initialize the shared RestClient object only once
        if (userId == 0) {
            logger.info("Kotlin")
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

fun main(args: Array<String>) {
    TulipApi.runTulip(benchmarkConfig)
}
