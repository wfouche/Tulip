import json
import copy

d = []

e =  {
   "id": 0,
   "name": "Test0",
   "users": [
      {
        "id": 0,
        "name": "User0",
          "results":[
              {"id": 0, "avg_tps": 0.1, "avg_rt": 12.5, "std_rt": 75.0, "min_rt": 1.0, "max_rt": 1.0, "max_rt_ts":"2019-01-10T10:12:23", "percentiles": [(90.0,10.4), (99.9,45.0)]},
              {"id": 1, "avg_tps": 0.1, "avg_rt": 12.5, "std_rt": 75.0, "min_rt": 1.0, "max_rt": 1.0, "max_rt_ts":"2019-01-10T10:12:23", "percentiles": [(90.0,10.4), (99.9,45.0)]},
              {"id": 2, "avg_tps": 0.1, "avg_rt": 12.5, "std_rt": 75.0, "min_rt": 1.0, "max_rt": 1.0, "max_rt_ts":"2019-01-10T10:12:23", "percentiles": [(90.0,10.4), (99.9,45.0)]},
          ]
      },
      {
          "id": 1,
          "name": "User1",
          "results":[
              {"id": 0, "avg_tps": 0.1, "avg_rt": 12.5, "std_rt": 75.0, "min_rt": 1.0, "max_rt": 1.0, "max_rt_ts":"2019-01-10T10:12:23", "percentiles": [(90.0,10.4), (99.9,45.0)]},
              {"id": 1, "avg_tps": 0.1, "avg_rt": 12.5, "std_rt": 75.0, "min_rt": 1.0, "max_rt": 1.0, "max_rt_ts":"2019-01-10T10:12:23", "percentiles": [(90.0,10.4), (99.9,45.0)]},
              {"id": 2, "avg_tps": 0.1, "avg_rt": 12.5, "std_rt": 75.0, "min_rt": 1.0, "max_rt": 1.0, "max_rt_ts":"2019-01-10T10:12:23", "percentiles": [(90.0,10.4), (99.9,45.0)]},
          ]
      }
   ]
}
d.append(e)

e = copy.deepcopy(e)
e["name"] = "Test1"
d.append(e)

e = copy.deepcopy(e)
e["name"] = "Test2"
d.append(e)

for test in d:
    print(test["name"])
    for user in test["users"]:
        print(" " + user["name"])
        for result in user["results"]:
            #print("  " + str(result))
            print("  " + json.dumps(result))