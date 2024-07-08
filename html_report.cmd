@echo off
echo.
pushd .\reports
call .\report.cmd
.\report.html
popd
