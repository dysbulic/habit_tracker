class CreateEvents < ActiveRecord::Migration
  def change
    create_table :events do |t|
      t.integer :habit_id
      t.datetime :time
      t.string :description

      t.timestamps
    end
  end
end
