#!/usr/bin/env kscript

val NUM_ACTIONS=100
var text = ""

text="package tulip"
println(text)
println("")
text="const val NUM_ACTIONS = $NUM_ACTIONS"
println(text)
println("")

text="""open class VirtualUser(val userId: Int) {

    private val map = arrayOf(
        ::start,"""
println(text)
for (i in 0..NUM_ACTIONS-3) {
    println("        ::action${i+1},")
}

text = """        ::stop
    )

    open fun start(): Boolean = false"""
println(text)

for (i in 0..NUM_ACTIONS-3) {
    println("    open fun action${i+1}(): Boolean = false")
}

val escaped_text = "userId: \${userId}, actionId: \${actionId}"

text = """    open fun stop(): Boolean = false

    open fun processAction(actionId: Int): Boolean {
        return try {
            map[actionId]()
        } catch (e: Exception) {
            Console.put("${escaped_text}, " + e.toString())
            false
        }
    }

}"""
println(text)