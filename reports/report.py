from __future__ import print_function
import datetime
import json
import sys
import org.HdrHistogram.Histogram as Histogram
from collections import OrderedDict
import java.io.PrintStream as PrintStream

# <h2><a href="https://wfouche.github.io/Tulip-docs">__DESC1__</a> / __DESC2__</h2>
# <h2>__DESC1__ / __DESC2__</h2>

header = '''<!DOCTYPE html>
<html>

<style>
table, th, td {
  border:1px solid black; font-size:16px; text-align: center;
}
th:nth-child(n+14) {
    background-color: #D3D3D3;
}
td:nth-child(n+14) {
    background-color: #D3D3D3;
}
</style>

<body>

<h2><a href="https://wfouche.github.io/Tulip-docs">__DESC1__</a> / __DESC2__</h2>

<table style="width:100%">
  <tr>
    <th>Benchmark</th>
    <th>Run Id</th>
    <th>#N</th>
    <th>#F</th>
    <th>Duration</th>
    <th>Aps</th>
    <th>Avg_Rt</th>
    <th>Stdev</th>
    <th>Min_Rt</th>
    <th>90p_Rt</th>
    <th>99p_Rt</th>
    <th>Max_Rt</th>
    <th>Max_Rtt</th>
    <th>AQS</th>
    <th>MQS</th>
    <th>AWT</th>
    <th>MWT</th>
    <th>CPU_T</th>
    <th>CPU</th>
    <th>MEM</th>
  </tr>
'''

benchmark_columns = '''
  <tr>
    <th>Benchmark</th>
    <th>Run Id</th>
    <th>#N</th>
    <th>#F</th>
    <th>Duration</th>
    <th>Aps</th>
    <th>Avg_Rt</th>
    <th>Stdev</th>
    <th>Min_Rt</th>
    <th>90p_Rt</th>
    <th>99p_Rt</th>
    <th>Max_Rt</th>
    <th>Max_Rtt</th>
    <th>AQS</th>
    <th>MQS</th>
    <th>AWT</th>
    <th>MWT</th>
    <th>CPU_T</th>
    <th>CPU</th>
    <th>MEM</th>
  </tr>
'''

benchmark_header = '''
  <tr>
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
    <td></td>
  </tr>
'''

benchmark_empty_row = '''
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
'''

benchmark_detail_row = '''
  <tr>
    <td>%s</td>
    <td>%d</td>
    <td>%d</td>
    <td>%d</td>
    <td>%s</td>
    <td>%.3f</td>
    <td>%.3f ms</td>
    <td>%.1f ms</td>
    <td>%.1f ms</td>
    <td>%.1f ms</td>
    <td>%.1f ms</td>
    <td>%.1f ms</td>
    <td>%s</td>
    <td>%.1f</td>
    <td>%d</td>
    <td>%.1f</td>
    <td>%.1f</td>
    <td>%s</td>
    <td>%.1f</td>
    <td>%.1f</td>
  </tr>
'''

benchmark_summary_row = '''
  <tr>
    <td>%s</td>
    <td>%s</td>
    <td><b>%d</b></td>
    <td><b><tag1>%d</tag1></b></td>
    <td><b>%s</b></td>
    <td><b><tag2>%.3f</tag2></b></td>
    <td><b>%.3f ms</b></td>
    <td><b>%.1f ms</b></td>
    <td><b>%.1f ms</b></td>
    <td><b>%.1f ms</b></td>
    <td><b>%.1f ms</b></td>
    <td><b>%.1f ms</b></td>
    <td><b>%s</b></td>
    <td><b>%.1f</b></td>
    <td><b>%d</b></td>
    <td><b>%.1f</b></td>
    <td><b>%.1f</b></td>
    <td><b>%s</b></td>
    <td><b>%.1f</b></td>
    <td><b>%.1f</b></td>
  </tr>
'''

trailer = '''
</table>
</body>
</html>
'''

class Summary:
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
    name = ""
    cpu_time_ns = 0

