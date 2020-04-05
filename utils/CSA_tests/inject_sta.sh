#!/usr/bin/expect -f
set timeout 10
spawn hostapd_cli
sleep .1
expect "> "
sleep .1
send "raw ADD bss_config=wlp9s0:/home/juan/Documents/vAP-SDN/hostapd-2.7/hostapd/ap_config/bss1.conf\r"
sleep .1
expect "> "
sleep .1
send "interface wlan1\r"
sleep .1
send "new_sta b8:27:eb:b1:83:2d\r"
sleep .1
puts "Fim do teste\r"
