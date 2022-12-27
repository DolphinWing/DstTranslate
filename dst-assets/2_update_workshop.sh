#!/bin/bash

WORKSHOP_ID=1993780385
WORKSHOP_DIR=workshop-$WORKSHOP_ID
LOCAL_PROPERTIES=.user.config

# go back to project root
pushd ../

# copy updated translation
if [ -f ~/Downloads/dst_cht.po ]; then
    ls -l ~/Downloads/dst_cht.po
    cp ~/Downloads/dst_cht.po ${WORKSHOP_DIR}/dst_cht.po
fi

## attach dst_cht_mod.po to dst_cht.po
#msgctxt=`cat ${WORKSHOP_DIR}/dst_cht.po |tail -4 |grep msgctxt |cut -d" " -f2`
#if [[ ${msgctxt} == *"STRINGS.NAMES.BAKA_LAMP_SHORT"* ]]; then
#    echo "already attached!"
#else
#    cat ${WORKSHOP_DIR}/dst_cht_mod.po >> ${WORKSHOP_DIR}/dst_cht.po
#fi

# show latest dst_cht.po
ls -l ${WORKSHOP_DIR}/dst_cht.po

exit 0

# check steam library version code
. $LOCAL_PROPERTIES

# print last version
old_version=`cat ${WORKSHOP_DIR}/modinfo.lua |grep "..version.." |cut -d" " -f5`
old_version=${old_version#\(v*}
old_version=${old_version%\)*}
echo "last version: ${old_version}"

# print current version
new_version=`cat "$STEAM_LIBRARY/steamapps/common/Don't Starve Together/version.txt"`
echo "current version: ${new_version}"

# replace old version code
sed -i "s/v${old_version}/v${new_version}/" ${WORKSHOP_DIR}/modinfo.lua

