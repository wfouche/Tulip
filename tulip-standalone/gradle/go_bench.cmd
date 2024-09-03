call ..\..\gradlew.bat  run --args="--config benchmark_config.jsonc"
@echo off
echo.
..\..\platform\msys64\bin\w3m.exe -dump -cols 200 benchmark_report.html