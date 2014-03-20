App.Router.map( function() {
    //this.route( 'habits', { path: "/" })
} )

App.IndexRoute = Ember.Route.extend( {
    model: function() {
        return this.store.find( 'habit' )
    }
} )
