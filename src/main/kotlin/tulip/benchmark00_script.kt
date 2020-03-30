/*-------------------------------------------------------------------------*/

package tulip

/*-------------------------------------------------------------------------*/

val NUM_USERS = 40
val NUM_THREADS = 4

val JSON_FILENAME="json_results.txt"

/*-------------------------------------------------------------------------*/

var testSuite = mutableListOf<TestCase>()

/*-------------------------------------------------------------------------*/

fun initTestSuite() {

    // Test 1.
    testSuite.add(
        TestCase(
            name = "Test0 (Initialize)",
            arrivalRate = 5.0,
            userProfile = listOf(1),
            actions = listOf(Action(0), Action(7)),
            filename = JSON_FILENAME
        )
    )

    // Test 2.
    testSuite.add(
        TestCase(
            // The name of this test.
            name = "Test1 (Throughput Test - Max)",

            // Duration in minutes
            warmDurationMinutes = 1,
            rampDurationMinutes = 1,
            mainDurationMinutes = 1,

            // Limit throughput 100.0 actions per second (on average).
            // A value of zero indicates that the arrival rate is uncapped.
            // λ value from Little's Law
            arrivalRate = 0.0,

            // Limit the number of active user objects, A value of
            // zero sets the number of active users to unlimited.
            // L value from Little's Law.
            userProfile = listOf(0, 20, 10),

            // Actions to be performed on the user objects during this test.
            actions = listOf(Action(8)),

            repeatCount = 3,

            filename = JSON_FILENAME
        )
    )

    // Test 3.
    testSuite.add(
        TestCase(
            // The name of this test.
            name = "Test2 (Throughput Test - Fixed)",

            // Duration in minutes
            rampDurationMinutes = 1,
            mainDurationMinutes = 1,

            // Limit throughput 100.0 actions per second (on average).
            // A value of zero indicates that the arrival rate is uncapped.
            // λ value from Little's Law
            arrivalRate = 100.0,

            // Limit the number of active user objects, A value of
            // zero sets the number of active users to unlimited.
            // L value from Little's Law.
            userProfile = listOf(0),

            // Actions to be performed on the user objects during this test.
            // 100 actions in total with a 50%/50% split between
            // action 0 and action 1
            // 6*1.0/4.0 + 14*3.0/4.0 = 12.0 ms (expected global average response time)
            actions = listOf(Action(1, 25), Action(2, 75)),

            repeatCount = 3,

            filename = JSON_FILENAME
        )
    )

    // Test 4.
    testSuite.add(
        TestCase(
            // The name of this test.
            name = "Test3 (Workflow Test)",

            // Duration in minutes
            rampDurationMinutes = 0,
            mainDurationMinutes = 1,

            // Limit throughput 60.0 actions per second (on average).
            // A value of zero indicates that the arrival rate is uncapped.
            arrivalRate = 0.25,

            // Limit the number of active user objects, A value of
            // zero sets the number of active users to unlimited.
            userProfile = listOf(NUM_THREADS),

            // Actions to be performed on the user objects during this test.
            // 100 actions in total with a 50%/50% split between
            // action 0 and action 1
            // 6*1.0/4.0 + 14*3.0/4.0 = 12.0 ms (expected global average response time)
            actions = listOf(Action(4), Action(5), Action(6)),

            filename = JSON_FILENAME
        )
    )

    // Test 5.
    testSuite.add(
        TestCase(
            name = "Test4 (Terminate)",
            arrivalRate = 5.0,
            userProfile = listOf(1),
            actions = listOf(Action(9)),
            filename = JSON_FILENAME
        )
    )
}

/*-------------------------------------------------------------------------*/

fun newUser(userId: Int): User {
    return UserHttp(userId)
}

/*-------------------------------------------------------------------------*/
