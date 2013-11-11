#!/bin/bash

adb shell "mount -o remount rw /sdcard"
adb shell "run-as com.synaptian.smoketracker.habits cat databases/habits.db > /sdcard/habits.db"
adb pull /sdcard/habits.db
[ -f habits.db ] && sqlitebrowser habits.db
