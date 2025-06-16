from __future__ import print_function
import json
import sys
import com.google.gson.JsonParser as JsonParser
import os
from collections import OrderedDict

# /// jbang
# requires-jython = "==2.7.4"
# requires-java = ">=21"
# dependencies = [
#   "com.google.code.gson:gson:2.13.1",
#   "org.hdrhistogram:HdrHistogram:2.2.2"
# ]
# [python-jvm]
#   debug = false
# ///

header = '''= __DESCRIPTION__
:toc: left
:sectnums:
:diagram-server-url: https://kroki.io/
:diagram-server-type: kroki_io

++++
<style>
/* CSS block for styling the main content area */
#content {
    max-width: 960px; /* Set your desired maximum width */
    margin: 0 auto;  /* Center the content */
    padding: 0 1em; /* Add some horizontal padding */
}
/* You can also target other elements like header or footer */
#header, #footer {
    max-width: 960px; /* Apply the same max-width to header and footer */
    margin: 0 auto;
}
</style>
++++

Filename::
  __CONFIG_FILENAME__

== Actions

=== Configuration

[%header,cols="1a,4a"]
|===
| id | value
'''

def createReport(filename):

    def printf(s):
        report_fh.write(s)

    def actionName(s):
        return s.split(",")[0].strip()

    def actionDesc(s):
        try:
            return s.split(",")[1].strip()
        except:
            return ""

    def generate_table1(e):
        printf("| *" + e + "*\n")
        printf("|\n")
        printf('[%header,cols="1a,3a"]\n')
        printf('!===\n')
        printf('! id ! value \n')
        for k in jb['actions'][e].keys():
            printf('! *' + k + '* ')
            printf('! ' + str(jb['actions'][e][k]) + '\n')
        printf('!===\n')

    def generate_table2(e):
        printf("| *" + e + "*\n")
        printf("|\n")
        printf('[%header,cols="2a,2a,4a"]\n')
        printf('!===\n')
        printf('! id ! value ! description\n')
        for k in jb['actions'][e].keys():
            printf('! *' + k + '* ')
            printf('! ' + actionName(str(jb['actions'][e][k])))
            printf('! ' + actionDesc(str(jb['actions'][e][k])) + '\n')
        printf('!===\n')

    def generate_workflow():
        diagId = -1
        def name_to_id(s):
            if s in '-':
                return 0
            return int(s)
        def action_name(s):
            if s in jb['actions']['user_actions'].keys():
                return actionName(jb['actions']['user_actions'][s])
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

    # report dir
    report_dir = "."
    config_dir = "."
    if os.path.isdir("build/reports/tulip"):
        report_dir = "build/reports/tulip"
        config_dir = "src/main/resources"

    if not os.path.exists(filename):
        filename = config_dir + "/" + filename

    # .json/.jsonc -> .adoc
    f_ext = os.path.splitext(filename)[1]
    report_fn = filename[:-len(f_ext)]+".adoc"
    report_fn = os.path.basename(report_fn)
    report_fp = report_dir + "/" + report_fn
    report_fh = open(report_fp, "w+")

    # Remove all JSONC comments from the JSON
    sf = open(filename,'r').read()
    gsonJsonTree = JsonParser.parseString(sf)
    jsonWithoutComments = gsonJsonTree.toString()
    gsonJsonTree = None

    # Restore JSON from String
    jb = json.loads(jsonWithoutComments, object_pairs_hook=OrderedDict)
    jsonWithoutComments = None

    # print header
    printf(header.replace("__CONFIG_FILENAME__", filename).replace("__DESCRIPTION__", jb['actions']['description']))

    # Actions
    for e in jb['actions'].keys():
        if e in ['user_params', 'user_actions']:
            if e == 'user_params':
                generate_table1(e)
            if e == 'user_actions':
                generate_table2(e)
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

    # Context Data
    printf("\n")
    printf("== Contexts\n")
    printf("\n")

    for k in jb['contexts'].keys():
        c = jb['contexts'][k]
        printf('=== %s'%(k) + '\n')
        printf('\n')
        printf('[%header,cols="1a,2a"]\n')
        printf('|===\n')
        printf('| id | value \n')
        if "enabled" in c.keys():
            printf('| *enabled* | %s\n'%(c["enabled"]))
        else:
            printf('| *enabled* | True\n')
        printf('| *num_users*   | %d\n'%(c["num_users"]))
        printf('| *num_threads* | %d\n'%(c["num_threads"]))
        printf("|===\n")

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
        # aps_rate
        if "aps_rate" in b.keys():
            printf('| *aps_rate* | %.1f\n'%(b["aps_rate"]))
        else:
            printf('| *aps_rate* | 0.0\n')
        if "aps_rate_step_change" in b.keys():
            printf('| *aps_rate_step_change* | %.1f\n'%(b["aps_rate_step_change"]))
        if "aps_rate_step_count" in b.keys():
            printf('| *aps_rate_step_count* | %d\n'%(b["aps_rate_step_count"]))

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
        # time
        if "time" in b.keys():
            printf('| *time* \n')
            printf('| \n')
            printf('[%noheader,cols="2a,1a"]\n')
            printf('!===\n')
            #printf('! id ! value \n')
            for k in b["time"].keys():
                printf('! *%s*\n'%(k))
                if k == "benchmark_iterations":
                    printf('! %d\n'%(b["time"][k]))
                else:
                    printf('! %d seconds\n'%(b["time"][k]))
            printf('!===\n')

        printf("|===\n")

    report_fh.close()
    return report_fp

if __name__ == "__main__":
    if len(sys.argv) >= 2:
        filename = sys.argv[1]
        createReport(filename)
