@echo off
echo.
pushd .\tulip-app
..\platform\msys64\bin\w3m.exe -dump -cols 205 benchmark_report.html
popd
