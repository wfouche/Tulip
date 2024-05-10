package tulip

/*-------------------------------------------------------------------------*/

import com.google.gson.annotations.SerializedName
import com.google.gson.GsonBuilder
import java.util.concurrent.TimeUnit

/*-------------------------------------------------------------------------*/

val g_contexts: MutableList<RuntimeContext> = mutableListOf()

val g_tests: MutableList<TestProfile> = mutableListOf()

/*-------------------------------------------------------------------------*/

data class ConfigContext(
    val name: String = "",
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
    val enabled: Boolean = false,
    val name: String,
    val time: ConfigDuration,
    @SerializedName("throughput_rate") val throughputRate: Double = 0.0,
    @SerializedName("work_in_progress") val workInProgress: Int = 0,
    val actions: List<ConfigAction> = listOf()
)

data class BenchmarkConfig(
    @SerializedName("json_filename") val jsonFilename: String,
    val contexts: List<ConfigContext> = listOf(),
    val benchmarks: List<ConfigTest> = listOf()
)

fun initConfig() {
    val gson = GsonBuilder().setPrettyPrinting().create()
    val sf = java.io.File("config.json").readText()
    val config = gson.fromJson(sf,BenchmarkConfig::class.java)
    //val json = gson.toJson(config)
    //println("$json")
    //println("${config}")
    for (e:ConfigContext in config.contexts) {
        //println("${e.name}")
        val v = RuntimeContext(e.name, e.numUsers, e.numThreads)
        g_contexts.add(v)
    }
    for (e:ConfigTest in config.benchmarks) {
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
            filename = config.jsonFilename,
        )
        g_tests.add(v)
    }
}

/*-------------------------------------------------------------------------*/