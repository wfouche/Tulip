call .\gradlew.bat -q run --args="--config benchmark_config.json"
@echo off
echo.
..\..\platform\msys64\bin\w3m.exe -dump -cols 205 benchmark_report.html