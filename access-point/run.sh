WIFI_IFACE=""
WIRED_IFACE=""
AP_MANAGEMENT_ADDR=""
PORT_NUMBER=9000
FILE_PATH=""
CHANNEL="1"
managedByNM=0

while [ "$1" != "" ]; do
    case $1 in
        -f | --file )           shift
                                FILE_PATH=$1
                                ;;
        -e | --ether-iface )    shift
								WIRED_IFACE=$1
                                ;;
		-w | --wifi-iface )     shift
								WIFI_IFACE=$1
                                ;;
		-c | --channel )        shift
								checkNumber($1)
								CHANNEL=$1
                                ;;
		-ip | --ap-ip-addr )    shift
								checkIpAddr($1)
								AP_MANAGEMENT_ADDR=$1
                                ;;
		-p | --ap-port )        shift
								checkNumber($1)
								PORT_NUMBER=$1
                                ;;
        -h | --help )           about
								usage
                                exit
                                ;;
        * )                     usage
                                exit 1
    esac
    shift
done

if [ -z "$WIFI_IFACE" ]
then
	echo "Error: WIRELESS interface not defined"
	echo "Execute with -h for help"
	exit 1
fi
if [ -z "$WIRED_IFACE" ]
then
	echo "Error: WIRED interface not defined"
	echo "Execute with -h for help"
	exit 1
fi
if [ -z "$AP_MANAGEMENT_ADDR" ]
then
	echo "Error: IP address not defined"
	echo "Execute with -h for help"
	exit 1
fi

if [ -z "$FILE_PATH" ]
then
	echo "File Path not set. Creating configuration file."
	if [ -e $FILE_PATH ]; then
		rm -f hostapd/ap_config/initial.conf
	fi
	mkdir hostapd/ap_config/
	touch hostapd/ap_config/initial.conf
	FILE_PATH=hostapd/ap_config/initial.conf
	cat > hostapd/ap_config/initial.conf<<EOF
	interface=$WIFI_IFACE
	bridge=vapbridge
	driver=nl80211
	ssid=INITIAL_VAP
	channel=$CHANNEL
	hw_mode=g

	no_probe_resp_if_max_sta=1
	broadcast_deauth=0

	ctrl_interface=udp:$((PORT_NUMBER+1))
	ctrl_interface_group=0
EOF
	
	
fi

#Check if NetworkManager is managing the wifi-iface
result=$(nmcli dev | grep $WIFI_IFACE)
if [[ $result == *" managed"* ]]; then
  managedByNM=1
  echo "Disabling NetworkManager interface management"
  sudo nmcli device set $WIFI_IFACE managed no
  sudo service NetworkManager restart
fi

#Verify if WiFi interface exists
result=$(ifconfig $WIFI_IFACE)
if [[ $result == *"Device not found"* ]]; then
  echo "$WIFI_IFACE not found!"
  exit 1
fi

#Verify if Wired interface exists
result=$(ifconfig $WIRED_IFACE)
if [[ $result == *"Device not found"* ]]; then
  echo "$WIRED_IFACE not found!"
  exit 1
fi

#Get MAC_ADDR of WIRED_IFACE
MAC_ADDR=$(ifconfig $WIRED_IFACE | awk '$1 == "ether" {print $2}')

echo "Setting bridge"
sudo brctl addbr vapbridge
sudo brctl addif vapbridge $WIRED_IFACE
sudo ifconfig vapbridge hw ether $MAC_ADDR
sudo ifconfig vapbridge $AP_MANAGEMENT_ADDR
sudo ifconfig vapbridge up
brctl show



echo "Starting HostAPD"
sudo hostapd/hostapd -b $WIFI_IFACE:$FILE_PATH -g udp:$PORT_NUMBER

echo "Setting bridge down"
sudo ifconfig vapbridge down
sudo brctl delif vapbridge $WIRED_IFACE
sudo brctl delbr vapbridge

if [[ $managedByNM == 1 ]]; then
  echo "Reenabling NetworkManager interface management"
  sudo nmcli device set $WIFI_IFACE managed yes
  sudo service NetworkManager restart
fi

echo "Restarting Network Service"

sudo /etc/init.d/networking restart
sudo systemctl restart networking

usage()
{
    echo "USAGE: run [options] -w <wifi-iface> -e <ether-iface> -ip <ap-management-addr>"
	echo "REQUIRED:"
	echo "[-w | --wifi-iface] - wireless interface name"
	echo "[-e | --ether-iface] - ethernet interface name"
	echo "[-ip | --ap-ip-addr] - IP address for management of the AP"
	echo "OPTIONS:"
	echo "[-f | --file] - hostapd configuration file path"
	echo "[-c | --channel] - Set AP channel. (Default: 1)"
	echo "[-p | --ap-port] - AP management port (Default: 9000)"
}

about()
{
	echo "-------------"
	echo " VAP-SDN AP"
	echo "-------------"
	echo "This software extends HostAPD 2.7 funcionalities to implement a data-plane of the VAP-SDN solution."
	echo "Extensions were developed by Juan Lucas Vieira"
	echo "For more information regarding VAP-SDN, visit:"
	echo "https://github.com/juanlucasvieira/VAP-SDN"
	echo "HostAPD 2.7 was developed by Jouni Malinen and his contributors:"
	echo "https://w1.fi/hostapd/"
}

checkNumber(value)
{
	re='^[0-9]+$'
	if ! [[ $value =~ $re ]] ; then
	   echo "Error: $value is not a number" >&2; exit 1
	fi
}

checkIpAddr(value){
    if [[ $ip =~ ^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$ ]]; then
        OIFS=$IFS
        IFS='.'
        ip=($ip)
        IFS=$OIFS
        if [[ ${ip[0]} -gt 255 && ${ip[1]} -gt 255 && ${ip[2]} -gt 255 && ${ip[3]} -gt 255 ]]
			echo "Error: Given IP Address is not valid" >&2
			exit 1
		fi
    fi
}