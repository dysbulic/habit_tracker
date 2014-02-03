function renderTasksCalendar() {
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

    function weeksBetween( start, end ) {
        return Math.floor( Math.abs( end.getTime() - start.getTime() ) / ( 7 * 24 * 60 * 60 * 1000 ) )
    }

    function statsList( events ) {
        var $list = $('<ul/>')
        var habits = events.map( function( event ) { return event.habit } )
        habits = habits.filter( function( habit, index, habits ) {
            return habits.lastIndexOf( habit ) === index
        } )
        habits.forEach( function( habit ) {
            var instances = events.filter( function( event, index ) {
                return event.habit == habit
            } )
            $list.append( $('<li/>')
                          .append( $('<span/>').addClass( 'color' ).css( { 'background-color': habit.color } ) )
                          .append( $('<span/>').text( habit.name + ": " + instances.length ) ) )
        } )
        return $list
    }

    var url = window.location.pathname
    if( url == '/events' ) {
        $('#loading-modal').modal()

        d3.json( '/habits.json', function( error, habits ) {
            habits = d3.nest()
                .key( function( d ) { return d.id } )
                .rollup( function( d ) { return d[0] } )
                .map( habits )
            
            d3.json( url + '.json', function( error, events ) {
                for( var i = 0; i < events.length; i++ ) {
                    events[i].time = new Date( Date.parse( events[i].time ) )
                    events[i].habit = habits[events[i].habit_id]
                }
                
                $('#stats').append( statsList( events ) )

                var start = new Date( d3.min( events, function( d ) { return d.time } ) )
                var end = new Date( d3.max( events, function( d ) { return d.time } ) )
                
                start.setDate( start.getDate() - 1 )

                var width = window.innerWidth,
                    cellSize = width / 8,
                    height = cellSize * Math.max( 1, weeksBetween( start, end ) ),
                    position = {
                        x: ( width - cellSize * 7 ) / 2,
                        y: 0
                    }

                var svg = d3.select( '#events' )
                    .append( 'svg' )
                    .attr( {
                        viewBox: "0 0 " + width + " " + height,
                        preserveAspectRatio: 'none',
                        width: '100%',
                        height: height * .9 // eyeballing
                    } )
                    .append( 'g' )
                    .attr( 'transform', "translate(" + position.x + "," + position.y + ")" )

                
                var days = svg.selectAll( '.day' )
                    .data( d3.time.days( start, end ) )
                    .enter()
                    .append( 'g' )
                    .attr( {
                        class: 'day',
                        transform: function( d ) {
                            return (
                                "translate("
                                    + weekday( d ) * cellSize
                                    + ","
                                    + ( week( d ) - week( start ) ) * cellSize
                                    + ")"
                            )
                        }
                    } )

                           
                days
                    .append( 'rect' )
                    .attr( {
                        width: cellSize,
                        height: cellSize
                    } )

                days
                    .append( 'text' )
                    .attr( {
                        class: 'date',
                        x: 10,
                        y: 30,
                    } )
                    .text( function( d ) { return d.getDate() } )

                days.selectAll( '.event' )
                    .data( function( d ) {
                        return events.filter( function( e ) { return date( d ) == date( e.time ) } )
                    } )
                    .enter()
                    .append( 'rect' )
                    .attr( {
                        class: 'event',
                        x: 0,
                        y: function( d ) { return cellSize * ( ( d.time.getHours() + d.time.getMinutes() / 60 ) / 24 ) },
                        width: cellSize,
                        height: cellSize * 0.01
                    } )
                    .style( {
                        fill: function( d ) { return habits[d.habit_id].color }
                    } )
                    .append( 'title' )
                    .text( function( d ) { return habits[d.habit_id].name } )
                

                $('#loading-modal').modal( 'hide' )
                return

                var nextWeek = function( d ) {
                    var nextWeek = new Date( d )
                    nextWeek.setDate( d.getDate() + 7 )
                    return nextWeek
                }

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
