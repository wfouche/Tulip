rm -f ./pkl
curl -L -o pkl https://github.com/apple/pkl/releases/download/0.25.3/pkl-linux-amd64
chmod +x pkl
./pkl --version
chmod -w ./pkl
rm -f /usr/local/bin/pkl
sudo cp ./pkl /usr/local/bin/pkl
