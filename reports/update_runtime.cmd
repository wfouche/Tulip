REM python3 jython/jython-cli.py report.py
call jbang --fresh run python-jvm@wfouche --keep-java report.py
REM python3 jython/jython-cli.py report2.py
call jbang         run python-jvm@wfouche --keep-java report2.py

copy package.txt+report_py.java report.txt
copy report.txt report_py.java

copy package.txt+report2_py.java report.txt
copy report.txt report2_py.java

del report.txt

copy report_py.java  ..\tulip-runtime\src\main\java\io\github\wfouche\tulip\report
copy report2_py.java ..\tulip-runtime\src\main\java\io\github\wfouche\tulip\report
