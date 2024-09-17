import kotlin.test.Test
import kotlin.test.assertTrue

class TulipTest {
    @Test
    fun shouldSucceed() {
        val kotlinVersion = KotlinVersion.CURRENT
        val kotlinVersionString = "${KotlinVersion.CURRENT}"
        println("TulipTest: Kotlin version = ${kotlinVersionString}")
        assertTrue(kotlinVersion.isAtLeast(2,0,20))
    }
}