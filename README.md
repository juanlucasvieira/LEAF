# vAP-SDN

**Configuraçoes para usar uma interface sem fio no HostAPD**
- Setar MAC da placa como nao gerenciado em /etc/NetworkManager/NetworkManager.conf

  > unmanaged-devices=mac:XX:XX:XX:XX:XX:XX
- Reiniciar serviço NetworkManager

  > sudo service NetworkManager restart
- Usar DNSmasq para fornecer um IP para os hosts

  > TODO
