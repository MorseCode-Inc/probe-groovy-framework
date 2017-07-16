
#!/bin/bash

NM_ROOT="/opt/nimsoft"

PATH="$NM_ROOT/bin:$PATH"

U="administrator"
P="*********"

HUBS="
#Name|Address|Security|Status|License|Version|IP|Communication Mode|Port|Domain|Robot
"

ROBOT_CFG="$NM_ROOT/robot/robot.cfg"

HUB="$1"
if [ -z "$HUB" ]
then
        HUBROBOT=$(grep hubrobotname "$ROBOT_CFG" | sed -e "s/.* = //g")
        HUB=$(grep "hub = " "$ROBOT_CFG" | sed -e "s/.* = //g")
        shift
else
        HUB_INFO=$(echo "$HUBS" | grep "^$HUB|")
        if [ -z "$HUB_INFO" ]
        then
                HUBROBOT=$(grep hubrobotname "$ROBOT_CFG" | sed -e "s/.* = //g")
                HUB=$(grep "hub = " "$ROBOT_CFG" | sed -e "s/.* = //g")
        else
                shift
        fi
fi


# get the hub for the current robot
HUB_INFO=$(echo "$HUBS" | grep "^$HUB|")
echo "hub info: $HUB_INFO"

HUB_NAME="${HUB_INFO%%|*}"; HUB_INFO="${HUB_INFO#*\|}"
HUB_ADDRESS="${HUB_INFO%%|*}"; HUB_INFO="${HUB_INFO#*|}"
HUB_SECURITY="${HUB_INFO%%|*}"; HUB_INFO="${HUB_INFO#*|}"
HUB_STATUS="${HUB_INFO%%|*}"; HUB_INFO="${HUB_INFO#*|}"
HUB_LICENSE="${HUB_INFO%%|*}"; HUB_INFO="${HUB_INFO#*|}"
HUB_VERSION="${HUB_INFO%%|*}"; HUB_INFO="${HUB_INFO#*|}"
HUB_IP="${HUB_INFO%%|*}"; HUB_INFO="${HUB_INFO#*|}"
HUB_MODE="${HUB_INFO%%|*}"; HUB_INFO="${HUB_INFO#*|}"
HUB_PORT="${HUB_INFO%%|*}"; HUB_INFO="${HUB_INFO#*|}"
HUB_DOMAIN="${HUB_INFO%%|*}"; HUB_INFO="${HUB_INFO#*|}"
HUB_ROBOT="${HUB_INFO%%|*}"; HUB_INFO="${HUB_INFO#*|}"


pu -u "$U" -p "$P" "$HUB_ADDRESS" "$@"
#echo pu -u "$U" -p "$P" "$HUB_ADDRESS" "$@" >&2


exit
