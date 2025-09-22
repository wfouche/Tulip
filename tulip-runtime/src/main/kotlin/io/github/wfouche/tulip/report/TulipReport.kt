package io.github.wfouche.tulip.report

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.zip.GZIPInputStream
import org.asciidoctor.Asciidoctor
import org.asciidoctor.Options
import org.asciidoctor.SafeMode
import org.python.core.PyObject
import org.python.util.PythonInterpreter

fun createHtmlReport(outputFilename: String, text1: String) {
    var text2: String = text1.trim()
    if (text2.startsWith("{")) {
        text2 = "{}"
    }
    val decodedBytes = Base64.getDecoder().decode(report_py.mainScriptTextBase64)
    val jythonCode = decompress(decodedBytes)
    PythonInterpreter().use { pyInterp ->
        pyInterp.exec("__name__=\"\"")
        pyInterp.exec(jythonCode)
        pyInterp.eval("createReport(\"${outputFilename}\",\"${text2}\")")
    }
}

fun createConfigReport(configFilename: String): String {
    val decodedBytes = Base64.getDecoder().decode(report2_py.mainScriptTextBase64)
    val jythonCode = decompress(decodedBytes)
    PythonInterpreter().use { pyInterp ->
        // Ensure that check for main in Jython script is not executed
        pyInterp.exec("__name__=\"\"")
        // Compile Jython code
        pyInterp.exec(jythonCode)
        // Run Jython function createReport()
        val result: PyObject = pyInterp.eval("createReport(\"${configFilename}\")")
        val adocFilename: String = result.__tojava__(String::class.java) as String
        return adocFilename
    }
}

fun convertAdocToHtml(adocFilename: String) {
    // println()
    // println("debug: begin adoc to html")
    //    if (!PlantUmlServer.running) {
    //        PlantUmlServer.start()
    //        Thread.sleep(1000)
    //    }
    val asciidoctor = Asciidoctor.Factory.create()
    asciidoctor.requireLibrary("asciidoctor-diagram")
    asciidoctor.convertFile(
        File(adocFilename), Options.builder().toFile(true).safe(SafeMode.UNSAFE).build())
    asciidoctor.shutdown()
    // println("debug: end adoc to html")
}

@Throws(IOException::class)
fun decompress(compressedData: ByteArray): String? {
    val bis = ByteArrayInputStream(compressedData)
    val gzip = GZIPInputStream(bis)
    val bos = ByteArrayOutputStream()
    val buffer = ByteArray(1024)
    var len: Int
    while ((gzip.read(buffer).also { len = it }) != -1) {
        bos.write(buffer, 0, len)
    }
    gzip.close()
    bis.close()
    bos.close()
    return bos.toString(StandardCharsets.UTF_8.name())
}
