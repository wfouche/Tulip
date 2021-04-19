from __future__ import print_function

import json
import fileinput

s0 = """[width="100%",options="header"]
|===
|Action ID | Avg TPS | Actions Total | Actions Failed | Duration (secs) | Avg RT (ms) | p95 RT (ms) | Max RT (ms) | CPU
"""

def ff(v):
    return "%.3f"%(float(v))

row_count = 0
for line in fileinput.input():
    x=json.loads(line)

    if int(x["row_id"]) == 0:
        if row_count > 0:
            print("|===")
        print("")
        print("." + x["scenario_name"] + " - " +  x["test_name"])
        print(s0)

    print("|", x["test_end"].split("T")[0], x["test_end"].split("T")[1])
    print("|", ff(x["avg_tps"]))
    print("|", x["num_actions"])
    print("|", x["num_failed"])
    print("|", x["duration"])
    print("|", ff(x["avg_rt"]))
    print("|", ff(x["percentiles_rt"]["95.0"]))
    print("|", ff(x["max_rt"]))
    print("|", ff(x["avg_cpu_system"]))

    print("")

    row_count += 1

    r = x["user_actions"]
    for k in r.keys():
        if True:
            b = r[k]
            print("|", k)
            print("|", ff(b["avg_tps"]))
            print("|", b["num_actions"])
            print("|", b["num_failed"])
            print("|", x["duration"])
            print("|", ff(b["avg_rt"]))
            print("|", ff(b["percentiles_rt"]["95.0"]))
            print("|", ff(b["max_rt"]))
            print("|", ff(x["avg_cpu_system"]))

            print("")

print("|===")