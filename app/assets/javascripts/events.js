function renderTasksCalendar() {
    $('#loading-modal').modal()

    var day = d3.time.format( '%-d' ),
        weekday = d3.time.format( '%w' ),
        week = d3.time.format( '%U' ),
        hour = d3.time.format( '%H' ),
        time = d3.time.format( '%H:%M' ),
        month = d3.time.format( '%B' ),
        year = d3.time.format( '%Y' ),
        monthNumber = d3.time.format( '%m' ),
        percent = d3.format( '.1%' ),
        date = d3.time.format( '%Y/%m/%d' )
    
    var url = window.location.pathname
    url = url.length <= 1 ? '/shifts/open' : url
    if( url == '/shifts' || url == '/shifts/open' || url == '/my/shifts' ) {
        d3.json( '/tasks.json', function( error, tasks ) {
            var newTasks = {}
            tasks.forEach( function( t ) { newTasks[t.id] = t } )
            tasks = newTasks
            
            d3.json( url + '.json', function( error, shifts ) {
                for( var i = 0; i < shifts.length; i++ ) {
                    shifts[i].start = new Date( Date.parse(shifts[i].start ) )
                    shifts[i].end = new Date( Date.parse(shifts[i].end ) )
                }
                
                var start = new Date( d3.min( shifts, function( d ) { return d.start } ) )
                var end = new Date( d3.max( shifts, function( d ) { return d.end } ) )
                
                start.setDate( start.getDate() - 7 ) // Range is exclusive

                var interval = d3.time.weeks( start, end )

                var nextWeek = function( d ) {
                    var nextWeek = new Date( d )
                    nextWeek.setDate( d.getDate() + 7 )
                    return nextWeek
                }

                var weekBounds = d3.nest()
                    .key( function( d ) { return week( d.start ) } )
                    .rollup( function( d ) {
                        return {
                            start: d3.min( d, function( d ) { return d.start.getHours() } ),
                            end: d3.max( d, function( d ) { return d.end.getHours() } ),
                        }
                    } )
                    .map( shifts )
                
                var shiftStarts = d3.nest()
                    .key( function( d ) { return d.start } )
                    .rollup( function( d ) { return d } )
                    .map( shifts )


                var shiftsByWeek = d3.nest()
                    .key( function( d ) { return week( d.start ) } )
                    .rollup( function( d ) { return d } )
                    .map( shifts )

                interval.forEach( function( day, index ) {
                    var weekdays = d3.time.days( day, nextWeek( day ) )

                    var titles = d3.select( '#shifts' )
                        .append( 'ol' )
                        .classed( 'titles', true )
                        .datum( day )
    
                    titles.append( 'li' )
    
                    titles.selectAll( '.title' )
                        .data( weekdays )
                        .enter()
                        .append( 'li' )
                        .classed( 'title', true )
                        .attr( {
                            weekday: weekday
                        } )
                        .text( d3.time.format( '%-d' ) )
    
                    if( index == 0 || month( day ) != month( nextWeek( day ) ) ) {
                        titles
                            .append( 'li' )
                            .classed( 'month', true )
                            .text( function() { return month( nextWeek( day ) ) } )
                    }

                    if( week( day ) in weekBounds ) {
                        var shiftsByTime = d3.nest()
                            .key( function( d ) { return time( d.start ) + "–" + time( d.end ) } )
                            .rollup( function( d ) { return d } )
                            .map( shiftsByWeek[ week( day ) ] )

                        var shiftTimes = []
                        for( var shiftTime in shiftsByTime ) {
                            shiftTimes.push( shiftTime )
                        }
                        shiftTimes.sort()
                        
                        var dayShifts = d3.nest()
                            .key( function( d ) { return time( d.start ) + "–" + time( d.end ) } )
                            .rollup( function( d ) { return d } )
                            .map( shifts.filter( function( d ) { return date( d.start ) == date( day ) } ) )

                        shiftTimes.forEach( function( times ) {
                            var weeks = d3.select( '#shifts' )
                                .append( 'ol' )
                                .classed( 'hours', true )
                            
                            weeks.append( 'li' )
                                .classed( 'legend', true )
                                .text( times )
                            
                            var shiftItems = weeks.selectAll( '.hour' )
                                .data( weekdays )
                                .enter()
                                .append( 'li' )
                                .classed( 'hour', true )
                                .attr( {
                                    weekday: weekday
                                } )
                                .append( 'ul' )
                                .classed( 'shifts', true )
                                .filter( function( d ) { return times in dayShifts } )
                                .data( function( d ) {
                                    dayShifts[ times ].filter( function( d ) { return date( d ) == date( day ) } )
                                } )
                                .filter( function( d ) { return d.length > 0 } )
                                .enter()
                                .classed( 'open', true )
                                .selectAll( '.shift' )
                                .append( 'li' )
                                .classed( 'shift', true )
                                .classed( 'taken', function( d ) { return d.taken } )
                                .on( 'dblclick', function( d ) { window.location = d.url } )
                                .append( 'label' )
                                .attr( {
                                    title: function( d ) {
                                        return tasks[d.task_id].name + " (×" + d.needed + ")"
                                    }
                                } )
                            
                            shiftItems
                                .append( 'input' )
                                .attr( {
                                    type: 'radio',
                                    name: function( d ) { return 'shift[' + d.start + ']' } 
                                } )
                            
                            shiftItems
                                .append( 'img' )
                                .classed( 'icon', true )
                                .attr( {
                                    src: function( d ) { return tasks[d.task_id] ? tasks[d.task_id].icon : null }
                                } )
                        } )
                    }
                } )

                $(document).trigger( 'shifts:loaded' )

                $('#loading-modal').modal( 'hide' )
            } )
        } )
    }
}

// Load on turbolinks page change
$(document).on( 'page:load', renderTasksCalendar )
$(document).ready( renderTasksCalendar )
