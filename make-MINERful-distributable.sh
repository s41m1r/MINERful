#!/bin/bash

VERSION_DATE=`date "+%y%j-%H%M"`

jar cvfm "MINERful-v${VERSION_DATE}.jar" manifest.mf lib src -C ./bin .
cp "MINERful-v${VERSION_DATE}.jar" "MINERful.jar"
zip -r "MINERful-v${VERSION_DATE}" \
    MINERful.jar \
    README.mv LICENSE licenses \
    run*.sh constraintsFunctions.cfg
cp "MINERful-v${VERSION_DATE}.zip" "MINERful.zip"
