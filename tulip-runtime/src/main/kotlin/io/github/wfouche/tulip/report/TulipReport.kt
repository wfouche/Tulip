package io.github.wfouche.tulip.report

import org.python.util.PythonInterpreter

val jythonCode: String = """
from __future__ import print_function
import datetime
import json
import sys
import org.HdrHistogram.Histogram as Histogram
from collections import OrderedDict

# <h2><a href="https://wfouche.github.io/Tulip">__DESC1__</a> / __DESC2__</h2>
# <h2>__DESC1__ / __DESC2__</h2>

header = '''<!DOCTYPE html>
<html>
<style>
table, th, td {
  border:1px solid black; font-size:16px; text-align: center;
}
</style>
<body>

<h2>__DESC1__ / __DESC2__</h2>

<table style="width:100%">
  <tr>
    <th>CID</th>
    <th>Name</th>
    <th>RID</th>
    <th>Duration</th>
    <th>#N</th>
    <th>#F</th>
    <th>Avg RPS</th>
    <th>Avg RT</th>
    <th>Stdev</th>
    <th>Min RT</th>
    <th>90p RT</th>
    <th>99p RT</th>
    <th>Max RT</th>
    <th>Max RTT</th>
    <th>AQS</th>
    <th>MQS</th>
    <th>AWT</th>
    <th>MWT</th>
    <th>CPU</th>
    <th>MEM</th>
  </tr>
'''

benchmark_columns = '''
  <tr>
    <th>CID</th>
    <th>Name</th>
    <th>RID</th>
    <th>Duration</th>
    <th>#N</th>
    <th>#F</th>
    <th>Avg RPS</th>
    <th>Avg RT</th>
    <th>Stdev</th>
    <th>Min RT</th>
    <th>90p RT</th>
    <th>99p RT</th>
    <th>Max RT</th>
    <th>Max RTT</th>
    <th>AQS</th>
    <th>MQS</th>
    <th>AWT</th>
    <th>MWT</th>
    <th>CPU</th>
    <th>MEM</th>
  </tr>
'''

benchmark_header = '''
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
    <td></td>
    <td>%s</td>
    <td>%d</td>
    <td>%s</td>
    <td>%d</td>
    <td>%d</td>
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
    <td>%.1f ms</td>
    <td>%.1f ms</td>
    <td>%.1f</td>
    <td>%.1f</td>
  </tr>
'''

benchmark_summary_row = '''
  <tr>
    <td></td>
    <td>%s</td>
    <td>%s</td>
    <td><b>%s</b></td>
    <td><b>%d</b></td>
    <td><b>%d</b></td>
    <td><b>%.3f</b></td>
    <td><b>%.3f ms</b></td>
    <td><b>%.1f ms</b></td>
    <td><b>%.1f ms</b></td>
    <td><b>%.1f ms</b></td>
    <td><b>%.1f ms</b></td>
    <td><b>%.1f ms</b></td>
    <td><b>%s</b></td>
    <td><b>%.1f</b></td>
    <td><b>%d</b></td>
    <td><b>%.1f ms</b></td>
    <td><b>%.1f ms</b></td>
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
    global name2s_list
    name2s = ""
    name2s_list = []

    sm = None
    jh = Histogram(1, 3600*1000*1000, 3)
    fileObj = open(filename)
    jb = json.load(fileObj, object_pairs_hook=OrderedDict)
    version = jb['version']
    desc1 = 'Tulip ' + version
    desc2 = jb["config"]["actions"]["description"] + " / " + jb["timestamp"].replace("_", " ")
    rb = jb["results"]

    report_fn = jb["config"]["actions"]["report_filename"]
    report_fh = open(report_fn, "w+")

    print("Report filename = " + report_fn)

    def printf(s):
        report_fh.write(s)

    def print_global_summary():
        global name2s
        global name2s_list
        html = benchmark_summary_row%(name2s,"",str(datetime.timedelta(seconds=int(sm.duration))),sm.num_actions,sm.num_failed,sm.num_actions/sm.duration,jh.getMean()/1000.0,jh.getStdDeviation()/1000.0,sm.min_rt,jh.getValueAtPercentile(90.0)/1000.0,jh.getValueAtPercentile(99.0)/1000.0,sm.max_rt,sm.max_rt_ts[8:].replace("_"," "),sm.avg_qs,sm.max_qs,sm.max_awt,sm.max_wt,sm.cpu,sm.mem)
        if not print_detail_rows:
            html = html.replace("<b>","")
            html = html.replace("</b>","")
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
            html = benchmark_summary_row%(name2s,text,str(datetime.timedelta(seconds=int(sm.duration))),smx.num_actions,smx.num_failed,smx.num_actions/smx.duration,jhx.getMean()/1000.0,jhx.getStdDeviation()/1000.0,smx.min_rt,jhx.getValueAtPercentile(90.0)/1000.0,jhx.getValueAtPercentile(99.0)/1000.0,smx.max_rt,smx.max_rt_ts[8:].replace("_"," "),smx.avg_qs,smx.max_qs,smx.max_awt,smx.max_wt,smx.cpu,smx.mem)
            if not print_detail_rows:
                html = html.replace("<b>","")
                html = html.replace("</b>","")
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
            jh.reset()
            jhh = {}
            jss = {}
            printf(benchmark_header%(int(e["scenario_id"]), e["test_name"]))
            if len(e["workflow_name"]) > 0:
                name2s_list = ["u:%d, t:%d"%(e["num_users"],e["num_threads"]), "w:%s"%(e["workflow_name"]), ""]
            else:
                name2s_list = ["u:%d, t:%d"%(e["num_users"],e["num_threads"]), ""]
            name2s = name2s_list[0]
            del name2s_list[0]
        ht = Histogram.fromString(e["histogram_rt"])
        jh.add(ht)
        p_mem = 100.0 * e["jvm_memory_used"] / e["jvm_memory_maximum"]
        p_cpu = e["process_cpu_utilization"]
        if print_detail_rows:
            printf(benchmark_detail_row%( \
                name2s,
                e["row_id"]+1,
                str(datetime.timedelta(seconds=int(e["duration"]))),
                e["num_actions"],
                e["num_failed"],
                e["avg_aps"],
                e["min_rt"],
                e["avg_rt"],
                ht.getStdDeviation()/1000.0,
                e["percentiles_rt"]["90.0"],
                e["percentiles_rt"]["99.0"],
                e["max_rt"],
                e["max_rt_ts"][8:].replace("_"," "),
                e["avg_wthread_qsize"],
                e["max_wthread_qsize"],
                e["avg_wt"],
                e["max_wt"],
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

val jythonCode2: String = """
from __future__ import print_function
import json
import sys
import com.google.gson.JsonParser as JsonParser
import os
from collections import OrderedDict

