# vAP-SDN

**TODO**
- [ ] Carregar informação de uma STA em tempo de execução sem associação
- [ ] Anunciar CSA sem trocar realmente de canal
- [ ] Comando channel_switch atua em todos os BSSs
- [ ] Ler informações de uma STA associada
- [ ] Forjar RSSI para evitar desassociação do cliente

**Configuraçoes para usar uma interface sem fio no HostAPD**
- Setar MAC da placa como nao gerenciado em /etc/NetworkManager/NetworkManager.conf

  > unmanaged-devices=mac:XX:XX:XX:XX:XX:XX
- Reiniciar serviço NetworkManager

  > sudo service NetworkManager restart
- Usar DNSmasq para fornecer um IP para os hosts

  > TODO
  
  > Necessário mudar a porta que está sendo utilizada pelo serviced.resolv
  
**Número máximo de APs para cada dispositivo**
- Raspberry Pi - Broadcom BCM43438 - **1**
- Laptop Acer - Qualcomm Atheros AR9485 Wireless Network Adapter (rev 01) - **8**
- Adaptador TP-Link TL-WN722N - Qualcomm Atheros AR9271 - **2** - **Obs.: Comportamento incorreto do CSA com 2 APs simultâneos**
- AirPcap NX - Qualcomm Atheros AR9001U - **2** -  **Obs.: CSA não suportado**

**Compilando HostAPD**
- Instalar dependências
  > sudo apt-get install libnl-3-dev

  > sudo apt-get install libnl-genl-3-dev
- Ir até a pasta hostapd
- Copiar **defconfig** como **.config**
- Executar comando make

**Inicialização do HostAPD**
- Comando de inicialização

  ```sudo hostapd/hostapd -b hostapd/ap_config/ap_configuration.conf ```

- Estrutura do arquivo de configuração
  ```
  interface=<wireless_phy_name>
  driver=nl80211
  ssid=<SSID>
  channel=<channel_number>

  ctrl_interface=/var/run/hostapd
  ctrl_interface_group=0

  bss=<bss_name>
  bssid=<BSSID_MAC>
  ssid=<BSS_SSID>

  ctrl_interface=/var/run/hostapd
  ctrl_interface_group=0
  ```

  Exemplo:
  
  ```
  interface=wlp9s0
  driver=nl80211
  ssid=TESTE_HOSTAPD_1
  channel=1

  ctrl_interface=/var/run/hostapd
  ctrl_interface_group=0

  bss=wlan1
  bssid=2c:d0:5a:42:73:14
  ssid=TESTE_HOSTAPD_2

  ctrl_interface=/var/run/hostapd
  ctrl_interface_group=0
  ```

**Adição / Remoção de BSSs em tempo de execução**
- Inicialização do HostAPD com GCI e BSS inicial

  ```sudo hostapd/hostapd -b <phy_interface>:<path_to_initial_bss_conf> -g /var/run/hostapd/gci```
  
  Exemplo: ```sudo hostapd/hostapd -b wlp9s0:hostapd/ap_config/initial.conf -g /var/run/hostapd/gci```
- Comando de Adição de BSS no GCI:

  ``` raw ADD bss_config=<phy_interface>:<path_to_bss_conf> ```
  
  Exemplo: 
  ``` raw ADD bss_config=wlp9s0:/home/juan/Documents/vAP-SDN/hostapd-2.7/hostapd/ap_config/bss1.conf ```
- Comando de Remoção de BSS no GCI:

  ``` raw REMOVE <bss_if_name>```
  
  Exemplo: 
  ``` raw REMOVE wlan1 ```
- Estrutura do arquivo de configuração bss1.conf
  ```
  interface=<bss_if_name>
  ssid=<SSID>
  bssid=<BSSID_MAC>
  ctrl_interface=/var/run/hostapd
  ctrl_interface_group=0
  ```
  
  Exemplo:
  ```
  interface=wlan1
  ssid=BSS_TEST_1
  bssid=2c:d0:5a:42:73:21
  ctrl_interface=/var/run/hostapd
  ctrl_interface_group=0
  ```

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
