# Description: Initializes a development environment on Windows using Scoop.
# Prerequisites: PowerShell, Windows OS
# Note: First install http://scoop.sh by running the following commands in PowerShell:
#
#> Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
#> Invoke-RestMethod -Uri https://get.scoop.sh | Invoke-Expression

scoop install git

scoop bucket add extras
scoop bucket add java

scoop install microsoft21-jdk
scoop install jbang
scoop install curl
scoop install zip
scoop install unzip

scoop install scoop-search
scoop install UniGetUI

scoop install scala-cli
scoop install extras/vcredist2022

bash -c "curl -s https://get.sdkman.io | bash"
