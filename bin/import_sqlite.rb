#!/usr/bin/env ruby

# Script to load the data from the sqlite database backing the android
# version of the program into Couchbase.

require 'sqlite3'
require 'couchbase'

sql = SQLite3::Database.new 'data/org.dhappy.habits.db'
couch = Couchbase.connect bucket: 'habits', host: 'localhost'

statement = sql.prepare 'SELECT * FROM habit'
res = statement.execute 
    
res.each do |row|
  key = "habit:#{row[0]}:#{Time.now.to_i}"
  db_obj = couch.get( key, quiet: true )

  puts key
  next

  if not db_obj
    puts "Creating reading in Couch: #{volume.well.name} @ #{volume.mcf} on #{volume.start_date}"
    save_response = @db.set( key,
                             {
                               type: 'reading',
                               well: db_well['id'],
                               rails_id: volume.id,
                               time: volume.start_date,
                               mcf: volume.mcf
                             } )
    
    db_well['readings'] = db_well['readings'] || []
    db_well['readings'].push(key)
    save_response = @db.set(db_well['id'], db_well)
  end

  puts row.join "\s"
end
