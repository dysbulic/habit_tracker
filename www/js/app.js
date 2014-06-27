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
    this.resource( 'sync', { path: '/sync' } )
} )

App.HabitsRoute = Ember.Route.extend( {
    model: function() {
        return this.store.find( 'habit' )
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
            .then(
                function( data ) {
                    var date = d3.time.format( '%Y-%m-%d' )
                    var nested = d3.nest()
                        .key( function( d ) { return date( d.get( 'time' ) ) } )
                        .rollup( function( d ) { return d } )
                        .map( data.content )
                    var arr = []
                    for( date in nested ) {
                        arr.push( {
                            date: date,
                            events: nested[date]
                        } )
                    }
                    return arr
                },
                function( err ) {
                    alert( err.status + ": " + err.statusText )
                    self.transitionTo( 'login' )
                }
            )
    }
} )

App.EventRoute = Ember.Route.extend( {
    model: function() {
        return this.store.find( 'event', params.event_id )
    }
} )

App.MoodRoute = Ember.Route.extend( {
    model: function() {
        return this.store.find( 'descriptor' )
    }
} )

App.StatsRoute = Ember.Route.extend( {
    model: function() {
        return this.store.find( 'event' )
    }
} )

App.StatsView = Ember.View.extend( {
    didInsertElement: function() {
        console.log( 'i', this.get( 'controller.model' ) )
        this.buildChart( this.get( 'controller.model' ) )
    },
    workspaceChanged: function() {
        this.buildChart( this.get( 'controller.model' ) )
    }.observes('controller.model'),
    buildChart: function( model ) {
        if( model && $('#stats').size() > 0 ) {
            var events = model.content

            var year = d3.time.format( '%Y' )
            var month = d3.time.format( '%m' )
            var monthName = d3.time.format( '%B' )
            var week = d3.time.format( '%U' )

            var by_year = d3.nest()
                .key( function( d ) { return year( d.get( 'time' ) ) } )
                .rollup( function( d ) { return d } )
                .map( events )
            
            var width = 1000, height = 100

            for( year in by_year ) {
                var events = by_year[year]

                d3.select( '#stats' )
                    .append( 'h1' )
                    .text( year )
                
                var bounds = d3.extent( events, function( d ) { return d.get( 'time' ) } )

                var by_week = d3.nest()
                    .key( function( d ) { return week( d.get( 'time' ) ) } )
                    .rollup( function( d ) { return d } )
                    .map( events )

                d3.time.weeks( bounds[0], bounds[1] ).forEach( function( start, index ) {
                    var events = by_week[ week( start ) ]
                    if( events ) {
                        var end = d3.time.day.floor( new Date( ( new Date( start ) ).setDate( start.getDate() + 7 ) ) )

                        if( index == 0 || month( start ) != month( end ) ) {
                            d3.select( '#stats' )
                                .append( 'h2' )
                                .text( monthName( end ) )
                        }
                        
                        var scale = d3.time.scale()
                            .domain( [start, end] )
                            .rangeRound( [0, width] )

                        var xAxis = d3.svg.axis()
                            .scale( scale )
                            .orient( 'bottom' )
                            .ticks( d3.time.days, 1 )
                            .tickFormat( d3.time.format( '%a %d' ) )

                        var svg = d3.select( '#stats' )
                            .append( 'svg' )
                            .attr( {
                                viewBox: "0 0 " + width + " " + height,
                                width: '100%',
                                height: height
                            } )

                        svg.selectAll( 'rect' )
                            .data( events )
                            .enter()
                            .append( 'a' )
                            .attr( { 'xlink:href': function( d ) { return "#/event/" + d.get( 'id' ) } } )
                            .append( 'rect' )
                            .attr( {
                                x: function( d ) { return scale( d.get( 'time' ) ) },
                                y: 0,
                                width: .0025 * width,
                                height: height - 35
                            } )
                            .style( {
                                fill: function( d ) { return d.get( 'habit' ).get( 'color' ) }
                                //fill: 'red'
                            } )

                        svg.append('g')
                            .attr( {
                                class: 'x-axis',
                                transform: "translate(0," + ( height - 30 ) + ")"
                            } )
                            .call(xAxis);
                    }
                } )
            }
        }
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

App.SyncRoute = Ember.Route.extend( {
    actions: {
        sync: function() {
            var coax = require( 'coax' )
            var db = coax( App.Host )

            var host = $('#host').val()
            var parts = host.split( '://' )
            var source = "%@://%@:%@@%@".fmt( parts[0], $('#user').val(), $('#pass').val(), parts[1] )

            console.log( 'source', source )

            db.post(
                '_replicate',
                {
                    source: source,
                    target: App.Host + "/habits"
                },
                function() {
                    console.log( 'repl', arguments )
                }
            )
        }
    }
} )

App.SyncController = Ember.ObjectController.extend( {
    init: function() {
        this.set( 'host', 'http://localhost:5984/habits' )
    }
} )

App.NewEventController = Ember.ObjectController.extend( {
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

Ember.Handlebars.registerBoundHelper( 'format-time-long', function( time ) {
    return moment( time ).format( 'LLL' )
} )

Ember.Handlebars.registerBoundHelper( 'format-time-numeric', function( time ) {
    return moment( time ).format( 'YYYY/MM/DD HH:mm' )
} )

Ember.Handlebars.registerBoundHelper( 'format-time', function( time ) {
    return moment( time ).format( 'H:mm' )
} )

Ember.Handlebars.registerBoundHelper( 'format-date', function( time ) {
    return moment( time ).format( 'D MMM YYYY' )
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
