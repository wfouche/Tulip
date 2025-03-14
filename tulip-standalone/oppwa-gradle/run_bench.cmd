call .\gradlew -q run --args="--config oppwa_config.json"
@echo off
echo.
..\..\platform\msys64\bin\w3m.exe -dump -cols 205 app/oppwa_report.html