def createReport(filename):

    print("\nOutput filename = " + filename)

    jhh = {}
    jss = {}

    print_detail_rows = True

    global name2s
    global name2s_list
    name2s = ""
    name2s_list = []

    sm = None
    jh = Histogram(1, 3600*1000*1000, 3)
    fileObj = open(filename)
    jb = json.load(fileObj, object_pairs_hook=OrderedDict)
    version = jb['version']
    desc1 = 'Tulip ' + version
    desc2 = jb["config"]["actions"]["description"] + " / " + jb["timestamp"]
    rb = jb["results"]

    report_fn = jb["config"]["actions"]["report_filename"]
    report_fh = open(report_fn, "w+")

    print("Report filename = " + report_fn)

    def printf(s):
        report_fh.write(s)

    def str_from_cpu_time_ns(v_ns):
        r = str(datetime.timedelta(seconds=v_ns/1000000000.0))  #[:-5]
        if '.' in r:
            r = r[:-5]
        return r

    def print_global_summary():
        global name2s
        global name2s_list
        avg_aps = 0.0 if sm.name in ["onStart", "onStop"] else sm.num_actions/sm.duration
        if sm.name in ["onStart", "onStop"]:
            cpu_t = "0:00:00"
            sm.cpu = 0.0
        else:
            cpu_t = str_from_cpu_time_ns(sm.cpu_time_ns)
        html = benchmark_summary_row%(name2s,"",sm.num_actions,sm.num_failed,str(datetime.timedelta(seconds=int(sm.duration))),avg_aps,jh.getMean()/1000.0,jh.getStdDeviation()/1000.0,sm.min_rt,jh.getValueAtPercentile(90.0)/1000.0,jh.getValueAtPercentile(99.0)/1000.0,sm.max_rt,sm.max_rt_ts[8:],sm.avg_qs,sm.max_qs,sm.max_awt,sm.max_wt,cpu_t,sm.cpu,sm.mem)
        if not print_detail_rows:
            html = html.replace("<b>","")
            html = html.replace("</b>","")
        # Validation: #F
        if sm.num_failed > 0:
            html = html.replace("<tag1>","<mark>")
            html = html.replace("</tag1>","</mark>")
        else:
            html = html.replace("<tag1>","")
            html = html.replace("</tag1>","")
        # Validation: Avg_APS
        #print(sm.name)
        if "aps_rate" in jb["config"]["benchmarks"][sm.name].keys():
            target_aps = jb["config"]["benchmarks"][sm.name]["aps_rate"]
        else:
            target_aps = 0.0
        if target_aps > 0.0:
            delta_percentage_aps = 100.0*abs(target_aps-avg_aps)/target_aps
            if delta_percentage_aps > 1.5:
                html = html.replace("<tag2>","<mark>")
                html = html.replace("</tag2>","</mark>")
            else:
                html = html.replace("<tag2>","")
                html = html.replace("</tag2>","")
        else:
            html = html.replace("<tag2>","")
            html = html.replace("</tag2>","")
        printf(html)
        if len(name2s_list) > 0:
            name2s = name2s_list[0]
            del name2s_list[0]

    def print_action_summary():
        global name2s
        global name2s_list
        for key in jss.keys():
            smx = jss[key]
            jhx = jhh[key]
            if jb["config"]["actions"]["user_actions"].has_key(key):
                text = "[%s.%s]"%(key, jb["config"]["actions"]["user_actions"][key])
            else:
                text = "[%s]"%(key)
            text = "<a href='file.html'>%s</a>"%(text)
            printStream = PrintStream('file.html')
            printStream.print("<html>")
            printStream.println()
            printStream.print("<body>")
            printStream.println()
            printStream.print("<h2>Benchmark: %s</h2>"%(smx.name))
            printStream.println()
            printStream.print("<h3>Response Time (ms) Percentile Distribution</h3>")
            printStream.println()
            printStream.print("<pre>")
            printStream.println()
            jhx.outputPercentileDistribution(printStream, 1000.0)
            printStream.print("</pre>")
            printStream.println()
            printStream.print("</body>")
            printStream.println()
            printStream.print("</html>")
            printStream.println()
            printStream.flush()
            printStream.close()
            avg_aps = 0.0 if smx.name in ["onStart", "onStop"] else smx.num_actions/smx.duration
            if smx.name in ["onStart", "onStop"]:
                cpu_t = "0:00:00"
                smx.cpu = 0.0
            else:
                cpu_t = str_from_cpu_time_ns(smx.cpu_time_ns)
            html = benchmark_summary_row%(name2s,text,smx.num_actions,smx.num_failed,str(datetime.timedelta(seconds=int(sm.duration))),avg_aps,jhx.getMean()/1000.0,jhx.getStdDeviation()/1000.0,smx.min_rt,jhx.getValueAtPercentile(90.0)/1000.0,jhx.getValueAtPercentile(99.0)/1000.0,smx.max_rt,smx.max_rt_ts[8:],smx.avg_qs,smx.max_qs,smx.max_awt,smx.max_wt,cpu_t,smx.cpu,smx.mem)
            if not print_detail_rows:
                html = html.replace("<b>","")
                html = html.replace("</b>","")
            # Remove tag1
            html = html.replace("<tag1>","")
            html = html.replace("</tag1>","")
            # Remove tag2
            html = html.replace("<tag2>","")
            html = html.replace("</tag2>","")

            printf(html)
            if len(name2s_list) > 0:
                name2s = name2s_list[0]
                del name2s_list[0]

    printf(header.replace("__DESC1__", desc1).replace("__DESC2__", desc2))

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
            sm.name = e["bm_name"]
            jh.reset()
            jhh = {}
            jss = {}
            printf(benchmark_header%(e["bm_name"]))
            if len(e["workflow_name"]) > 0:
                name2s_list = ["u:%d, t:%d"%(e["num_users"],e["num_threads"]), "w:%s"%(e["workflow_name"]), "c:%d"%(e["context_id"]) ,""]
            else:
                name2s_list = ["u:%d, t:%d"%(e["num_users"],e["num_threads"]), "c:%d"%(e["context_id"]),""]
            name2s = name2s_list[0]
            del name2s_list[0]
        ht = Histogram.fromString(e["histogram_rt"])
        jh.add(ht)
        p_mem = 100.0 * e["jvm_memory_used"] / e["jvm_memory_maximum"]
        p_cpu = e["process_cpu_utilization"]
        if e["bm_name"] in ["onStart", "onStop"]:
            cpu_t = "0:00:00"
            p_cpu = 0.0
        else:
            cpu_t = str_from_cpu_time_ns(e["process_cpu_time_ns"])
            p_cpu = e["process_cpu_utilization"]
        if print_detail_rows:
            printf(benchmark_detail_row%( \
                name2s,
                e["row_id"]+1,
                e["num_actions"],
                e["num_failed"],
                str(datetime.timedelta(seconds=int(e["duration"]))),
                0.0 if e["bm_name"] in ["onStart", "onStop"] else e["avg_aps"],
                e["avg_rt"],
                ht.getStdDeviation()/1000.0,
                e["min_rt"],
                e["percentiles_rt"]["90.0"],
                e["percentiles_rt"]["99.0"],
                e["max_rt"],
                e["max_rt_ts"][8:],
                e["avg_wthread_qsize"],
                e["max_wthread_qsize"],
                e["avg_wt"],
                e["max_wt"],
                cpu_t,
                p_cpu,
                p_mem
                ))
            if len(name2s_list) > 0:
                name2s = name2s_list[0]
                del name2s_list[0]
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
        sm.cpu_time_ns += e["process_cpu_time_ns"]

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
                smx.name = e["bm_name"]

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
            smx.cpu_time_ns += e["process_cpu_time_ns"]

    print_action_summary()
    print_global_summary()
    printf(trailer)

    report_fh.close()

if __name__ == "__main__":
    filename = sys.argv[1]
    createReport(filename)
