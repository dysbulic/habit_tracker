App.Router.map( function() {
    this.route( 'habits', { path: "/" })
} )

App.HabitsRoute = Ember.Route.extend( {
    model: function() {
        return this.store.find( 'habit' )
    }
} )
