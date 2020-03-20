print("""/*-------------------------------------------------------------------------*/

package tulip

/*-------------------------------------------------------------------------*/

//
// Open base class for a Virtual User.
//
open class User(val userId: Int) {

    private val map = arrayOf(
        ::init,""")
        
for i in range(99): 
    print("        ::action%02d,"%(i+1))
    
print("""        ::done
    )

    open fun init(): Boolean = false""")

for i in range(99): 
    print("    open fun action%02d(): Boolean = false"%(i+1))

print("""    open fun done(): Boolean = false

    open fun processAction(actionId: Int): Boolean {
        return map[actionId].invoke()
    }

}

/*-------------------------------------------------------------------------*/""")