#!/bin/bash

MAX_DISK_TOTAL="200000"
WARN_MB="5000"
ERROR_MB="4000"

DOMAIN="UIM"
THISDIR="${0%/*}"
NM_ROOT="/opt/nimsoft"
PATH="$NM_ROOT/bin:$PATH"
U="administrator"
P="*******"

cd "$THISDIR"

LOOK_FOR_DISKS="
C:
D:
E:
F:
/
/var
/opt
/tmp
/var/log
/home
/usr
/usr/local
"

for HUB in $(./list_hubs.sh)
do
        ROBOTS=$(./list_robots.sh "$HUB")

        echo "$ROBOTS"| while read ROBOT_NAME
        do

                [ -z "$ROBOT_NAME" ] && continue;

                ROBOT_ADDRESS="$DOMAIN/$HUB/$ROBOT_NAME"
                CDM_ADDRESS="/$DOMAIN/$HUB/$ROBOT_NAME/cdm"
                CONTROLLER_ADDRESS="/$DOMAIN/$HUB/$ROBOT_NAME/controller"

                echo
                echo "$CDM_ADDRESS"

                #echo pu -u "$U" -p "$P" "$CDM_ADDRESS" "$@"
                DISKS=$(pu -u "$U" -p "$P" "$CDM_ADDRESS" disk_status "" "" | sed -e "s/\\\//g")
                RC="$?"
                if [ "$RC" != 0 ]
                then

                        echo "WARN pu command failure ($RC): $DISKS" >&2
                        continue
                fi

                I=0
                echo "$DISKS" | grep -A11 FileSys |
                while read KEY DATATYPE SIZE VALUE
                do
                        #echo "$I $KEY='$VALUE'"
                        (( I+= 1 ))

                        case "$KEY" in

                        DiskActive) #='1'
                                DISK_ACTIVE="$VALUE"
                                # skip if the disk is not active
                                if [ "$DISK_ACTIVE" != "1" ]
                                then
                                        break
                                fi
                                ;;
                        DiskTotal) #='61087'
                                DISK_TOTAL="$VALUE"
                                ;;
                        DiskFree) #='48022'
                                DISK_FREE="$VALUE"
                                ;;
                        DiskAvail) #='48022'
                                DISK_AVAIL="$VALUE"
                                ;;
                        DiskUsed) #='13065'
                                DISK_USED="$VALUE"
                                ;;
                        TypeDesc)
                                # skip if the disk is not local
                                if [ "$VALUE" != "Local" ]
                                then
                                        break
                                fi
                                ;;
                        FileSys)
                                FILESYS="$VALUE"
                                if [ "$SIZE" -gt 18 ]
                                then
                                        echo "WARN: Unable to configure $CDM_ADDRESS $KEY $VALUE is too long, and may be truncated." >&2
                                        break
                                fi

                                # see if we care about this one
                                FOUND=$(echo "$LOOK_FOR_DISKS" | grep "^$FILESYS")
                                if [ -z "$FOUND" ]
                                then
                                        break
                                fi
                                ;;

                        DiskUsedPct) #='21'
                                DISK_USED_PCNT="$VALUE"
                                # this is the last piece of data we need to have all of the information for this disk

                                if [ "$DISK_TOTAL" -gt "$MAX_DISK_TOTAL" ]
                                then
                                        # this disk is big enough, make sure it is configured to be monitored in MB, not %

                                        if [ "$FILESYS" != "${FILESYS#*:}" ]
                                        then
                                                FILESYS=$(echo "$FILESYS" | sed -e "s/:/:\\\\/")
                                                CONFIG_BASE="/disk/alarm/fixed/$(echo "$FILESYS" | sed -e "s/\//#/g")"
                                        fi

                                        CONFIG_BASE="/disk/alarm/fixed/$(echo "$FILESYS" | sed -e "s/\//#/g")"


                                        echo
                                        echo "# Configuring: $ROBOT $FILESYS total=$DISK_TOTAL free=$DISK_FREE"

                                        PERCENT=$(pu -u "$U" -p "$P" "$CONTROLLER_ADDRESS" probe_config_get cdm "$ROBOT" "$CONFIG_BASE/percent" | grep "^value" | awk '{print $4}')
                                        ERROR_THRESH=$(pu -u "$U" -p "$P" "$CONTROLLER_ADDRESS" probe_config_get cdm "$ROBOT" "$CONFIG_BASE/error/threshold" | grep "^value"| awk '{print $4}')
                                        WARN_THRESH=$(pu -u "$U" -p "$P" "$CONTROLLER_ADDRESS" probe_config_get cdm "$ROBOT" "$CONFIG_BASE/warning/threshold" | grep "^value"| awk '{print $4}')

                                        ## percentage = no
                                        SET_CONFIG=$(pu -u "$U" -p "$P" "$CONTROLLER_ADDRESS" probe_config_set cdm "$CONFIG_BASE" "percent" "no" "1" "$ROBOT")
                                        #[ "$?" == 0 ] && echo OK || echo ERROR "$SET_CONFIG" >&2
                                        echo "$SET_CONFIG"


                                        ## error/threshold = $ERROR_MB
                                        SET_CONFIG=$(pu -u "$U" -p "$P" "$CONTROLLER_ADDRESS" probe_config_set cdm "$CONFIG_BASE/error" "threshold" "$ERROR_MB" "1" "$ROBOT")
                                        #[ "$?" == 0 ] && echo OK || echo ERROR "$SET_CONFIG" >&2
                                        echo "$SET_CONFIG"


                                        ## warning/threshold = $ERROR_MB
                                        SET_CONFIG=$(pu -u "$U" -p "$P" "$CONTROLLER_ADDRESS" probe_config_set cdm "$CONFIG_BASE/warning" "threshold" "$WARN_MB" "1" "$ROBOT")
                                        #[ "$?" == 0 ] && echo OK || echo ERROR "$SET_CONFIG" >&2
                                        echo "$SET_CONFIG"

                                        ## restart
                                        SET_CONFIG=$(pu -u "$U" -p "$P" "$CDM_ADDRESS" _stop)
                                        #[ "$?" == 0 ] && echo OK || echo ERROR "$SET_CONFIG" >&2
                                        echo "$SET_CONFIG"

                                fi


                                ;;
                        esac



                done

        done

#break  # temporary break to bail after the first hub.

done

exit
