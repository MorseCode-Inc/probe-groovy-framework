#!/bin/bash

HUB="$1"

DATA=$(hub_command.sh "$HUB" getrobots "" "")
#echo "data: $DATA"

echo "$DATA" | grep name | awk '{print $4}'

exit
