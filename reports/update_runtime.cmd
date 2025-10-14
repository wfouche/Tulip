REM python3 jython/jython-cli.py report.py
call jbang run python_jvm_tulip.java --keep-java report.py
REM python3 jython/jython-cli.py report2.py
call jbang run python_jvm_tulip.java --keep-java report2.py

REM copy package.txt+report_py.java report.txt
REM copy report.txt report_py.java
call jbang run FileModifier.java report_py.java

REM copy package.txt+report2_py.java report.txt
REM copy report.txt report2_py.java
call jbang run FileModifier.java report2_py.java

REM del report.txt

copy report_py.java  ..\tulip-runtime\src\main\java\io\github\wfouche\tulip\report
copy report2_py.java ..\tulip-runtime\src\main\java\io\github\wfouche\tulip\report
