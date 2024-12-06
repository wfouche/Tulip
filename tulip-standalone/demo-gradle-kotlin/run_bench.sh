./gradlew run --args="--config benchmark_config.json"
echo ""
lynx -dump -width 200 app/benchmark_report.html
