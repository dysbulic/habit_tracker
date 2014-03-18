Habit Tracker
=============

This is a rewrite of the android app currently in the master branch in couchdb.

# Live

The [current test server](http://doh.cloudant.com/habits/_design/habits/index.html).

# Running

* `sudo apt-get install couchdb'
* `sudo apt-get install python-setuptools`
* `sudo easy_install pip`
* `sudo apt-get install python-dev`
* `sudo pip install couchapp`
* `couchapp push . http://localhost:5984/habits`
