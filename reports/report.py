from __future__ import print_function

import json
import sys
import org.HdrHistogram.Histogram as Histogram

header = """<!DOCTYPE html>
<html>
<style>
table, th, td {
  border:1px solid black; font-size:16px; text-align: center;
}
</style>
<body>

<h2>Tulip Benchmark Report</h2>

<table style="width:100%">
  <tr>
    <th>SID</th>
    <th>Name</th>
    <th>RID</th>
    <th>Duration</th>
    <th>#T</th>
    <th>#S</th>
    <th>#F</th>
    <th>Avg TPS</th>
    <th>Avg RT</th>
    <th>90p RT</th>
    <th>95p RT</th>
    <th>99p RT</th>
    <th>Max RT</th>
    <th>Max RT Timestamp</th>
  </tr>
"""

benchmark_header = """
  <tr>
    <td>%d</td>
    <td><b>%s</b></td>
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

benchmark_detail_row = """
  <tr>
    <td></td>
    <td></td>
    <td>%d</td>
    <td>%s</td>
    <td>%d</td>
    <td>%d</td>
    <td>%d</td>
    <td>%.1f</td>
    <td>%.3f ms</td>
    <td>%.1f ms</td>
    <td>%.1f ms</td>
    <td>%.1f ms</td>
    <td>%.1f ms</td>
    <td>%s</td>
  </tr>
"""

trailer = """
</table>

</body>
</html>
"""

print(header)

jh = Histogram(30*1000*1000, 3)
filename = sys.argv[1]
fileObj = open(filename)
jb = json.load(fileObj)
prev_row_id = 0
for e in jb:
    current_row_id = int(e["row_id"])
    if current_row_id <= prev_row_id:
        jh.reset()
        print(benchmark_header%(int(e["scenario_id"]), e["test_name"]))
        # print("<trace - reset jh>")
    #print(e["row_id"]) #, e["histogram_rt"])
    ht = e["histogram_rt"]
    for key in ht.keys():
        jh.recordValueWithCount(int(key), ht[key])
    #print(jh.toString())
    #print("   ", e["avg_rt"])
    #print("   ", jh.getMean()/1000.0)
    #print("   ", e["max_rt"])
    #print("   ", jh.getMaxValue()/1000.0)
    print(benchmark_detail_row%( \
        e["row_id"],
        e["duration"],
        e["num_actions"],
        e["num_success"],
        e["num_failed"],
        e["avg_tps"],
        e["avg_rt"],
        e["percentiles_rt"]["90.0"],
        e["percentiles_rt"]["95.0"],
        e["percentiles_rt"]["99.0"],
        e["max_rt"],
        e["max_rt_ts"].replace("_", " ")
        ))

print(trailer)