package org.tulip.report

import org.python.util.PythonInterpreter

val jythonCode: String = """
from __future__ import print_function
import datetime
import json
import sys
import org.HdrHistogram.Histogram as Histogram

header = ""${'"'}<!DOCTYPE html>
<html>
<style>
table, th, td {
  border:1px solid black; font-size:16px; text-align: center;
}
</style>
<body>

<h2>Benchmark Report __DESC__</h2>

<table style="width:100%">
  <tr>
    <th>SID</th>
    <th>Name</th>
    <th>RID</th>
    <th>Duration</th>
    <th>#N</th>
    <th>#F</th>
    <th>Avg TPS</th>
    <th>Min RT</th>
    <th>Avg RT</th>
    <th>Stdev</th>
    <th>90p RT</th>
    <th>99p RT</th>
    <th>Max RT</th>
    <th>Max RTT</th>
    <th>MQS</th>
    <th>AQS</th>
    <th>Max WT</th>
    <th>Avg WT</th>
    <th>CPU</th>
    <th>JMM</th>
  </tr>
""${'"'}

benchmark_columns = ""${'"'}
  <tr>
    <th>SID</th>
    <th>Name</th>
    <th>RID</th>
    <th>Duration</th>
    <th>#N</th>
    <th>#F</th>
    <th>Avg TPS</th>
    <th>Min RT</th>
    <th>Avg RT</th>
    <th>Stdev</th>
    <th>90p RT</th>
    <th>99p RT</th>
    <th>Max RT</th>
    <th>Max RTT</th>
    <th>MQS</th>
    <th>AQS</th>
    <th>Max WT</th>
    <th>Avg WT</th>
    <th>CPU</th>
    <th>JMM</th>
  </tr>
""${'"'}

benchmark_header = ""${'"'}
  <tr>
    <td>%d</td>
    <td>%s</td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
  </tr>
""${'"'}

benchmark_empty_row = ""${'"'}
  <tr>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
  </tr>
""${'"'}

benchmark_detail_row = ""${'"'}
  <tr>
    <td></td>
    <td>%s</td>
    <td>%d</td>
    <td>%s</td>
    <td>%d</td>
    <td>%d</td>
    <td>%.3f</td>
    <td>%.1f ms</td>
    <td>%.3f ms</td>
    <td>%.1f ms</td>
    <td>%.1f ms</td>
    <td>%.1f ms</td>
    <td>%.1f ms</td>
    <td>%s</td>
    <td>%d</td>
    <td>%.1f</td>
    <td>%.1f ms</td>
    <td>%.1f ms</td>
    <td>%.1f</td>
    <td>%.1f</td>
  </tr>
""${'"'}

benchmark_summary_row = ""${'"'}
  <tr>
    <td></td>
    <td>%s</td>
    <td>%s</td>
    <td><b>%s</b></td>
    <td><b>%d</b></td>
    <td><b>%d</b></td>
    <td><b>%.3f</b></td>
    <td><b>%.1f ms</b></td>
    <td><b>%.3f ms</b></td>
    <td><b>%.1f ms</b></td>
    <td><b>%.1f ms</b></td>
    <td><b>%.1f ms</b></td>
    <td><b>%.1f ms</b></td>
    <td><b>%s</b></td>
    <td><b>%d</b></td>
    <td><b>%.1f</b></td>
    <td><b>%.1f ms</b></td>
    <td><b>%.1f ms</b></td>
    <td><b>%.1f</b></td>
    <td><b>%.1f</b></td>
  </tr>
""${'"'}

trailer = ""${'"'}
</table>
</body>
</html>
""${'"'}

class Summary:
    num_rows = 0
    num_actions = 0
    num_failed = 0
    duration = 0.0
    min_rt = 1000000000.0
    max_rt = 0.0
    max_rt_ts = ""
    mem = 0.0
    cpu = 0.0
    max_awt = 0.0
    max_wt = 0.0
    avg_qs = 0.0
    max_qs = 0

def createReport(filename):

    print("\nOutput filename = " + filename)
    
    jhh = {}
    jss = {}

    print_detail_rows = True

    global name2s
    name2s = ""

    sm = None
    jh = Histogram(1, 3600*1000*1000, 3)
    fileObj = open(filename)
    jb = json.load(fileObj)
    description = "/ " + jb["config"]["static"]["description"] + " / " + jb["timestamp"].replace("_", " ")
    rb = jb["results"]

    report_fn = jb["config"]["static"]["report_filename"]
    report_fh = open(report_fn, "w+")

    print("Report filename = " + report_fn)

    def printf(s):
        report_fh.write(s)

    def print_global_summary():
        global name2s
        html = benchmark_summary_row%(name2s,"",str(datetime.timedelta(seconds=int(sm.duration))),sm.num_actions,sm.num_failed,sm.num_actions/sm.duration,sm.min_rt,jh.getMean()/1000.0,jh.getStdDeviation()/1000.0,jh.getValueAtPercentile(90.0)/1000.0,jh.getValueAtPercentile(99.0)/1000.0,sm.max_rt,sm.max_rt_ts[8:-4].replace("_"," "),sm.max_qs,sm.avg_qs,sm.max_wt,sm.max_awt,sm.cpu,sm.mem)
        if not print_detail_rows:
            html = html.replace("<b>","")
            html = html.replace("</b>","")
        printf(html)
        name2s = ""

    def print_action_summary():
        global name2s
        for key in jss.keys():
            smx = jss[key]
            jhx = jhh[key]
            if jb["config"]["static"]["user_actions"].has_key(key):
                text = "[%s.%s]"%(key, jb["config"]["static"]["user_actions"][key])
            else:
                text = "[%s]"%(key)
            html = benchmark_summary_row%(name2s,text,str(datetime.timedelta(seconds=int(sm.duration))),smx.num_actions,smx.num_failed,smx.num_actions/smx.duration,smx.min_rt,jhx.getMean()/1000.0,jhx.getStdDeviation()/1000.0,jhx.getValueAtPercentile(90.0)/1000.0,jhx.getValueAtPercentile(99.0)/1000.0,smx.max_rt,smx.max_rt_ts[8:-4].replace("_"," "),smx.max_qs,smx.avg_qs,smx.max_wt,smx.max_awt,smx.cpu,smx.mem)
            if not print_detail_rows:
                html = html.replace("<b>","")
                html = html.replace("</b>","")
            printf(html)
            name2s = ""

    printf(header.replace("__DESC__", description))

    prev_row_id = 0
    for e in rb:
        current_row_id = int(e["row_id"])
        if current_row_id <= prev_row_id:
            if sm is not None:
                print_action_summary()
                print_global_summary()
                printf(benchmark_empty_row)
                printf(benchmark_columns)
            sm = Summary()
            jh.reset()
            jhh = {}
            jss = {}
            printf(benchmark_header%(int(e["scenario_id"]), e["test_name"]))
            name2s = "(u:%d t:%d)"%(e["num_users"],e["num_threads"])
        ht = Histogram.fromString(e["histogram_rt"])
        jh.add(ht)
        p_mem = 100.0 * e["jvm_memory_total"] / e["jvm_memory_maximum"]
        p_cpu = e["system_cpu_utilization"]
        if print_detail_rows:
            printf(benchmark_detail_row%( \
                name2s,
                e["row_id"],
                e["duration"],
                e["num_actions"],
                e["num_failed"],
                e["avg_tps"],
                e["min_rt"],
                e["avg_rt"],
                ht.getStdDeviation()/1000.0,
                e["percentiles_rt"]["90.0"],
                e["percentiles_rt"]["99.0"],
                e["max_rt"],
                e["max_rt_ts"][8:-4].replace("_"," "),
                e["max_wthread_qsize"],
                e["avg_wthread_qsize"],
                e["max_wt"],
                e["avg_wt"],
                p_cpu,
                p_mem
                ))
            name2s = ""
        if sm.min_rt > e["min_rt"]:
            sm.min_rt = e["min_rt"]
        if sm.max_rt < e["max_rt"]:
            sm.max_rt = e["max_rt"]
            sm.max_rt_ts = e["max_rt_ts"]
        sm.num_actions += e["num_actions"]
        sm.num_failed += e["num_failed"]
        sm.duration += e["duration"]
        if sm.avg_qs < e["avg_wthread_qsize"]:
            sm.avg_qs = e["avg_wthread_qsize"]
        if sm.max_qs < e["max_wthread_qsize"]:
            sm.max_qs = e["max_wthread_qsize"]
        if sm.max_awt < e["avg_wt"]:
            sm.max_awt = e["avg_wt"]
        if sm.max_wt < e["max_wt"]:
            sm.max_wt = e["max_wt"]
        if sm.mem < p_mem:
            sm.mem = p_mem
        if sm.cpu < p_cpu:
            sm.cpu = p_cpu

        # jhh ...
        for key in e["user_actions"].keys():
            ar = e["user_actions"][key]
            htt = Histogram.fromString(ar["histogram_rt"])
            #print(ar["name"] + " - " + "%.3f"%(htt.getMean()/1000.0))
            if jhh.has_key(key):
                jhh[key].add(htt)
            else:
                jhh[key] = Histogram(1, 3600*1000*1000, 3)
                jhh[key].add(htt)
            #print(ar["name"] + " - " + "%.3f"%(jhh[key].getMean()/1000.0) + " - %d"%(jhh[key].getTotalCount()))

        # jss ...
        for key in e["user_actions"].keys():
            ar = e["user_actions"][key]
            if jss.has_key(key):
                smx = jss[key]
            else:
                smx = jss[key] = Summary()

            if smx.min_rt > ar["min_rt"]:
                smx.min_rt = ar["min_rt"]
            if smx.max_rt < ar["max_rt"]:
                smx.max_rt = ar["max_rt"]
                smx.max_rt_ts = ar["max_rt_ts"]
            smx.num_actions += ar["num_actions"]
            smx.num_failed += ar["num_failed"]

            smx.duration += e["duration"]
            if smx.avg_qs < e["avg_wthread_qsize"]:
                smx.avg_qs = e["avg_wthread_qsize"]
            if smx.max_qs < e["max_wthread_qsize"]:
                smx.max_qs = e["max_wthread_qsize"]
            if smx.max_awt < e["avg_wt"]:
                smx.max_awt = e["avg_wt"]
            if smx.max_wt < e["max_wt"]:
                smx.max_wt = e["max_wt"]
            if smx.mem < p_mem:
                smx.mem = p_mem
            if smx.cpu < p_cpu:
                smx.cpu = p_cpu

    print_action_summary()
    print_global_summary()
    printf(trailer)

    report_fh.close()
"""

fun createHtmlReport(outputFilename: String) {
    PythonInterpreter().use { pyInterp ->
        pyInterp.exec(jythonCode)
        pyInterp.eval("createReport('${outputFilename}')")
    }
}