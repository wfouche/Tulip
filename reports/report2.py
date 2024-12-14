from __future__ import print_function
import json
import sys


header = """= Tulip Configuration Report
:toc: left
:sectnums:

[NOTE]
====
__CONFIG_FILENAME__
====

== Static Data

[%header,cols="1a,2a"]
|===
| id | value
"""

def createReport(filename):

    def printf(s):
        report_fh.write(s)

    def generate_table(e):
        printf("| *" + e + "*\n")
        printf("|\n")
        printf('[%header,cols="1a,2a"]\n')
        printf('!===\n')
        printf('! id ! value \n')
        for k in jb['static'][e].keys():
            printf('! *' + k + '* ')
            printf('! ' + str(jb['static'][e][k]) + '\n')
        printf('!===\n')

    def generate_workflow(e):
        diagId = -1
        def name_to_id(s):
            if s in '-':
                return 0
            return int(s)
        def action_name(s):
            if s in jb['static']['user_actions'].keys():
                return jb['static']['user_actions'][s]
            return '<unknown>'

        for wn in jb['static'][e].keys():
            diagId += 1
            printf("| *" + e + "[%s]*\n"%(wn))
            printf("|\n")
            printf("[plantuml,wfd%d,svg]"%(diagId) + '\n')
            printf('....\n')
            printf('@startuml\n')
            printf('title %s\n'%(wn))
            for sname in jb['static'][e][wn].keys():
                if sname in ['-']:
                    printf('state "-" as A0\n')
                    continue
                sid = int(sname)
                printf('state "Action %d" as A%d\n'%(sid,sid))
                printf('A%d: %s\n'%(sid,action_name(sname)))
                printf('\n')
            for sname in jb['static'][e][wn].keys():
                if sname in ['*']:
                    continue
                if sname in ['-']:
                    mid = 0
                else:
                    mid = int(sname)
                for k in jb['static'][e][wn][sname].keys():
                    nid = name_to_id(k)
                    fv = jb['static'][e][wn][sname][k]
                    printf('A%d --> A%d: %.3f\n'%(mid,nid,fv))

            printf('@enduml\n')
            printf('....\n')

    print("\nConfig filename = " + filename)

    report_fn = filename[:-4]+"adoc"
    report_fh = open(report_fn, "w+")

    printf(header.replace("__CONFIG_FILENAME__", filename))

    fileObj = open(filename)
    jb = json.load(fileObj)

    # Static Data
    for e in jb['static'].keys():
        if e in ['workflows']:
            generate_workflow(e)
            continue
        if e in ['user_params', 'user_actions']:
            generate_table(e)
            continue
        # | *description*
        # | Micro-benchmarks
        printf("| *" + e + '*\n')
        printf("| " + jb['static'][e] + '\n')
    printf("|===" + '\n')

    # Context Data
    printf("\n")
    printf("== Contexts\n")
    for c in jb['contexts']:
        printf('.%s'%(c["name"]) + '\n')
        printf('[%header,cols="1a,2a"]\n')
        printf("|===\n")
        printf("| id | value\n")
        if "enabled" in c.keys():
            printf('| *enabled* | %s\n'%(c["enabled"]))
        printf('| *num_users*   | %d\n'%(c["num_users"]))
        printf('| *num_threads* | %d\n'%(c["num_threads"]))
        printf("|===\n")

    # Benchmarks Data
    printf("\n")
    printf("== Benchmarks\n")
    printf('[%header,cols="1a,2a"]\n')
    printf("|===\n")
    printf("| id | value\n")
    printf("|===\n")

    print("Report filename = " + report_fn)

    report_fh.close()

if __name__ == "__main__":
    filename = sys.argv[1]
    createReport(filename)
