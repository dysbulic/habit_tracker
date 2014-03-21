App.Habit = DS.Model.extend( {
    name: DS.attr( 'string' ),
    isComplete: DS.attr( 'boolean' )
} )

App.Habit.FIXTURES = [
    {
        id: 1,
        name: 'Pipe of Tobacco',
        isComplete: true
    },
    {
        id: 2,
        name: '10-Minute Meditation',
        isComplete: false
    },
    {
        id: 3,
        name: '150mg Buoropion',
        isComplete: false
    }
]
