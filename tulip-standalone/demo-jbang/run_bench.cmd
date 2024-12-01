set JBANG_JAVA_OPTIONS=-server -Xmx1024m -XX:+UseZGC -XX:+ZGenerational
call jbang run App.kt --config=benchmark_config.json
@echo off
echo.
call ..\..\platform\msys64\bin\w3m.exe -dump -cols 200 benchmark_report.html
call .\benchmark_report.html
