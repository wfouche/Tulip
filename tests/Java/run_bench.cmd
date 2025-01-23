REM jbang io.github.wfouche.tulip:tulip-runtime:2.1.4 Java
if exist benchmark_report.htmlgis del benchmark_report.html
set JBANG_JAVA_OPTIONS=-server -Xms2g -Xmx2g -XX:+UseZGC -XX:+ZGenerational
call jbang run App.java
@echo off
echo.
REM w3m.exe -dump -cols 205 benchmark_report.html
REM lynx.exe -dump -width 205 benchmark_report.html
start benchmark_report.html
REM jbang run asciidoc@wfouche benchmark_config.adoc
REM start benchmark_config.html
