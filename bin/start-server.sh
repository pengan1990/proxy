#!/bin/sh

current_path=$(pwd)
# get the shell's father directory
case "$(uname)" in
    Linux)
        bin_abs_path=$(readlink -f $(dirname $0))
        ;;
    *)
        bin_abs_path=$(cd $(dirname $0); pwd)
        ;;
esac

base=${bin_abs_path}/..
conf=${base}/conf

export LANG=en_US.UTF-8
export BASE=$base

if [ -f $base/bin/proxy.pid ] ; then
    echo "found tracker.pid , please write stop-server.sh first." 2>&2
    exit 1
fi

if [ ! -d $base/logs  ] ; then
    mkdir -p $base/logs
    echo "mkdired $base/logs"
fi

## set java path
if [ -z "$JAVA" ] ; then
    JAVA=$(which java)
fi

if [ -z "$JAVA" ] ; then
    echo "cannot find a java jdk" 2>&2
    exit 1
fi


str=$(file $JAVA_HOME/bin/java | grep 64-bit)
if [ -n "$str" ] ; then
    JAVA_OPTS="-server -Xms40960m -Xmx40960m -Xmn10240m -XX:-UseAdaptiveSizePolicy -XX:MaxTenuringThreshold=15 -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:+HeapDumpOnOutOfMemoryError"
else
    JAVA_OPTS="-server -Xms1024m -Xmx1024m -XX:NewSize=256m -XX:MaxNewSize=256m -XX:MaxPermSize=128m"
fi

JAVA_OPTS=" $JAVA_OPTS -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8"
SERVER_OPTS="-DappName=proxy -Dconfig=$conf"


if [ -e $conf ]
then
    for i in $base/lib/*;
        do CLASSPATH=$i:"$CLASSPATH";
    done
    for i in $base/conf/*;
        do CLASSPATH=$i:"$CLASSPATH";
    done

    cd $bin_abs_path
    cd $base
    $JAVA $JAVA_OPTS $JAVA_DEBUG_OPT $SERVER_OPTS -classpath .:$CLASSPATH server.ServerStart 1>>$base/logs/console.log 2>&1 &
    echo $! > $base/bin/proxy.pid

    echo "ProxyServer started"
    cd $current_path
else
    echo "conf $conf is not exists!"
fi