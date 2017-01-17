#!/bin/bash

USAGE_MSG="$0 {start|stop|restart} {crawl|web} {work|home}(config file suffix)"

if [ $# != 3 ]; then
        echo $USAGE_MSG 2>&1
        exit 1
fi

PID_DIR="pids"

if [ ! -d $PID_DIR ]
then
        mkdir $PID_DIR
fi

PID_FILE="$PID_DIR/$2.pid"

case $1 in
start)
        cp config/config.$3.xml config/config.xml
        mvn clean compile
        if [ $2 = "web" ]
        then
                nohup mvn spring-boot:run 2>&1 > /dev/null &
        elif [ $2 = "crawl" ]
        then
                JVM_ARGS = "-Xmx2048m -XX:PermSize=256M -XX:MaxPermSize=512M"
                nohup mvn exec:exec -Dexec.executable="java" -Dexec.args="$JVM_ARGS -cp %classpath com.wpf.realestate.server.CrawlServer" 2>&1 > /dev/null &
        else
                rm config/config.xml
                echo $USAGE_MSG 2>&1
                exit 1
        fi

        if [ $? = 0 ]
        then
if echo -n $! > $PID_FILE
                then
                        sleep 1
                        echo "$2 server started pid $!"
                else
                        echo "failed to write pid $!"
                        exit 1
                fi
        else
                echo "$2 server did not start ret code $?"
                exit 1
        fi
        ;;
stop)
        echo "stopping $2 server...."
        if [ ! -f $PID_FILE ]
        then
                echo "could not find pid file"
        else
                TPPID="$(cat $PID_FILE)"
                kill -9 $TPPID
                rm $PID_FILE
                echo "$2 stopped"
        fi
        exit 0
        ;;
restart)
        shift
        "$0" stop ${@}
        sleep
        "$0" start ${@}
        ;;
*)
        echo $USAGE_MSG 2>&1
        exit 1

esac