./gradlew run --args="--config=oppwa_config.jsonc"
echo ""
w3m -dump -cols 205 app/oppwa_report.html
#lynx -dump -width 205 app/oppwa_report.html
