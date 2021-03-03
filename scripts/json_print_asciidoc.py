from __future__ import print_function

import json
import fileinput

s0 = """[width="100%",options="header"]
|===
|Row ID | Avg TPS | Actions Total | Actions Failed | Duration (secs) | Avg RT (ms) | p95 RT (ms) | Max RT (ms) | CPU
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

    print("|", x["row_id"])
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

print("|===")
