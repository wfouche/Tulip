NUM_ACTIONS=100

print("""/*-------------------------------------------------------------------------*/

package tulip

/*-------------------------------------------------------------------------*/
""")

print("const val NUM_ACTIONS = %d"%(NUM_ACTIONS))

print("""
//
// Open base class for a Virtual User.
//
open class VirtualUser(val userId: Int) {

    private val map = arrayOf(
        ::start,""")
        
for i in range(NUM_ACTIONS-2):
    print("        ::action%d,"%(i+1))
    
print("""        ::stop
    )

    open fun start(): Boolean = false""")

for i in range(NUM_ACTIONS-2):
    print("    open fun action%d(): Boolean = false"%(i+1))

print("""    open fun stop(): Boolean = false

    open fun processAction(actionId: Int): Boolean {
        return try {
            map[actionId]()
        } catch (e: Exception) {
            Console.put("userId: ${userId}, actionId: ${actionId}, " + e.toString())
            false
        }
    }

}

/*-------------------------------------------------------------------------*/""")
