@echo off
pushd .\reports
call .\report.cmd
.\benchmark_report.html
popd
