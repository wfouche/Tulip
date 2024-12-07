./gradlew run --args="--config=benchmark_config.json"
echo ""
w3m -dump -cols 200 benchmark_report.html
#lynx -dump -width 200 app/benchmark_report.html
