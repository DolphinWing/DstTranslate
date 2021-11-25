#!/bin/bash

WORKSHOP_ID=1993780385
WORKSHOP_DIR=workshop-$WORKSHOP_ID
LOCAL_PROPERTIES=.user.config

# check local properties
#echo "check if we have set steam library in $LOCAL_PROPERTIES"
if [ ! -f $LOCAL_PROPERTIES ]; then
    echo "Please enter your Steam library folder having DST installed."
    read -e -p "Steam Library: " -i "~/SteamLibrary" steam_library_path
    echo "export STEAM_LIBRARY=$steam_library_path" > $LOCAL_PROPERTIES
fi
. $LOCAL_PROPERTIES

# check installation
DST_DATA_BUNDLES="$STEAM_LIBRARY/steamapps/common/Don't Starve Together/data/databundles"
#ls -l $DST_DATA_BUNDLES
if [ ! -d "$DST_DATA_BUNDLES" ] || [ ! -f "$DST_DATA_BUNDLES/scripts.zip" ]; then
    echo "No DST installation found."
    exit 1
fi


# print last version
old_version=`cat ${WORKSHOP_DIR}/modinfo.lua |grep "..version.." |cut -d" " -f5`
old_version=${old_version#\(v*}
old_version=${old_version%\)*}
echo ">>> last mod version: ${old_version}"

# list old file info
ls -l android-app/app/src/main/assets/chinese_*.po


# print current version
new_version=`cat "$DST_DATA_BUNDLES/../../version.txt"`
echo ">>>  current version: ${new_version}"

# unzip from dst scripts
unzip -p "$DST_DATA_BUNDLES/scripts.zip" scripts/languages/chinese_s.po > /tmp/chinese_s.po
unzip -p "$DST_DATA_BUNDLES/scripts.zip" scripts/languages/chinese_t.po > /tmp/chinese_t.po
ls -l /tmp/chinese_*.po

# copy as new file
cp /tmp/chinese_s.po android-app/app/src/main/assets/chinese_s.po
cp /tmp/chinese_t.po android-app/app/src/main/assets/chinese_t.po
ls -l android-app/app/src/main/assets/chinese_*.po

sed -i "s/v${old_version}/v${new_version}/" ${WORKSHOP_DIR}/modinfo.lua

