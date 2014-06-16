$( function() {
    $.getJSON( 'http://localhost/db/habits/_all_docs',
               function( data ) {
                   console.log( data )
               } )
} )
