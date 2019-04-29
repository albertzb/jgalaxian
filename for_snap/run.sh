#!/bin/bash


export JAVA_HOME=$SNAP/usr/lib/jvm/java-8-openjdk-amd64
export PATH=$JAVA_HOME/jre/bin:$PATH

# Not good, needed for fontconfig
export XDG_DATA_HOME=$SNAP/usr/share
# Font Config
export FONTCONFIG_PATH=$SNAP/etc/fonts/config.d
export FONTCONFIG_FILE=$SNAP/etc/fonts/fonts.conf

#ARCH=x86_64-linux-gnu
#export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$SNAP/usr/lib/$ARCH/pulseaudio

SCALE=1.5
if [[ "$1" != "" ]] ; then
  SCALE=$1
fi

java -Djava.util.prefs.userRoot="$SNAP_USER_DATA" -jar $SNAP/jar/jgalaxian-1.0-SNAPSHOT.jar $SCALE
