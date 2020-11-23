import tulip.Action
import tulip.Duration
import tulip.NUM_ACTIONS
import tulip.RuntimeContext
import tulip.TestProfile
import tulip.User

const val JSON_FILENAME = "json_results.txt"

val contexts: List<RuntimeContext> = listOf(
        // Context 1
        RuntimeContext("Scenario-1", 4, 4),

        // Context 2
        RuntimeContext("Scenario-2", 8, 8)
)

val tests: List<TestProfile> = listOf(

        // 0
        TestProfile(
                name = "Test0 (Initialize)",
                arrivalRate = 0.0,
                queueLenghts = listOf(1),
                actions = listOf(Action(0)),
                filename = JSON_FILENAME
        ),

        // 1
        TestProfile(
                name = "Test1 (Throughput Test - Max)",
                duration = Duration(1,1,5,3)
                arrivalRate = 0.0,
                queueLenghts = listOf(-1),
                actions = listOf(Action(1)),
                filename = JSON_FILENAME
        ),

        // 2
        TestProfile(
                name = "Test2 (Terminate)",
                arrivalRate = 0.0,
                queueLenghts = listOf(1),
                actions = listOf(Action(NUM_ACTIONS - 1)),
                filename = JSON_FILENAME
        )
)
