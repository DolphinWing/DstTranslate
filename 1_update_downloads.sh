#!/bin/bash

WORKSHOP_ID=1993780385
WORKSHOP_DIR=workshop-$WORKSHOP_ID

ls -l ~/Downloads/chinese_*.po
ls -l ~/Downloads/dst_cht.po
ls -l android-app/app/src/main/assets/chinese_*.po
# list old file info
cp android-app/app/src/main/assets/chinese_s.po ~/Downloads/
cp android-app/app/src/main/assets/chinese_t.po ~/Downloads/
cp ${WORKSHOP_DIR}/dst_cht.po ~/Downloads/

ls -l ~/Downloads/chinese_*.po
ls -l ~/Downloads/dst_cht.po

