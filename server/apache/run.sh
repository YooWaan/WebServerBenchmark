#!/bin/sh

mpm_mode=$1
sig=$2

if [ "${mpm_mode}" = "" ]; then
	mpm_mode=prefork
fi

if [ "${sig}" = "" ]; then
	sig=start
fi

# remove log files
for f in `ls ./var/log/`; do
	if test "$sig" = "start"; then
		echo remove $f
		rm ./var/log/$f
	fi
done

APACHE_HOME=/home/develop/lib/share/apache
APACHE_HTTPD=${APACHE_HOME}/${mpm_mode}/bin/httpd

export APACHE_CONFDIR=`pwd`/conf
APACHE_CONF_FILE=httpd-${mpm_mode}.conf
APACHE_ENVVARS=./conf/envvars

if test -f $APACHE_ENVVARS; then
	. $APACHE_ENVVARS
fi

${APACHE_HTTPD} -d ${APACHE_CONFDIR} -f ${APACHE_CONF_FILE} -k $sig
#${APACHE_HTTPD} -d ${APACHE_CONFDIR} -f ${APACHE_CONF_FILE} -t

ERROR=$?
if [ "$ERROR" != 0 ] ; then
	echo Action \'"$@"\' failed.
	echo The Apache error log may have more information.
fi

exit $ERROR
# E O F