WIFI_IFACE=""
WIRED_IFACE=""
AP_MANAGEMENT_ADDR=""
PORT_NUMBER=9000
FILE_PATH=""
CHANNEL="1"
managedByNM=0


about()
{
	echo "---------------------------"
	printf "\tVAP-SDN AP\n"
	echo "---------------------------"
	printf "This software extends HostAPD 2.7 funcionalities to implement a data-plane of the VAP-SDN solution.\n"
	printf "\nHostAPD 2.7 was developed by Jouni Malinen and his contributors:\n"
	printf "https://w1.fi/hostapd/\n"
	printf "\nExtensions were developed by Juan Lucas Vieira.\n"
	printf "For more information regarding VAP-SDN, visit:\n"
	printf "https://github.com/juanlucasvieira/VAP-SDN\n"

}

usage()
{
    printf "\nUSAGE: run [options] -w <wifi-iface> -e <ether-iface> -ip <ap-management-addr>\n"
	printf "\nREQUIRED:\n"
	printf "[-w | --wifi-iface] - wireless interface name\n"
	printf "[-e | --ether-iface] - ethernet interface name\n"
	printf "[-ip | --ap-ip-addr] - IP address for management of the AP\n"
	printf "\nOPTIONS:\n"
	printf "[-f | --file] - hostapd configuration file path\n"
	printf "[-c | --channel] - Set AP channel. (Default: 1)\n"
	printf "[-p | --ap-port] - AP management port (Default: 9000)\n"
}

checkNumber()
{
	re='^[0-9]+$'
	if ! [[ $1 =~ $re ]] ; then
	   echo "Error: $1 is not a number" >&2; exit 1
	fi
}

checkIpAddr(){
    if [[ $1 =~ ^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$ ]]; then
        OIFS=$IFS
        IFS='.'
        ip=($1)
        IFS=$OIFS
        if [[ ${ip[0]} -gt 255 || ${ip[1]} -gt 255 || ${ip[2]} -gt 255 || ${ip[3]} -gt 255 ]]; then
			echo "Error: Given IP Address is not valid" >&2
			exit 1
	fi
    else
			echo "Error: Given IP Address is not valid" >&2
			exit 1
    fi
}

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
				checkNumber $1
				CHANNEL=$1
                                ;;
	-ip | --ap-ip-addr )    shift
				checkIpAddr $1
				AP_MANAGEMENT_ADDR=$1
                                ;;
	-p | --ap-port )        shift
				checkNumber $1
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

#Verify if WiFi interface exists
result=$(ifconfig $WIFI_IFACE 2> >( grep ""))
if [[ "$result" == *"Device not found"* ]]; then
  echo "$WIFI_IFACE not found!"
  exit 1
fi

#Verify if Wired interface exists
result=$(ifconfig $WIRED_IFACE 2> >( grep ""))
if [[ "$result" == *"Device not found"* ]]; then
  echo "$WIRED_IFACE not found!"
  exit 1
fi

#Check if NetworkManager is managing the wifi-iface
result=$(nmcli dev | grep $WIFI_IFACE)
if [[ "$result" == *"managed"* ]] || [[ "$result" == *"connected"* ]];then
  managedByNM=1
  echo "Disabling NetworkManager interface management..."
  sudo nmcli device set $WIFI_IFACE managed no
  sudo service NetworkManager restart
fi

if [ -z "$FILE_PATH" ]
then
	echo "File Path not set. Creating configuration file..."
	if [ -e $FILE_PATH ]; then
		rm -f hostapd/ap_config/startup.conf
	fi
	mkdir -p hostapd/ap_config/
	touch hostapd/ap_config/startup.conf
	FILE_PATH=hostapd/ap_config/startup.conf
	cat > hostapd/ap_config/startup.conf<<EOF
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

#Get MAC_ADDR of WIRED_IFACE
MAC_ADDR=$(ifconfig $WIRED_IFACE | awk '$1 == "ether" {print $2}')
#echo "mac: $MAC_ADDR"
echo "Setting bridge"
sudo brctl addbr vapbridge
sudo brctl addif vapbridge $WIRED_IFACE
sudo ifconfig vapbridge hw ether $MAC_ADDR
sudo ifconfig vapbridge $AP_MANAGEMENT_ADDR
sudo ifconfig vapbridge up
brctl show



echo "Starting HostAPD..."
sudo hostapd/hostapd -b $WIFI_IFACE:$FILE_PATH -g udp:$PORT_NUMBER

echo "Setting bridge down..."
sudo ifconfig vapbridge down
sudo brctl delif vapbridge $WIRED_IFACE
sudo brctl delbr vapbridge

if [[ $managedByNM == 1 ]]; then
  echo "Reenabling NetworkManager interface management..."
  sudo nmcli device set $WIFI_IFACE managed yes
  sudo service NetworkManager restart
fi

echo "Restarting Network Service..."

sudo /etc/init.d/networking restart
sudo systemctl restart networking

echo "Stopped."


