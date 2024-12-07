echo ""
pushd ./tulip-app
echo ""
w3m -dump -cols 200 benchmark_report.html
#lynx -dump -width 200 benchmark_report.html
popd
