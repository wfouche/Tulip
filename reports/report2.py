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
        if "save_stats" in b.keys():
            if b["save_stats"] == False:
                continue
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
                printf('! %d seconds\n'%(b["time"][k]))
            printf('!===\n')

        printf("|===\n")

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
        printf('| *num_users*   | %d\n'%(c["num_users"]))
        printf('| *num_threads* | %d\n'%(c["num_threads"]))
        if "enabled" in c.keys():
            printf('| *enabled* | %s\n'%(c["enabled"]))
        else:
            printf('| *enabled* | True\n')
        printf("|===\n")

    report_fh.close()

if __name__ == "__main__":
    filename = sys.argv[1]
    createReport(filename)
