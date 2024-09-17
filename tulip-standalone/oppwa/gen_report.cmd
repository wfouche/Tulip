call .\gradlew -q run --args="--result benchmark_output.json"

..\..\platform\msys64\bin\w3m.exe -dump -cols 200 benchmark_report.html