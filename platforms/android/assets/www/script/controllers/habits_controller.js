App.HabitsController = Ember.ArrayController.extend( {
    actions: {
        createHabit: function() {
            var name = this.get( 'newName' )

            if( ! name.trim() ) { return }

            var habit = this.store.createRecord( 'habit', {
                name: name,
                isComplete: false
            } )

            this.set( 'newName', '' ) // Clear the text field

            habit.save()

            // Too early
            $('#habits [data-role="listview"]').listview( 'refresh' )
        }
    }
} )
