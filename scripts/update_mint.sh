#sudo systemctl daemon-reload

sudo apt update -y
sudo apt upgrade -y
sudo apt autoremove -y
sudo snap refresh

# snap refresh intellij-idea-community  --channel=2024.1/stable
# snap refresh intellij-idea-community  --channel=latest/stable
# snap refresh intellij-idea-community  --channel=latest/candidate
# snap refresh intellij-idea-community  --channel=latest/edge
#
# sudo snap install intellij-idea-community --classic
# sudo snap install intellij-idea-ultimate --classic

# install packages that are not upgrading due to phasing
sudo apt install ?upgradable