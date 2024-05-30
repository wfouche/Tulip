package tulip

/*-------------------------------------------------------------------------*/

import com.google.gson.annotations.SerializedName
import com.google.gson.GsonBuilder
import java.util.concurrent.TimeUnit

/*-------------------------------------------------------------------------*/

var g_config = BenchmarkConfig()

val g_contexts = mutableListOf<RuntimeContext>()

val g_tests = mutableListOf<TestProfile>()

/*-------------------------------------------------------------------------*/

data class ConfigContext(
    val name: String = "",
    val enabled: Boolean = false,
    @SerializedName("num_users") val numUsers: Int = 0,
    @SerializedName("num_threads") val numThreads: Int = 0
)

data class ConfigDuration(
    @SerializedName("startup_duration") val startupDuration: Long = 0,
    @SerializedName("warmup_duration") val warmupDuration: Long = 0,
    @SerializedName("main_duration") val mainDuration: Long = 0,
    @SerializedName("main_duration_repeat_count") val mainDurationRepeatCount: Int = 1
)

data class ConfigAction(
    val id: Int,
    val weight: Int = 0
)

data class ConfigTest(
    val name: String,
    val enabled: Boolean = false,
    val time: ConfigDuration,
    @SerializedName("throughput_rate") val throughputRate: Double = 0.0,
    @SerializedName("work_in_progress") val workInProgress: Int = 0,
    val actions: List<ConfigAction> = listOf()
)

data class BenchmarkConfig(
    @SerializedName("json_filename") val jsonFilename: String = "",
    @SerializedName("user_class") val userClass: String = "",
    @SerializedName("user_params") val userParams: Map<String,String> = mapOf(),
    @SerializedName("user_actions") val userActions: Map<Int,String> = mapOf(),
    val contexts: List<ConfigContext> = listOf(),
    val benchmarks: List<ConfigTest> = listOf()
)

fun initConfig(configFilename: String) {
    val gson = GsonBuilder().setPrettyPrinting().create()
    val sf = java.io.File(configFilename).readText()
    g_config = gson.fromJson(sf,BenchmarkConfig::class.java)
    if (false) {
        val json = gson.toJson(g_config)
        println("$json")
        println("${g_config}")
    }
    for (e:ConfigContext in g_config.contexts) {
        //println("${e.name}")
        if (e.enabled) {
            val v = RuntimeContext(e.name, e.numUsers, e.numThreads)
            g_contexts.add(v)
        }
    }
    for (e:ConfigTest in g_config.benchmarks) {
        //println("${e.name}")
        val v = TestProfile(
            enabled = e.enabled,
            name = e.name,
            duration = Duration(e.time.startupDuration, e.time.warmupDuration, e.time.mainDuration, e.time.mainDurationRepeatCount, TimeUnit.SECONDS),
            arrivalRate = e.throughputRate,
            queueLengths = listOf(e.workInProgress),
            actions = mutableListOf<Action>().apply {
                for (a: ConfigAction in e.actions) {
                    this.add(Action(a.id, a.weight))
                }
            },
            filename = g_config.jsonFilename,
        )
        g_tests.add(v)
    }
}

/*-------------------------------------------------------------------------*/