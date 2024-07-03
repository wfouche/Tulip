echo ""
pushd ./reports
./report.sh
echo ""
w3m -dump -cols 200 report.html
popd
