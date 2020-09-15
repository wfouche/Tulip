import json
import fileinput

i = 0
for line in fileinput.input():
    i += 1
    print("")
    print("-------------------------- %02d ------------------------------"%(i))
    print("")
    print(json.dumps(json.loads(line),indent=2))