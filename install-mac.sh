#!/system/bin/sh
cp /data/nvram/APCFG/APRDEB/WIFI_tmp /data/nvram/APCFG/APRDEB/WIFI
sleep 7
HOST_NAME_SYS=`getprop persist.sys.hostname`
setprop net.hostname $HOST_NAME_SYS

