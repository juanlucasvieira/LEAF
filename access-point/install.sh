sudo apt-get update
sudo apt-get -y install libnl-3-dev libnl-genl-3-dev libssl-dev make net-tools bridge-utils pkg-config
(cd hostapd/ && make)

