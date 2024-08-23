@echo off
echo.
pushd .\reports
call .\report.cmd
..\platform\msys64\bin\w3m.exe -dump -cols 200 benchmark_report.html
popd
