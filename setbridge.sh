# $1 is the ethernet port, $2 is the configuration IP Address
if [[ $1 == up ]]; then
	if [ "$#" -ne 4 ]; then
		echo "Incorrect number of parameters."
		echo "Usage: setbridge up <ethernet_iface> <bridge_ip> <bridge_mac>"
	else
		echo "Setting bridge up"
		sudo brctl addbr vapbridge
		sudo brctl addif vapbridge $2
		sudo ifconfig vapbridge hw ether $4
		sudo ifconfig vapbridge $3
		sudo ifconfig vapbridge up
		brctl show
	fi
elif [[ $1 == down ]]; then
	echo "Setting bridge down"
	sudo ifconfig vapbridge down
	sudo brctl delbr vapbridge
	sudo service NetworkManager restart
else
	echo "USAGE:"
	echo "setbridge up <ethernet_iface> <bridge_ip> <bridge_mac> OR"
	echo "setbridge down"
fi
