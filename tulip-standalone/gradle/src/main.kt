/*-------------------------------------------------------------------------*/

import org.tulip.api.TulipUser
import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option

import org.tulip.api.TulipApi
import org.tulip.api.TulipUserFactory

/*-------------------------------------------------------------------------*/

class UserFactory: TulipUserFactory() {

    override fun getUser(userId: Int, className: String, threadId: Int): TulipUser {
        return when (className) {
            "user.http.HttpUser" -> HttpUser(userId, threadId)
            else -> throw Exception("Unknown user class name provided - $className")
        }
    }
}

/*-------------------------------------------------------------------------*/

class TulipCli : CliktCommand() {
    private val configOpt by option("--config").default("config.json")
    override fun run() {
        TulipApi.runTulip(configOpt, UserFactory())
    }
}

/*-------------------------------------------------------------------------*/

fun main(args: Array<String>) = TulipCli().main(args)

/*-------------------------------------------------------------------------*/
