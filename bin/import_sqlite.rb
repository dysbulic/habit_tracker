#!/usr/bin/env ruby

# Script to load the data from the sqlite database backing the android
# version of the program into Couchbase.

require 'sqlite3'
require 'couchbase'

sql = SQLite3::Database.new 'data/org.dhappy.habits.db'
couch = Couchbase.connect bucket: 'habits', host: 'localhost'

res = sql.prepare('SELECT * FROM habit').execute 

habits = {}

res.each do |row|
  key = "habit:#{row[1].downcase}"
  habit = couch.get( key, quiet: true )

  if not habit
    puts "Creating habit in Couch: #{key}"
    habit = {
      type: 'habit',
      name: row[1],
      color: row[2],
      events: []
    }

    save_response = couch.set( key, habit )
  end
  
  habit[:events] ||= []
  habit[:id] = key
  habits[row[0]] = habit
end

puts habits

res = sql.prepare('SELECT * FROM event').execute 
res.each do |row|
  if habit = habits[row[1]]
    time = Time.at row[2]
    key = "event:#{habit['name'].downcase}:#{time}"

    puts "Creating event in Couch: #{key}"

    event = {
      type: 'event',
      habit: habit[:id],
      time: time
    }
    habit[:events].push key

    save_response = couch.set( key, event )
  end
end

habits.each do |id, habit|
  save_response = couch.set( habit[:id], habit )
end
