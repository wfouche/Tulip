del benchmark_report.html
set JBANG_JAVA_OPTIONS=-server -Xmx1024m -XX:+UseZGC -XX:+ZGenerational
call jbang run App.java
@echo off
echo.
REM call w3m.exe -dump -cols 205 benchmark_report.html
start benchmark_report.html
jbang run https://gist.github.com/wfouche/70738de122128bbc19ea888799151699 benchmark_config.adoc
start benchmark_config.html
