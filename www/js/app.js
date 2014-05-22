App.Router.map( function() {
    //this.resource( 'readings', function() {
    //    this.resource( 'reading', { path: ':reading_id' } )
    //} )
    this.resource( 'habits', { path: '/' } )
    this.resource( 'habit', { path: '/habit/:habit_id' } )
    this.resource( 'new_habit', { path: '/habit/new' } )
    this.resource( 'events' )
    this.resource( 'event', { path: '/event/:event_id' } )
    this.resource( 'new_event', { path: '/event/new/:habit_id' } )
    this.resource( 'mood', { path: '/mood' } )
    this.resource( 'goals', { path: '/goals' } )
    this.resource( 'stats', { path: '/stats' } )
    this.resource( 'login', { path: '/login' } )
} )

App.HabitsRoute = Ember.Route.extend( {
    model: function() {
        return this.store.find( 'habit' ).then( function( habit ) {
            console.log( 'HabitRoute.model', habit )
            return habit
        } )
    },
    actions: {
        createEvent: function( habitId ) {
            var self = this
            var store = this.get( 'store' )
            store.find( 'habit', habitId ).then( function( habit ) {
                store
                    .createRecord( 'event', {
                        habit: habit,
                        time: new Date()
                    } )
                    .save().then( function( event ) {
                        habit.get( 'events' ).then( function( events ) {
                            if( ! events ) {
                                events = []
                                habit.set( 'events', events )
                            }
                            events.pushObject( event )
                            habit.save().then( function() {
                                self.transitionTo( 'events' )
                            } )
                        } )
                    } )
            } )
        }
    }
} )

App.HabitRoute = Ember.Route.extend( {
    model: function( params ) {
        return this.store.find( 'habit', params.habit_id )
    }
} )

App.EventsRoute = Ember.Route.extend( {
    model: function() {
        var self = this
        return this.store
            .findQuery( 'event', {
                designDoc: 'event',
                viewName: 'by_time',
                options: {
                    descending: true,
                    limit: 100
                }
            } )
            .then( function( data ) { return data },
                   function( err ) {
                       alert( err.status + ": " + err.statusText )
                       self.transitionTo( 'login' )
                   } )
    }
} )

App.EventRoute = Ember.Route.extend( {
    model: function() {
        return this.store.find( 'event', params.event_id )
    }
} )

App.NewHabitController = Ember.ObjectController.extend( {
    actions: {
        add: function() {
            var self = this
            var store = this.get( 'store' )
            var habit = store.createRecord( 'habit', {
                name: $('#name').val(),
                color: $('#color').val()
            } )
            habit.save()
            
            self.transitionToRoute( 'habits' )
        }
    }
} )

App.NewEventRoute = Ember.Route.extend( {
    model: function( params ) {
        return this.store.find( 'habit', params.habit_id )
    },
    setupController: function( controller, model ) {
        controller.set( 'selectedHabit', model )
    }
} )

App.NewEventController = Ember.ObjectController.extend( {
    init: function() {
        console.log( 'init', this.get( 'selectedHabit' ) )
    },
    selectedHabit: null,
    actions: {
        save: function() {
            var self = this
            var store = this.get( 'store' )
            store.find( 'habit', $('#habit').val() ).then( function( habit ) {
                var event = store.createRecord( 'event', {
                    habit: habit,
                    time: new Date()
                } )
                event.save()

                habit.get( 'events' ).then( function( events ) {
                    events.pushObject( event )
                    habit.save()
                } )

                self.transitionToRoute( 'events' )
            } )
        }
    }
} )

App.LoginController = Ember.ObjectController.extend( {
    actions: {
        login: function() {
            var self = this
    
            $
                .ajax( {
                    type: 'POST',
                    url: "%@/_session".fmt( App.Host ),
                    data: { name: $('#username').val(), password: $('#password').val() }
                } )
                .then(
                    function( response ) {
                        self.transitionToRoute( 'habits' )
                        return response
                    },
                    function() {
                        console.error( 'Error establishing session', arguments )
                    }
                )
        }
    }
} )


Ember.Handlebars.registerBoundHelper( 'format-time-passed', function( time ) {
    return moment( time ).fromNow()
} )

Ember.Handlebars.registerBoundHelper( 'format-time-long', function( time ) {
    return moment( time ).format( 'LLL' )
} )

Ember.Handlebars.registerBoundHelper( 'format-time-numeric', function( time ) {
    return moment( time ).format( 'YYYY/M/D @ H:mm' )
} )

Ember.Handlebars.registerBoundHelper( 'two-digit-float', function( number ) {
    return Number( number ).toFixed( 2 )
} )

Ember.Handlebars.registerBoundHelper( 'timer-from', function( time ) {
    return moment( time ).fromNow()
} )

Ember.LinkView.reopen( {
    attributeBindings: [ 'data-toggle', 'data-target' ]
} )
