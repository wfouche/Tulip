call .\gradlew -q run --args="--config benchmark_config.json"
@echo off
echo.
..\..\platform\msys64\bin\w3m.exe -dump -cols 200 app/benchmark_report.html