class CreateHabits < ActiveRecord::Migration
  def change
    create_table :habits do |t|
      t.string :color
      t.string :name
      t.string :description

      t.timestamps
    end
  end
end
