App = Ember.Application.create()

App.ApplicationAdapter = DS.FixtureAdapter.extend()

App.Host = window.location.origin + "/db"

App.ApplicationAdapter = EmberCouchDBKit.DocumentAdapter.extend( { db: 'wells', host: App.Host } )
App.ApplicationSerializer = EmberCouchDBKit.DocumentSerializer.extend()

App.AttachmentAdapter = EmberCouchDBKit.AttachmentAdapter.extend( { db: 'wells', host: App.Host } )
App.AttachmentSerializer = EmberCouchDBKit.AttachmentSerializer.extend()

App.Well = DS.Model.extend( {
    type: DS.attr('string', { defaultValue: 'well' } ),
    name: DS.attr( 'string' ),
    asset_id:  DS.attr( 'number' ),
    latitude:  DS.attr( 'number' ),
    longitude:  DS.attr( 'number' ),
    readings: DS.hasMany( 'reading', { async: true } )
} )

App.Reading = DS.Model.extend( {
    type: DS.attr('string', { defaultValue: 'reading' } ),
    time: DS.attr( 'date' ),
    mcf:  DS.attr( 'number' ),
    line:  DS.attr( 'number' ),
    tbg:  DS.attr( 'number' ),
    csg:  DS.attr( 'number' ),
    well: DS.belongsTo( 'well' )
} )

;( function() {
    var numReadings = 10

    var wellNames = [
        "GLASS 7", "GASTON 2", "GASTON 3", "ADAMCHIK 2", "KOOS 3", "LOWMAN 1", "LYDICK 1", "R&L DEVELOPMENT 2", "COUNTY OF CAMBRIA (IVORY) 1", "COUNTY OF CAMBRIA (BRZEZ/HUM) 3"
    ]

    var wells = []
    for( var i = 1; i <= wellNames.length; i++ ) {
        wells.push( {
            id: i,
            asset_id: 1000 + i,
            name: wellNames[ i - 1 ],
            longitude: ( typeof gpsCoordinate !== 'undefined' && gpsCoordinate.x || 20 ) + 100 * Math.random(),
            latitude: ( typeof gpsCoordinate !== 'undefined' && gpsCoordinate.y || 20 ) + 100 * Math.random()
        } )
    }

    var readings = []
    for( var i = 1; i <= numReadings; i++ ) {
        readings.push( {
            id: i,
            time: new Date( ( new Date() ).getTime() - 1000 * 60 * 60 * 24 * Math.random() ),
            well: wells[ Math.floor( wells.length * Math.random() ) ].id,
            mcf: 40 * Math.random(),
            line: 100 * Math.random() - 50,
            tbg: 60 * Math.random(),
            csg: Math.random()
        } )
    }
    
    var wellReadings = []
    readings.forEach( function( reading ) {
        wellReadings[ reading.well ] = wellReadings[ reading.well ] || []
        wellReadings[ reading.well ].push( reading.id )
    } )
    
    wellReadings.forEach( function( readings, idx ) {
        wells[ idx - 1 ].readings = readings
    } )

    App.Well.FIXTURES = wells
    App.Reading.FIXTURES = readings
} )()


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
