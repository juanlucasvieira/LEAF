#!/usr/bin/expect -f
set timeout 10
set max 10
set wait_time .1
set channel1 2437
set channel2 2412
set repeat 5
array set intervals {
    0 0 
    1 1 
    2 5 
    3 10 
    4 50 
    5 100 
    6 150 
    7 200 
    8 250
}
spawn hostapd_cli
sleep 1
expect "> "
send "get_config\r"
sleep .1
expect "> "
for {set i 0} {$i < [array size intervals]} {incr i 1} {
    for {set j 1} {$j <= $repeat} {incr j 1} {
        set count $intervals($i)
        set wait_time [expr .12 * $count]
        send "chan_switch $count $channel1\r"
        sleep .1
        if { $count <= 1 } {
            expect "AP-STA-CONNECTED"
            sleep 1
        } else {
            expect "AP-CSA-FINISHED"
            sleep .1
            puts "WT: $wait_time"
            sleep $wait_time
        }
        send "chan_switch $count $channel2\r"
        if { $count <= 1 } {
            expect "AP-STA-CONNECTED"
            sleep 1
        } else {
            expect "AP-CSA-FINISHED"
            sleep .1
            puts "WT: $wait_time"
            sleep $wait_time
        }
        
    }
}
sleep .1
puts "Fim do teste\r"