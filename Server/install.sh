#!/bin/bash

#Install requirements for Suricata
apt-get install -y libpcre3 libpcre3-dbg libpcre3-dev \
build-essential autoconf automake libtool libpcap-dev libnet1-dev \
libyaml-0-2 libyaml-dev zlib1g zlib1g-dev libmagic-dev libcap-ng-dev \
libjansson-dev pkg-config

#Install additional requirements for IPS
apt-get install -y libnetfilter-queue-dev

#Install Suricata
cd /tmp
wget downloads.suricata-ids.org/suricata-current.tar.gz
tar -xzf suricata-current.tar.gz
cd /tmp/suricata-*
./configure --prefix=/usr --sysconfdir=/etc --localstatedir=/var && make && make install-full
ldconfig

#Install Oinkmaster
apt-get install -y oinkmaster
echo url = http://rules.emergingthreats.net/open/suricata/emerging.rules.tar.gz >> /etc/oinkmaster.conf

#Update newest detection rules
oinkmaster -C /etc/oinkmaster.conf -o /etc/suricata/rules

#Install Pyrebase and requirements for Pyrebase
pip install pyrebase pyasn1-modules rsa jws

#Remove Suricata installer
rm -rf /tmp/suricata-*

#Print Suricata version
suricata -V