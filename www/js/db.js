App = Ember.Application.create()

App.deferReadiness()

App.ApplicationAdapter = DS.FixtureAdapter.extend()

App.Host = window.location.origin + "/db" // served via reverse proxy

App.ApplicationAdapter = EmberCouchDBKit.DocumentAdapter.extend( { db: 'habits', host: App.Host } )
App.ApplicationSerializer = EmberCouchDBKit.DocumentSerializer.extend()

App.AttachmentAdapter = EmberCouchDBKit.AttachmentAdapter.extend( { db: 'habits', host: App.Host } )
App.AttachmentSerializer = EmberCouchDBKit.AttachmentSerializer.extend()

App.Selectable = DS.Model.extend( {
    id: DS.attr( 'string' ),
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
    } ),
    maxTime: function() {
        return this.store
            .findQuery( 'event', {
                designDoc: 'event',
                viewName: 'by_origin',
                options: {
                    key: this.get( 'id' ),
                    descending: true,
                    limit: 1
                }
            } )

    }
} )

App.Habit = App.Selectable.extend( {
    type: DS.attr('string', { defaultValue: 'habit' } ),
} )

App.Descriptor = App.Selectable.extend( {
    type: DS.attr('string', { defaultValue: 'descriptor' } ),
} )

App.Event = DS.Model.extend( {
    type: DS.attr('string', { defaultValue: 'event' } ),
    time: DS.attr( 'date' ),
    weight: DS.attr( 'number' ),
    origin: DS.belongsTo( 'selectable' )
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
                                                    var d = new Date( doc.time )
                                                    emit( [d.getFullYear(), d.getMonth(), d.getDate(), d.getHours(), d.getMinutes()], doc )
                                                }
                                            }.toString()
                                        },
                                        by_origin: {
                                            map: function( doc ) {
                                                if( doc.type == 'event' ) {
                                                    function pad2( num ) {
                                                        return ( num < 10 ? '0' : '' ) + num
                                                    }
                                                    var d = new Date( doc.time )
                                                    emit( [d.origin, d.getFullYear() + "-" + pad2( d.getMonth() ) + "-" + pad2( d.getDate() ) + "T" + pad2( d.getHours() ) + ":" + pad2( d.getMinutes() + "Z" )], null )
                                                }
                                            }.toString()
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
