echo ""
pushd ./tulip-app
echo ""
#lynx -dump -width 200 benchmark_report.html
w3m -dump -cols 200 benchmark_report.html
popd
