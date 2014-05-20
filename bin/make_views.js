var coax = require( 'coax' )
var util = require( 'util' )

var url = util.format( 'http://%s:%s@%s', process.env.COUCH_USER, process.env.COUCH_PASS, process.env.COUCH_HOST || 'localhost:5984' )
var db = coax( [url, 'wells'] )

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

