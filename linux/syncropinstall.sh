#!/bin/bash
#
# Installs files into /usr/share/syncrop/
#
STARTPWD=$PWD
if [[ $EUID -ne 0 ]] 
	then echo 'Must be run as root! Try again with sudo !!'
	exit 1
fi
if [[ ! -d /usr/share/syncrop ]]
	then mkdir /usr/share/syncrop
fi
cd /usr/share/syncrop
if [[ ! "$PWD" == "/usr/share/syncrop" ]]
	then echo 'Unable to change directory! Aborting!'
	exit 1
fi

if [[ -f './SyncropCMD.jar' ]]; then rm './SyncropCMD.jar'; fi; 
wget -q --tries=3 ropmc.no-ip.info:50005/syncrop/SyncropCMD.jar
if [[ ! "$?" == "0" ]]
	then echo 'Unable to download SyncropCMD.jar! Aborting!'
	exit 1
fi
chmod 755 SyncropCMD.jar

if [[ -f './CLI.jar' ]]; then rm './CLI.jar'; fi; 
if [[ -f './Syncrop.jar' ]]; then rm './Syncrop.jar'; fi; 
wget -q --tries=3 ropmc.no-ip.info:50005/syncrop/Syncrop.jar
if [[ ! "$?" == "0" ]]
	then echo 'Unable to download Syncrop.jar! Aborting!'
	exit 1
fi
chmod 755 Syncrop.jar

if [[ -f './Client.jar' ]]; then rm './Client.jar'; fi; 
wget -q --tries=3 ropmc.no-ip.info:50005/syncrop/Client.jar
if [[ ! "$?" == "0" ]]
	then echo 'Unable to download Client.jar! Aborting!'
	exit 1
fi
chmod 755 Client.jar



cd /usr/bin
if [[ ! $PWD == "/usr/bin" ]]
	then echo 'Unable to change directory! Aborting!'
	exit 1
fi
if [[ -f './syncrop' ]]; then rm './syncrop'; fi;
wget -q --tries=3 ropmc.no-ip.info:50005/syncrop/linux/syncrop
if [[ ! "$?" == "0" ]]
	then echo 'Unable to download syncrop! Aborting!'
	exit 1
fi
chmod 755 syncrop
if [[ -f './updateSyncrop' ]]; then rm './updateSyncrop'; fi; 
wget -q --tries=3 ropmc.no-ip.info:50005/syncrop/linux/updateSyncrop
if [[ ! "$?" == "0" ]]
	then echo 'Unable to download updateSyncrop! Aborting!'
	exit 1
fi
chmod 755 updateSyncrop

cd $STARTPWD
echo 'Successfully installed Syncrop! Run command `syncrop help` to begin!'
exit 0
