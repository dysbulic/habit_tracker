Habit Tracker
=============

This is a rewrite of the android app currently in the master branch in couchdb.

Get Paid To Work On This App
============================

My Ember skills are minimal and the application is shaping up to look like it was written by a beginner. I'd really like the assistance of an experienced developer and I have bitcoins to pay. Just [contact me](mailto:will@dhappy.org).

# Live

The [current test server](http://hbit.herokuapp.com)

# Running

To run locally, you'll need CouchDB to serve the data. `sudo apt-get install couchdb` in Ubuntu. The program relies on views to operate. To create these, run:

    COUCH_USER=will COUCH_PASS=secret node bin/make_views.js

The application is currently set up to deploy to Heroku. Though the application is straight HTML/JS there are issues passing credentials and cookies to ports other than 80. So, the Rack setup runs a reverse proxy — everything under `/db/` gets sent to the Couch server which is configured in `[config.ru](config.ru)`.

The application can also be built with Cordova to run on a mobile device. Simply execute `cordova run android` to run the app.
