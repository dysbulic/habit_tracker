window.App = Ember.Application.create( {
    LOG_TRANSITIONS: true
} )

App.ApplicationAdapter = DS.FixtureAdapter.extend()

App.Router.reopen( {
    rootURL: '/habits/_design/habits/index.html'
} )
