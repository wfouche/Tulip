/*-------------------------------------------------------------------------*/

package tulip

/*-------------------------------------------------------------------------*/
//
// Open base class for a Virtual User.
//
open class User(val userId: Int) {

    private val map = arrayOf(
        ::initialize,
        ::action1,
        ::action2,
        ::action3,
        ::action4,
        ::action5,
        ::action6,
        ::action7,
        ::action8,
        ::terminate
    )

    open fun initialize(): Boolean = false
    open fun action1(): Boolean = false
    open fun action2(): Boolean = false
    open fun action3(): Boolean = false
    open fun action4(): Boolean = false
    open fun action5(): Boolean = false
    open fun action6(): Boolean = false
    open fun action7(): Boolean = false
    open fun action8(): Boolean = false
    open fun terminate(): Boolean = false

    open fun processAction(actionId: Int): Boolean {
        return map[actionId].invoke()
    }

}

/*-------------------------------------------------------------------------*/
