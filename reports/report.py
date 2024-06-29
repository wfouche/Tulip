from __future__ import print_function

import json
import sys
import org.HdrHistogram.Histogram as Histogram

jh = Histogram(30*1000*1000, 3)
filename = sys.argv[1]
fileObj = open(filename)
jb = json.load(fileObj)
prev_row_id = 0
for e in jb:
    current_row_id = int(e["row_id"])
    if current_row_id <= prev_row_id:
        jh.reset()
        print("<trace - reset jh>")
    print(e["row_id"]) #, e["histogram_rt"])
    ht = e["histogram_rt"]
    for key in ht.keys():
        jh.recordValueWithCount(int(key), ht[key])
    #print(jh.toString())
    print("   ", e["avg_rt"])
    print("   ", jh.getMean()/1000.0)
    print("   ", e["max_rt"])
    print("   ", jh.getMaxValue()/1000.0)
