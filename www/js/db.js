App = Ember.Application.create()

App.deferReadiness()

App.ApplicationAdapter = DS.FixtureAdapter.extend()

if( window.location.host == 'localhost' ) {
    App.Host = 'http://localhost:5984'
    App.Host = 'http://localhost:8092'
    App.Host = 'http://localhost:4984'
} else {
    App.Host = window.location.origin + "/db" // served from rack
}

App.ApplicationAdapter = EmberCouchDBKit.DocumentAdapter.extend( { db: 'habits', host: App.Host } )
App.ApplicationSerializer = EmberCouchDBKit.DocumentSerializer.extend()

App.AttachmentAdapter = EmberCouchDBKit.AttachmentAdapter.extend( { db: 'habits', host: App.Host } )
App.AttachmentSerializer = EmberCouchDBKit.AttachmentSerializer.extend()

App.Habit = DS.Model.extend( {
    type: DS.attr('string', { defaultValue: 'habit' } ),
    name: DS.attr( 'string' ),
    color: DS.attr( 'string' ),
    events: DS.hasMany( 'event', { async: true, defaultValue: [] } ),
    style: function() {
        return 'background-color: %@'.fmt( this.get( 'color' ) )
    }.property( 'color' ),
    lastTime: Ember.reduceComputed( 'events.@each.time', {
        initialValue: -Infinity,
        addedItem: function( accValue, event ) { 
            return Math.max( accValue, event.get( 'time' ) )
        },
        removedItem: function( accValue, event ) { 
            if( event.get( 'time' ) < accValue ) {
                return accValue
            }
        }
    } )
} )

App.Event = DS.Model.extend( {
    type: DS.attr('string', { defaultValue: 'event' } ),
    time: DS.attr( 'date' ),
    habit: DS.belongsTo( 'habit' )
} )

;( function() {
    function checkURL() {
        if( ! window.cblite ) {
            console.error( 'couchbase lite not present' )
        } else {
            cblite.getURL( function( err, url ) {
                var adapter = App.__container__.lookup('store:main').adapterFor( 'application' )
                url = url.substring( 0, url.length - 1 )
                Ember.set( adapter, 'host', url )
                
                App.advanceReadiness()

                var coax = require( 'coax' )
                var db = coax( [url, 'habits'] )
                
                db.put( function( err, res ) {
                    if( err && err.status != 412 ) {
                        for( prop in err ) {
                            alert( "ip:" + prop + " : " + err[prop] )
                        }
                    } else {
                        db.get( function( err, res ) {
                            if( err ) {
                                alert( "ig:" + err )
                            } else {
                                var design = ['_design', 'habit']
                                var views = {
                                    views: {
                                        all: {
                                            map: function( doc ) { emit( doc.type, doc ) }.toString()
                                        }
                                    }
                                }
                                db.put( design, views, function( err, info ) {
                                    if( err && err.status != 409 ) {
                                        alert( "phv:" + err.status )
                                    }
                                } )

                                design = ['_design', 'event']
                                views = {
                                    views: {
                                        all: {
                                            map: function( doc ) { emit( doc.type, doc ) }.toString()
                                        },
                                        by_time: {
                                            map: function( doc ) {
                                                if( doc.type == 'event' ) {
                                                    d = new Date(doc.time)
                                                    emit( [d.getFullYear(), d.getMonth(), d.getDate(), d.getHours(), d.getMinutes()], doc )
                                                }
                                            }.toString()
                                        }
                                    }
                                }
                                db.put( design, views, function( err, info ) {
                                    if( err && err.status != 409 ) {
                                        alert( "pev:" + err.status )
                                    }
                                } )

                            }
                        } )
                    }
                } )
            } )
        }
    }
    if( ! window.cordova ) {
        App.advanceReadiness()
    } else {
        document.addEventListener( 'deviceready', checkURL, false )
    }
} )()
