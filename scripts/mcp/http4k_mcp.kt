#!/usr/bin/env jbang

//DEPS org.http4k:http4k-ai-mcp-sdk:6.16.0.0
//DEPS org.http4k:http4k-format-jackson:6.16.0.0
//DEPS org.slf4j:slf4j-simple:2.0.12

import org.http4k.ai.mcp.McpServer
import org.http4k.ai.mcp.ProtocolVersion.Companion.V2024_11_05
import org.http4k.ai.mcp.Tool
import org.http4k.ai.mcp.ToolHandler
import org.http4k.ai.mcp.model.Implementation
import org.http4k.ai.mcp.model.McpResult
import org.http4k.ai.mcp.model.TextContent
import org.http4k.format.Jackson
import java.io.File
import java.util.concurrent.TimeUnit

fun main() {
    // 1. Define the 'kwrk' Tool
    val kwrkTool = Tool(
        name = "run_kwrk",
        description = "Execute a Tulip tulip-runtime benchmark (kwrk) on a specified workload file.",
        inputSchema = Jackson.obj(
            "workload" to Jackson.string("Path to the .yaml or .lua workload file"),
            "duration" to Jackson.string("Duration of the test (e.g., 10s, 1m)"),
            "rps" to Jackson.number("Target requests per second (e.g., 500)")
        )
    )

    // 2. Logic to invoke the system command
    val kwrkHandler = ToolHandler { request ->
        val workload = request.arguments["workload"]?.textValue() ?: ""
        val duration = request.arguments["duration"]?.textValue() ?: "10s"
        val rps = request.arguments["rps"]?.asInt() ?: 100

        // Security check: ensure file exists before execution
        val workloadFile = File(workload)
        if (!workloadFile.exists()) {
            return@ToolHandler McpResult.Error("Workload file not found: ${workloadFile.absolutePath}")
        }

        try {
            // Build the command: kwrk -w <workload> -d <duration> -r <rps>
            val process = ProcessBuilder("kwrk", "-w", workload, "-d", duration, "-r", rps.toString())
                .redirectErrorStream(true)
                .start()

            // Capture output with a timeout to prevent hanging the MCP session
            val output = process.inputStream.bufferedReader().use { it.readText() }
            val finished = process.waitFor(5, TimeUnit.MINUTES)

            if (!finished) {
                process.destroyForcibly()
                McpResult.Error("Benchmark timed out after 5 minutes.")
            } else {
                McpResult.Success(listOf(TextContent("Process exited with code ${process.exitValue()}\n\n$output")))
            }
        } catch (e: Exception) {
            McpResult.Error("Failed to execute kwrk: ${e.message}")
        }
    }

    // 3. Setup and Run the Server
    val server = McpServer(
        Implementation("Tulip-JBang-Server", "1.0.0"),
        V2024_11_05,
        Jackson
    ).apply {
        withTool(kwrkTool, kwrkHandler)
    }

    // Connect via Stdio for easy integration with Claude/IDE
    server.asStdioServer().start()
}