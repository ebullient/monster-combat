#!/bin/bash

# Download well-formatted monster descriptions
wget -r --wait=5 -nc -nd -nH -l 2 \
     -D www.dandwiki.com \
     --accept '5e_SRD:*' \
     --user-agent='Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36' \
     https://www.dandwiki.com/wiki/5e_SRD:Monsters

for x in $(grep -l 'ACTIONS' 5e_SRD:*); do echo $x; done | sort -u > list.txt
