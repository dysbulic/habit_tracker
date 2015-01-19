Habit Tracker
=============

This is a rewrite of an [android app](https://play.google.com/store/apps/details?id=org.dhappy.habits) in Ember.js backed by Couchbase.

# Live

The [current test server](http://hbit.herokuapp.com)

# Running

To run locally, you'll need CouchDB to serve the data. `sudo apt-get install couchdb` in Ubuntu. The program relies on views to operate. To create these, run:

    COUCH_USER=will COUCH_PASS=secret node bin/make_views.js

The application is currently set up to deploy to Heroku. Though the application is straight HTML/JS there are issues passing credentials and cookies to ports other than 80. So, the Rack setup runs a reverse proxy — everything under `/db/` gets sent to the Couch server which is configured in [config.ru](config.ru).

The application can also be built with Cordova to run on a mobile device. Simply execute `cordova run android` to run the app.
