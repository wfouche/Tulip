from __future__ import print_function
import datetime
import json
import sys
import org.HdrHistogram.Histogram as Histogram

filename = sys.argv[1]
if sys.argv[2] == '0':
    print_detail_rows = False
else:
    print_detail_rows = True

now = datetime.datetime.now()

class Summary:
    num_rows = 0
    num_actions = 0
    num_failed = 0
    duration = 0.0
    max_rt = 0.0
    max_rt_ts = ""
    max_awt = 0.0
    max_wt = 0.0
    avg_qs = 0.0
    max_qs = 0

jhh = {}
jss = {}

header = """<!DOCTYPE html>
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
    <th>Avg WT</th>
    <th>Max WT</th>
    <th>Avg QS</th>
    <th>Max QS</th>
    <th>Avg TPS</th>
    <th>Avg RT</th>
    <th>Stdev</th>
    <th>90p RT</th>
    <th>99p RT</th>
    <th>Max RT</th>
    <th>Max RTT</th>
  </tr>
"""

benchmark_columns = """
  <tr>
    <th>SID</th>
    <th>Name</th>
    <th>RID</th>
    <th>Duration</th>
    <th>#N</th>
    <th>#F</th>
    <th>Avg WT</th>
    <th>Max WT</th>
    <th>Avg QS</th>
    <th>Max QS</th>
    <th>Avg TPS</th>
    <th>Avg RT</th>
    <th>Stdev</th>
    <th>90p RT</th>
    <th>99p RT</th>
    <th>Max RT</th>
    <th>Max RTT</th>
  </tr>
"""

benchmark_header = """
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
  </tr>
"""

benchmark_empty_row = """
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
  </tr>
"""

benchmark_detail_row = """
  <tr>
    <td></td>
    <td></td>
    <td>%d</td>
    <td>%s</td>
    <td>%d</td>
    <td>%d</td>
    <td>%.1f ms</td>
    <td>%.1f ms</td>
    <td>%.3f</td>
    <td>%d</td>
    <td>%.3f</td>
    <td>%.3f ms</td>
    <td>%.1f ms</td>
    <td>%.1f ms</td>
    <td>%.1f ms</td>
    <td>%.1f ms</td>
    <td>%s</td>
  </tr>
"""

benchmark_summary_row = """
  <tr>
    <td></td>
    <td></td>
    <td>%s</td>
    <td><b>%s</b></td>
    <td><b>%d</b></td>
    <td><b>%d</b></td>
    <td><b>%.1f ms</b></td>
    <td><b>%.1f ms</b></td>
    <td><b>%.3f</b></td>
    <td><b>%d</b></td>
    <td><b>%.3f</b></td>
    <td><b>%.3f ms</b></td>
    <td><b>%.1f ms</b></td>
    <td><b>%.1f ms</b></td>
    <td><b>%.1f ms</b></td>
    <td><b>%.1f ms</b></td>
    <td><b>%s</b></td>
  </tr>
"""

trailer = """
</table>
</body>
</html>
"""

sm = None
jh = Histogram(1, 3600*1000*1000, 3)
fileObj = open(filename)
jb = json.load(fileObj)
description = "/ " + jb["config"]["static"]["description"] + " / " + jb["timestamp"].replace("_", " ")
rb = jb["results"]

report_fn = jb["config"]["static"]["report_filename"]
report_fh = open(report_fn, "w+")

def printf(s):
    report_fh.write(s)

def print_global_summary():
    html = benchmark_summary_row%("",str(datetime.timedelta(seconds=int(sm.duration))),sm.num_actions,sm.num_failed,sm.max_awt,sm.max_wt,sm.avg_qs,sm.max_qs,sm.num_actions/sm.duration,jh.getMean()/1000.0,jh.getStdDeviation()/1000.0,jh.getValueAtPercentile(90.0)/1000.0,jh.getValueAtPercentile(99.0)/1000.0,sm.max_rt,sm.max_rt_ts[8:-4].replace("_"," "))
    if not print_detail_rows:
        html = html.replace("<b>","")
        html = html.replace("</b>","")
    printf(html)

def print_action_summary():
    for key in jss.keys():
        smx = jss[key]
        jhx = jhh[key]
        if jb["config"]["static"]["user_actions"].has_key(key):
            text = "[%s.%s]"%(key, jb["config"]["static"]["user_actions"][key])
        else:
            text = "[%s]"%(key)
        html = benchmark_summary_row%(text,str(datetime.timedelta(seconds=int(sm.duration))),smx.num_actions,smx.num_failed,smx.max_awt,smx.max_wt,smx.avg_qs,smx.max_qs,smx.num_actions/smx.duration,jhx.getMean()/1000.0,jhx.getStdDeviation()/1000.0,jhx.getValueAtPercentile(90.0)/1000.0,jhx.getValueAtPercentile(99.0)/1000.0,smx.max_rt,smx.max_rt_ts[8:-4].replace("_"," "))
        if not print_detail_rows:
            html = html.replace("<b>","")
            html = html.replace("</b>","")
        printf(html)

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
        printf(benchmark_header%(int(e["scenario_id"]), e["test_name"] + " (u:%d t:%d)"%(e["num_users"],e["num_threads"])))
    ht = Histogram.fromString(e["histogram_rt"])
    jh.add(ht)
    if print_detail_rows: printf(benchmark_detail_row%( \
        e["row_id"],
        e["duration"],
        e["num_actions"],
        e["num_failed"],
        e["avg_wt"],
        e["max_wt"],
        e["avg_wthread_qsize"],
        e["max_wthread_qsize"],
        e["avg_tps"],
        e["avg_rt"],
        ht.getStdDeviation()/1000.0,
        e["percentiles_rt"]["90.0"],
        e["percentiles_rt"]["99.0"],
        e["max_rt"],
        e["max_rt_ts"][8:-4].replace("_"," ")
        ))
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

print_action_summary()
print_global_summary()
printf(trailer)

report_fh.close()