@echo off
echo.
pushd .\tulip-main
lynx.exe -dump -width 205 benchmark_report.html
popd
