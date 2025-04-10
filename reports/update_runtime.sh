python3 jython/jython-cli.py report.py
python3 jython/jython-cli.py report2.py

cat package.txt report_py.java > report.txt
cp   report.txt report_py.java

cat package.txt report2_py.java > report.txt
cp   report.txt report2_py.java

rm report.txt

cp report_py.java  ../tulip-runtime/src/main/java/io/github/wfouche/tulip/report/
cp report2_py.java ../tulip-runtime/src/main/java/io/github/wfouche/tulip/report/
