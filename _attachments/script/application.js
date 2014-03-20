window.App = Ember.Application.create( {
    LOG_TRANSITIONS: true
} )

App.ApplicationAdapter = DS.FixtureAdapter.extend()

App.Router.reopen( {
} )

App.MobileBaseView = Ember.View.extend( {
    attributeBindings: ['data-role']
} )

App.PageView = App.MobileBaseView.extend( {
    'data-role': 'page'
} )

App.HabitsView = App.PageView.extend( {
    templateName: 'habits',
    id: 'habits-view',
    didInsertElement: function() {
        $.mobile.pageContainer.pagecontainer( 'change', this.$() )
    }
} )
