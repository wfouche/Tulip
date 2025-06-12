package io.github.wfouche.tulip.report

import org.asciidoctor.Asciidoctor
import org.asciidoctor.Options
import org.asciidoctor.SafeMode
import org.python.util.PythonInterpreter
import java.io.File
import java.util.*

fun createHtmlReport(outputFilename: String, text1: String) {
    var text2: String = text1.trim()
    if (text2.startsWith("{")) {
        text2 = "{}"
    }

    val decodedBytes = Base64.getDecoder().decode(report_py.mainScriptTextBase64)
    val jythonCode = String(decodedBytes)

    PythonInterpreter().use { pyInterp ->
        pyInterp.exec("__name__=\"\"")
        pyInterp.exec(jythonCode)
        pyInterp.eval("createReport(\"${outputFilename}\",\"${text2}\")")
    }
}

fun createConfigReport(configFilename: String) {
    val decodedBytes = Base64.getDecoder().decode(report2_py.mainScriptTextBase64)
    val jythonCode = String(decodedBytes)
    PythonInterpreter().use { pyInterp ->
        // Ensure that check for main in Jython script is not executed
        pyInterp.exec("__name__=\"\"")
        // Compile Jython code
        pyInterp.exec(jythonCode)
        // Run Jython function createReport()
        pyInterp.eval("createReport(\"${configFilename}\")")
    }
}

fun convertAdocToHtml(adocFilename: String) {
    val asciidoctor = Asciidoctor.Factory.create()
    asciidoctor.requireLibrary("asciidoctor-diagram")
    asciidoctor.convertFile(
        File(adocFilename),
        Options.builder()
            .toFile(true)
            .safe(SafeMode.UNSAFE)
            .build()
    )
}
