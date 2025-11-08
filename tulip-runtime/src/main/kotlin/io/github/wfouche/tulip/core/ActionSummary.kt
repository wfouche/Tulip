package io.github.wfouche.tulip.core

import io.github.wfouche.tulip.stats.LlqHistogram
import org.HdrHistogram.Histogram

data class ActionSummary(
    var actionId: Int = 0,
    var rowId: Int = 0,
    var testBegin: String = "",
    var testEnd: String = "",
    var testName: String = "",
    var testPhase: String = "",
    var testId: Int = 0,
    var indexUserProfile: Int = 0,
    var queueLength: Int = 0,
    var numActions: Int = 0,
    var numSuccess: Int = 0,
    var hdr_histogram: Histogram = Histogram(histogramNumberOfSignificantValueDigits),
    var llq_histogram: LlqHistogram = LlqHistogram(),
    var durationSeconds: Double = 0.0,
    var aps: Double = 0.0,
    var aps_target: Double = 0.0,
    var art: Double = 0.0,
    var sdev: Double = 0.0,
    var minRt: Double = 0.0,
    var maxRt: Double = 0.0,
    var maxRtTs: String = "",
    var awt: Double = 0.0,
    var maxWt: Double = 0.0,
    var pk: List<Double> = mutableListOf(),
    var pv: List<Double> = mutableListOf(),

    // var avgCpuSystem: Double = 0.0,
    // var avgCpuProcess: Double = 0.0
    var processCpuTime: Long = 0,
    var processCpuCores: Double = 0.0,
    var processCpuUtilization: Double = 0.0,
)
