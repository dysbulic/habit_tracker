json.array!(@habits) do |habit|
  json.extract! habit, :id, :color, :name, :description
  json.url habit_url(habit, format: :json)
end
