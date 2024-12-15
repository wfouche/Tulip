echo ""
pushd ./tulip-main
echo ""
w3m -dump -cols 205 benchmark_report.html
#lynx -dump -width 205 benchmark_report.html
popd
