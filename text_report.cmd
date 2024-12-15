@echo off
echo.
pushd .\tulip-main
..\platform\msys64\bin\w3m.exe -dump -cols 205 benchmark_report.html
popd
