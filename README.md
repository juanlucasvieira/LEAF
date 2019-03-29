# vAP-SDN

**Configuraçoes para usar uma interface sem fio no HostAPD**
- Setar MAC da placa como nao gerenciado em /etc/NetworkManager/NetworkManager.conf

  > unmanaged-devices=mac:XX:XX:XX:XX:XX:XX
- Reiniciar serviço NetworkManager

  > sudo service NetworkManager restart
- Usar DNSmasq para fornecer um IP para os hosts

  > TODO
  
**Número máximo de APs para cada dispositivo**
- Raspberry Pi - Broadcom BCM43438 - **1**
- Laptop Acer - Qualcomm Atheros AR9485 Wireless Network Adapter (rev 01) - **8**
- Adaptador TP-Link TL-WN722N - Qualcomm Atheros AR9271 - **2** - **Obs.: Comportamento incorreto do CSA com 2 APs simultâneos**
- AirPcap NX - Qualcomm Atheros AR9001U - **2** -  **Obs.: CSA não suportado**

**Compilando HostAPD**
- Ir até a pasta hostapd
- Copiar **defconfig** como **.config**
- Executar comando Make

**Testes de comportamento do CSA em diferentes dispositivos**

  Execução -> Ping - chan_switch 10 2412 - 2437

- Samsung P6800 - STA 18:3f:47:6f:b0:f9 - com timestamps
  > Behavior: Reassociação (Rápido [Sem timeout])

  > Modo Bloqueio do CSA: Bloqueia transmissão - chan_switch 100 2412

- Samsung P585M - STA 3c:a1:0d:83:ca:15 - com timestamps
  > Behavior: Reassociação
  
  > Modo Bloqueio do CSA: Sem Efeito

- Samsung G925F - STA e8:50:8b:65:c8:ab - com timestamps
  > Behavior: Sem reassociação (com possíveis timeouts do ping)
  
  > Modo Bloqueio do CSA: Sem Efeito

- LG E612F - STA 00:aa:70:5c:8f:85
  > Behavior: Reassociação com timeouts

- Moto G4 Play XT1600 - STA f4:f5:24:9e:bd:9b
  > Behavior: Reassociação com timeouts

- Raspberry Model B - Broadcom BCM43438 - STA b8:27:eb:b1:83:2d  - com timestamps
  > Behavior: Sem reassociação
  
  > Modo Bloqueio do CSA: Sem Efeito

- Laptop Dell - Qualcomm Atheros QCA6174 802.11ac Wireless Network Adapter (rev 32) - STA 40:49:0f:fe:40:77
  > Behavior: Sem reassociação
  
  > Modo Bloqueio do CSA: Bloqueia transmissão - chan_switch 100 2412