header = '''= Tulip Configuration Report
:toc: left
:sectnums:
:diagram-server-url: https://kroki.io/
:diagram-server-type: kroki_io

Description::
  __DESCRIPTION__ 

Filename::
  __CONFIG_FILENAME__

== Actions

[%header,cols="1a,2a"]
|===
| id | value
'''

def createReport(filename):

    def printf(s):
        report_fh.write(s)

    def generate_table(e):
        printf("| *" + e + "*\n")
        printf("|\n")
        printf('[%header,cols="1a,2a"]\n')
        printf('!===\n')
        printf('! id ! value \n')
        for k in jb['actions'][e].keys():
            printf('! *' + k + '* ')
            printf('! ' + str(jb['actions'][e][k]) + '\n')
        printf('!===\n')

    def generate_workflow():
        diagId = -1
        def name_to_id(s):
            if s in '-':
                return 0
            return int(s)
        def action_name(s):
            if s in jb['actions']['user_actions'].keys():
                return jb['actions']['user_actions'][s]
            return '<unknown>'

        #print(jb.keys())
        #print(jb["workflows"].keys())
        for wn in jb["workflows"].keys():
            diagId += 1
            printf("\n")
            printf("[[%s]]\n"%(wn))
            printf("=== " + "%s\n"%(wn))
            printf("\n")
            printf('[%header,cols="1a,1a"]\n')
            printf('|===\n')
            printf('| Workflow Diagram | Specification\n')
            printf('|')
            printf("[plantuml,wfd%d,svg]"%(diagId) + '\n')
            printf('----\n')
            printf('@startuml\n')
            #printf('title %s\n'%(wn))
            for sname in jb['workflows'][wn].keys():
                if sname in ['-']:
                    printf('state "-" as A0\n')
                    continue
                sid = int(sname)
                printf('state "Action %d" as A%d\n'%(sid,sid))
                printf('A%d: <%s>\n'%(sid,action_name(sname)))
                printf('\n')
            for sname in jb['workflows'][wn].keys():
                if sname in ['*']:
                    continue
                if sname in ['-']:
                    mid = 0
                else:
                    mid = int(sname)
                for k in jb['workflows'][wn][sname].keys():
                    nid = name_to_id(k)
                    fv = jb['workflows'][wn][sname][k]
                    printf('A%d --> A%d: %.3f\n'%(mid,nid,fv))
            printf('@enduml\n')
            printf('----\n')
            printf('| \n')
            printf('[source,json]\n')
            printf('----\n')
            printf('%s\n'%(json.dumps(jb['workflows'][wn], indent=4)))
            printf('----\n')
            printf('|===\n')

    #print("\nConfig filename = " + filename)

    # .jsonc -> .adoc
    f_ext = os.path.splitext(filename)[1]
    report_fn = filename[:-len(f_ext)]+".adoc"
    report_fh = open(report_fn, "w+")

    # Remove all JSONC comments from the JSON
    sf = open(filename,'r').read()
    gsonJsonTree = JsonParser.parseString(sf)
    jsonWithoutComments = gsonJsonTree.toString()

    # Restore JSON from String
    jb = json.loads(jsonWithoutComments, object_pairs_hook=OrderedDict)

    # print header
    printf(header.replace("__CONFIG_FILENAME__", filename).replace("__DESCRIPTION__", jb['actions']['description']))

    # Actions
    for e in jb['actions'].keys():
        if e in ['user_params', 'user_actions']:
            generate_table(e)
            continue
        # | *description*
        # | Micro-benchmarks
        printf("| *" + e + '*\n')
        printf("| " + jb['actions'][e] + '\n')
    printf("|===" + '\n')

    # Workflows
    if "workflows" in jb.keys():
        printf("\n")
        printf("== Workflows \n")
        generate_workflow()

    # Benchmarks Data
    printf("\n")
    printf("== Benchmarks\n")
    for k in jb['benchmarks'].keys():
        b = jb['benchmarks'][k]
        printf("\n")
        printf('=== %s'%(k) + '\n')
        printf("\n")
        printf('[%header,cols="1a,2a"]\n')
        printf("|===\n")
        printf("| id | value\n")
        # enabled
        if "enabled" in b.keys():
            printf('| *enabled* | %s\n'%(b["enabled"]))
        else:
            printf('| *enabled* | True\n')
        # throughput_rate
        if "throughput_rate" in b.keys():
            printf('| *throughput_rate* | %.1f\n'%(b["throughput_rate"]))
        else:
            printf('| *throughput_rate* | 0.0\n')
        # worker_thread_queue_size
        if "worker_thread_queue_size" in b.keys():
            printf('| *worker_thread_queue_size* | %d\n'%(b["worker_thread_queue_size"]))
        else:
            printf('| *worker_thread_queue_size* | 0\n')
        # workflow
        if "scenario_workflow" in b.keys():
            workflow_is_defined = b["scenario_workflow"] in jb["workflows"].keys()
            if workflow_is_defined:
                printf('| *scenario_workflow* | <<%s>>\n'%(b["scenario_workflow"]))
            else:
                printf('| *scenario_workflow* | Error, scenario_workflow *%s* is not defined\n'%(b["scenario_workflow"]))
        elif "scenario_actions" in b.keys():
            printf('| *scenario_actions* \n')
            printf('| \n')
            printf('[%header,cols="1a,2a"]\n')
            printf('!===\n')
            printf('! id ! weight \n')
            for a in b["scenario_actions"]:
                printf('! %d\n'%(a["id"]))
                if "weight" in a.keys():
                    printf('! %d \n'%(a["weight"]))
                else:
                    printf('! - \n')
            printf('!===\n')

        printf("|===\n")

    # Context Data
    printf("\n")
    printf("== Contexts\n")
    printf("\n")
    printf('[%header,cols="1a,2a"]\n')
    printf("|===\n")
    printf("| id | value\n")

    for k in jb['contexts'].keys():
        c = jb['contexts'][k]
        printf('| %s'%(k) + '\n')
        printf('| \n')
        printf('[%header,cols="1a,2a"]\n')
        printf('!===\n')
        printf('! id ! value \n')
        printf('! *num_users*   ! %d\n'%(c["num_users"]))
        printf('! *num_threads* ! %d\n'%(c["num_threads"]))
        if "enabled" in c.keys():
            printf('! *enabled* ! %s\n'%(c["enabled"]))
        else:
            printf('! *enabled* ! True\n')
        printf("!===\n")
    printf("|===\n")

    print("  config report = " + report_fn)

    report_fh.close()
"""

fun createConfigReport(configFilename: String) {
    PythonInterpreter().use { pyInterp ->
        pyInterp.exec(jythonCode2)
        pyInterp.eval("createReport('${configFilename}')")
    }
}