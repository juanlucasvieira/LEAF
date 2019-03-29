from scapy.layers.dot11 import Dot11, Dot11Elt, Dot11EltRates, Dot11Beacon, RadioTap
from sys import argv as args
from scapy.config import conf
from time import time


def beaconBaker(sender_mac = "00:01:02:03:04:05"):

    dst_mac = "ff:ff:ff:ff:ff:ff"

    dot11 = Dot11(type=0,subtype=8, addr1=dst_mac, addr2=sender_mac, addr3=sender_mac)
    beacon = Dot11Beacon(cap="ESS", timestamp=1)
    rates = Dot11EltRates(rates=[130, 132, 11, 22])
    ssid = Dot11Elt(ID="SSID", info="TESTE_SEM_FIO")
    ds_set = Dot11Elt(ID="DSset", info="\x03")
    tim = Dot11Elt(ID="TIM", info="\x00\x01\x00\x00")
    csa = Dot11Elt(ID=37, len=3, info="\x01\x01\x00")

    frame = RadioTap()/dot11/beacon/rates/ssid/ds_set/tim/csa

    return frame

def beaconSender(frame, limit, iface='wlp9s0'):
    limitless = False
    if limit == -1:
        limitless = True
    with conf.L2socket(iface=iface) as s:
        pckt_num = 0
        start = time()
        while limitless or pckt_num < limit :
            s.send(frame)
            pckt_num += 1
        print('Total {} seconds'.format(time() - start))
        print("{} beacons were sent".format(pckt_num))
        # for pkt in pkt_list:

def PacketHandler(packet) :
    if packet.haslayer(Dot11) :
        if packet.type == 0 and packet.subtype == 4: #Subtype 04 -> Probe Request
            if packet.addr2 not in ap_list:
                ap_list.append(packet.addr2)
                print("Access Point MAC: %s with SSID: %s " %(packet.addr2, packet.info))

if __name__ == '__main__':
    beacon = beaconBaker()
    beaconSender(beacon, 5000)

    #sniff(iface="wlp9s0",prn=phandle, store=0)