import json
js="""
{
    "json_filename": "json_results.txt",
    "contexts": [
        {
            "name":"Screnario-1",
            "num_users":"16",
            "num_threads": "2"
        },
        {
            "name":"Screnario-2",
            "num_users":"32",
            "num_threads": "4"
        }
    ],
    "tests": [
        {
            "enabled":  "true",
            "name":     "Test0 (Initialize)",
            "duration": {
                "startupDurationUnits": 1,
                "warmupDurationUnits": 1,
                "mainDurationUnits": 1,
                "mainDurationRepeatCount": 1,
                "timeUnit": 0
            },
            "arrivalRate": 0.0,
            "queueLengths": [-1],
            "actions":  [{"id": 0}, {"id": 7}]
        },
        {
        }
    ]
}
"""
pj = json.loads(js)
print(json.dumps(pj,indent=4))
