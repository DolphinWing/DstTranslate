#!/bin/bash

ls -l ~/Downloads/dst_cht.po

# copy updated translation
cp ~/Downloads/dst_cht.po workshop-1993780385/dst_cht.po
cat workshop-1993780385/dst_cht_mod.po >> workshop-1993780385/dst_cht.po

ls -l workshop-1993780385/dst_cht.po


