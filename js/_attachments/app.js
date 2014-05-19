App.Router.map( function() {
    //this.resource( 'readings', function() {
    //    this.resource( 'reading', { path: ':reading_id' } )
    //} )
    this.resource( 'readings', { path: '/' } )
    this.resource( 'reading', { path: '/reading/:reading_id' } )
    this.resource( 'new_reading', { path: '/reading/new' } )
    this.resource( 'wells' )
    this.resource( 'well', { path: '/well/:well_id' } )
    this.resource( 'new_well', { path: '/well/new' } )
    this.resource( 'login', { path: '/login' } )
} )

App.WellsRoute = Ember.Route.extend( {
    model: function() {
        return this.store.find( 'well' )
    }
} )

App.WellRoute = Ember.Route.extend( {
    model: function( params ) {
        return this.store.find( 'well', params.well_id )
    }
} )

App.ReadingsRoute = Ember.Route.extend( {
    model: function() {
        var self = this
        return this.store
            .findQuery( 'reading', {
                designDoc: 'reading',
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

App.ReadingRoute = Ember.Route.extend( {
    model: function() {
        return typeof(params) !== 'undefined' && this.store.find( 'reading', params.reading_id )
    }
} )

var gpsCoordinate
navigator.geolocation.getCurrentPosition( function( position ) {
    gpsCoordinate = { x: position.coords.longitude, y: position.coords.latitude }
} )

App.NewReadingController = Ember.ObjectController.extend( {
    init: function() {
        this._super()
        this.set( 'wells', Ember.ArrayProxy.createWithMixins( Ember.SortableMixin, {
            content: this.get( 'store' ).find( 'well' ),
            sortProperties: ['name'],
            sortAscending: true,
            orderBy: function( item1, item2 ) {
                if( gpsCoordinate ) {
                    function distance( p1, p2 ) {
                        return Math.sqrt( Math.pow( p1.x - p2.x, 2 ) + Math.pow( p1.y - p2.y, 2 ) )
                    }
                    
                    function toGPS( item ) {
                        return distance(
                            gpsCoordinate,
                            {
                                x: Ember.get( item, 'longitude' ),
                                y: Ember.get( item, 'latitude' )
                            }
                        )
                    }

                    var offsets = [ toGPS( item1 ), toGPS( item2 ) ]
                    
                    return offsets[0] - offsets[1]
                } else {
                    return Ember.get( item1, 'name' ).localeCompare( Ember.get( item2, 'name' ) )
                }
            }
        } ) )
    },
    actions: {
        save: function() {
            var self = this
            var store = this.get( 'store' )
            store.find( 'well', $('#well').val() ).then( function( well ) {
                var reading = store.createRecord( 'reading', {
                    well: well,
                    time: new Date(),
                    mcf: $('#mcf').val(),
                    line: $('#line').val(),
                    tbg: $('#tbg').val(),
                    csg: $('#csg').val()
                } )
                reading.save()

                well.get( 'readings' ).then( function( readings ) {
                    readings.pushObject( reading )
                    well.save()
                } )

                self.transitionToRoute( 'readings' )
            } )
        }
    }
} )

App.NewWellController = Ember.ObjectController.extend( {
    actions: {
        save: function() {
            var self = this
            var store = this.get( 'store' )
            var well = store.createRecord( 'well', {
                asset_id: $('#asset-id').val(),
                name: $('#name').val()
            } )
            well.save()
            
            self.transitionToRoute( 'wells' )
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
                        self.transitionToRoute( 'readings' )
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

Ember.LinkView.reopen( {
    attributeBindings: [ 'data-toggle', 'data-target' ]
} )
