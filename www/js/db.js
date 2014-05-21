App = Ember.Application.create()

App.ApplicationAdapter = DS.FixtureAdapter.extend()

App.Host = window.location.origin + "/db"

App.ApplicationAdapter = EmberCouchDBKit.DocumentAdapter.extend( { db: 'habits', host: App.Host } )
App.ApplicationSerializer = EmberCouchDBKit.DocumentSerializer.extend()

App.AttachmentAdapter = EmberCouchDBKit.AttachmentAdapter.extend( { db: 'habits', host: App.Host } )
App.AttachmentSerializer = EmberCouchDBKit.AttachmentSerializer.extend()

App.Habit = DS.Model.extend( {
    type: DS.attr('string', { defaultValue: 'habit' } ),
    name: DS.attr( 'string' ),
    color: DS.attr( 'string' ),
    events: DS.hasMany( 'event', { async: true } ),
    style: function() {
        return 'background-color: %@'.fmt( this.get( 'color' ) )
    }.property( 'color' ),
    lastTime: function() {
        var events = this.get( 'events' )
        var times = events.map( function( e ) {
            return e.get( 'time' )
        } )
        return times.length == 0 ? undefined : times.sort()[0]
    }.property( 'events' ),
} )

App.Event = DS.Model.extend( {
    type: DS.attr('string', { defaultValue: 'event' } ),
    time: DS.attr( 'date' ),
    habit: DS.belongsTo( 'habit' )
} )

;( function() {
    function checkURL() {
        if( window.cblite ) {
            cblite.getURL( function( err, url ) {
                var adapter = App.__container__.lookup('store:main').adapterFor( 'application' )
                //url = url.substring( 0, url.length - 1 )
                alert( url )
                Ember.set( adapter, 'host', url )
                
                var xmlHttp = new XMLHttpRequest()
                xmlHttp.open( 'GET', url, false )
                xmlHttp.send( null )
                alert( 'XMLHttpRequest get: ' +  xmlHttp.responseText )

                var xmlHttp = new XMLHttpRequest()
                xmlHttp.open( 'PUT', url + "wells", false )
                xmlHttp.setRequestHeader( 'Accept', 'application/json,*/*;q=0.1' )
                xmlHttp.send( null )
                alert( 'XMLHttpRequest put: ' +  xmlHttp.responseText )

                var coax = require( 'coax' )
                var db = coax( [url, 'wells'] )
                
                db.put( function( err, res ) {
                    if( err && err.status != 412 ) {
                        for( prop in err ) {
                            alert( "ip:" + prop + " : " + err[prop] )
                        }
                    } else {
                        alert( "ip: after" )
                        db.get( function( err, res ) {
                            if( err ) {
                                alert( "ig:" + err )
                            } else {
                                var design = "_design/reading"
                                var views = {
                                    views: {
                                        by_time: {
                                            map: function(doc) {
                                                if( doc.type == 'reading' ) {
                                                    d = new Date(doc.time);
                                                    emit([d.getFullYear(), d.getMonth(), d.getDate(), d.getHours(), d.getMinutes()], doc)
                                                }
                                            }.toString()
                                        }
                                    }
                                }
                                db.put( design, views, function( err, info ) {
                                    if( err && err.status != 409 ) {
                                        alert( "pv:" + err.status )
                                    } else {
                                        var view = db( [design, '_view'] )
                                        view.get( 'by_time', function( err, res ) {
                                            if( err ) {
                                                for( prop in err ) {
                                                    alert( "gv:" + prop + " : " + err[prop] )
                                                }
                                            } else {
                                                alert( res )
                                            }
                                        } )
                                    }
                                } )
                            }
                        } )
                    }
                } )
            } )
        }
    }
    document.addEventListener( 'deviceready', checkURL, false )
} )()
