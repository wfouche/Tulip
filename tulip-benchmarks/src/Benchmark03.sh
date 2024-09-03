# Install Graal VM
#sdk install java 21.0.4-graal

# Add missing GCC libraries
#sudo apt-get install build-essential libz-dev zlib1g-dev

jbang --native --native-option=--no-fallback --native-option=--verbose Benchmark03.kt --config=./user/http/config.jsonc
