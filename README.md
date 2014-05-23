Habit Tracker
=============

This is a rewrite of the android app currently in the master branch in couchdb.

Get Paid To Work On This App
============================

My Ember skills are minimal and the application is shaping up to look like it was written by a beginner. I'd really like the assistance of an experienced developer and I have bitcoins to pay. Just [contact me](mailto:will@dhappy.org).

# Live

The [current test server](http://hbit.herokuapp.com)

# Running

The application is currently set up to deploy to Heroku. Though the application is straight HTML/JS I am having issues passing credentials and cookies to ports other than 80. So, the Rack setup runs a reverse proxy — everything under `/db/` gets sent to the Couch server.

The application can also be built with Cordova to run on a mobile device. Simply execute `cordova run android` to run the app.
