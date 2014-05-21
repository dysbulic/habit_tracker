var coax = require( 'coax' )
var util = require( 'util' )

var url = util.format( 'http://%s:%s@%s', process.env.COUCH_USER, process.env.COUCH_PASS, process.env.COUCH_HOST || 'localhost:5984' )
var db = coax( [url, 'habits'] )

console.log( url )

db.put( ['_design', 'habit'],
        {
            views: {
                all: {
                    map: function( doc ) { emit( doc.type, doc ) }.toString()
                }
            }
        },
        function( err, res ) {
            console.log( err, res )
        }
      )

db.put( ['_design', 'event'],
        {
            views: {
                all: {
                    map: function( doc ) { emit( doc.type, doc ) }.toString()
                },
                by_time: {
                    map: function( doc ) {
                        if( doc.type == 'reading' ) {
                            d = new Date(doc.time)
                            emit( [d.getFullYear(), d.getMonth(), d.getDate(), d.getHours(), d.getMinutes()], doc )
                        }
                    }.toString()
                }

            }
        },
        function( err, res ) {
            console.log( err, res )
        }
      )

