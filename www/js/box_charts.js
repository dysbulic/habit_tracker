$( function() {
    $.getJSON( 'http://localhost/db/habits/_design/habit/_view/all',
               function( data ) {
                   var habits = {}

                   data.rows
                       .filter( function( d ) { return d.key == 'habit' } )
                       .forEach( function( d ) {
                           habits[d.id] = d.value
                           habits[d.id].events = habits[d.id].events || []
                       } )

                   data.rows
                       .filter( function( d ) { return d.key == 'event' } )
                       .forEach( function( d ) { habits[d.value.habit].events.push( d.value ) } )
                   
                   console.log( habits )
                   
               } )
} )
