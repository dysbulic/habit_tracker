#!/bin/bash

adb shell "run-as com.synaptian.smoketracker.habits cat databases/habits.db > /sdcard/habits.db"
adb pull /sdcard/habits.db
sqlitebrowser habits.db
