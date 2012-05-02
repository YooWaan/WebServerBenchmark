#!/bin/sh

export SERVER_HOME=`pwd`
export LIGHTTPD_HOME=/home/develop/lib/share/lighttpd
LIGHTTPD_RUN=${LIGHTTPD_HOME}/sbin/lighttpd

echo $SERVER_HOME
${LIGHTTPD_RUN} -D -f conf/lighttpd.conf
#${LIGHTTPD_RUN} -p -f conf/lighttpd.conf

# E O F