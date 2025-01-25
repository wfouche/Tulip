REM kwrk
if exist benchmark_report.html del benchmark_report.html
set JBANG_JAVA_OPTIONS=-server -Xms2g -Xmx2g -XX:+UseZGC -XX:+ZGenerational
jbang run kwrk.kt --rate 10.0 --threads 2 --duration 30,s --repeat 4 --url https://jsonplaceholder.typicode.com/posts/1
@echo off
echo.
REM w3m.exe -dump -cols 205 benchmark_report.html
REM lynx.exe -dump -width 205 benchmark_report.html
start benchmark_report.html
REM jbang run https://gist.github.com/wfouche/70738de122128bbc19ea888799151699 benchmark_config.adoc
REM start benchmark_config.html

