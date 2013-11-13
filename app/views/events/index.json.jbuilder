json.array!(@events) do |event|
  json.extract! event, :habit_id, :time, :description
  json.url event_url(event, format: :json)
end
