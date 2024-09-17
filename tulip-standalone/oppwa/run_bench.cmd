call .\gradlew -q run --args="--config benchmark_config.jsonc"

..\..\platform\msys64\bin\w3m.exe -dump -cols 200 benchmark_report.html