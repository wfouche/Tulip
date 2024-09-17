import kotlin.test.Test
import kotlin.test.assertTrue

class TulipTest {
    @Test
    fun shouldSucceed() {
        val version = "${KotlinVersion.CURRENT}"
        println("TulipTest: Kotlin version = ${version}")
        assertTrue(version == "2.0.20")
    }
}