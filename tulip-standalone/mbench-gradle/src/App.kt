/*-------------------------------------------------------------------------*/

import io.github.wfouche.tulip.api.TulipUser
import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option

import io.github.wfouche.tulip.api.TulipApi
import io.github.wfouche.tulip.api.TulipUserFactory

/*-------------------------------------------------------------------------*/

//class UserFactory: TulipUserFactory() {
//
//    override fun getUser(userId: Int, className: String, threadId: Int): TulipUser {
//        return when (className) {
//            "user.http.HttpUser" -> HttpUser(userId, threadId)
//            else -> throw Exception("Unknown user class name provided - $className")
//        }
//    }
//}

/*-------------------------------------------------------------------------*/

class TulipCli : CliktCommand() {
    private val configOpt by option("--config").default("")
    private val resultOpt by option("--result").default("")
    override fun run() {
        if (configOpt != "") {
            TulipApi.runTulip(configOpt, TulipUserFactory())
        } else if (resultOpt != "") {
            echo(resultOpt)
            TulipApi.createHtmlReport(resultOpt)
        }
    }
}

/*-------------------------------------------------------------------------*/

fun main(args: Array<String>) = TulipCli().main(args)

/*-------------------------------------------------------------------------*/
