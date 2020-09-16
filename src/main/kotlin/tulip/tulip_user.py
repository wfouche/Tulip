NUM_ACTIONS=100

print("""/*-------------------------------------------------------------------------*/

package tulip

/*-------------------------------------------------------------------------*/
""")

print("val NUM_ACTIONS = %d"%(NUM_ACTIONS))

print("""
//
// Open base class for a Virtual User.
//
open class User(val userId: Int) {

    private val map = arrayOf(
        ::init,""")
        
for i in range(NUM_ACTIONS-2):
    print("        ::action%d,"%(i+1))
    
print("""        ::done
    )

    open fun init(): Boolean = false""")

for i in range(NUM_ACTIONS-2):
    print("    open fun action%d(): Boolean = false"%(i+1))

print("""    open fun done(): Boolean = false

    open fun processAction(actionId: Int): Boolean {
        try {
            return map[actionId].invoke()
        } catch (e: Exception) {
            return false
        }
    }

}

/*-------------------------------------------------------------------------*/""")