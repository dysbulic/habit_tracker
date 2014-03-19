Habits.Habit = DS.Model.extend( {
    name: DS.attr( 'string' ),
    isCompleted: DS.attr( 'boolean' )
} )

Habits.Habit.FIXTURES = [
    {
        id: 1,
        name: 'Pipe of Tobacco',
        isCompleted: true
    },
    {
        id: 2,
        name: 'Pipe of Marijuana',
        isCompleted: false
    },
    {
        id: 3,
        name: '80mg Latuda',
        isCompleted: false
    }
]
