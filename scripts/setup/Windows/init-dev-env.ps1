Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
Invoke-RestMethod -Uri https://get.scoop.sh | Invoke-Expression

scoop install git

scoop bucket add extras
scoop bucket add java

scoop install microsoft21-jdk
scoop install jbang
scoop install curl
scoop install zip
scoop install unzip

scoop install scoop-search
scoop install unigetui

bash -c "curl -s https://get.sdkman.io | bash"