var coax = require( 'coax' )
var util = require( 'util' )

var url = util.format( 'http://%s:%s@%s', process.env.COUCH_USER, process.env.COUCH_PASS, process.env.COUCH_HOST || 'localhost:5984' )
var db = coax( [url, 'habits'] )

console.log( url )

var habitDesign = db( ['_design', 'habit'] )

habitDesign.get( function( err, doc ) {
    if( err && err.error != 'not_found' ) {
        console.log( err )
    } else {
        console.log( 'habit rev', doc._rev )
        habitDesign.put(
            {
                views: {
                    all: {
                        map: function( doc ) { emit( doc.type, doc ) }.toString()
                    }
                },
                _rev: doc._rev
            },
            function( err, res ) {
                console.log( err, res )
            }
        )
    }
} )

var eventDesign = db( ['_design', 'event'] )

eventDesign.get( function( err, doc ) {
    if( err && err.error != 'not_found' ) {
        console.log( err )
    } else {
        console.log( 'event rev', doc._rev )
        eventDesign.put(
            {
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
                },
                _rev: doc._rev
            },
            function( err, res ) {
                console.log( err, res )
            }
        )
    }
} )
