java -Dpython.path=./report-jars/HdrHistogram-2.2.2.jar -jar ./report-jars/jython-standalone-2.7.3.jar  report.py ./benchmark_output.json 1 > benchmark_report.html
echo ""
w3m -dump -cols 200 benchmark_report.html
