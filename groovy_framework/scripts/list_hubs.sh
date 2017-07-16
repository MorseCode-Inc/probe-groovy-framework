#!/bin/bash
HUB="$1"
DATA=$(hub_command.sh "$HUB" gethubs "" "")
#echo "data: $DATA"
echo "$DATA" | grep name | grep -v robotname | awk '{print $4}'
exit