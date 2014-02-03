class EventsController < ApplicationController
  before_action :set_event, only: [:show, :edit, :update, :destroy]
  doorkeeper_for :all, if: lambda { !current_user && request.format.json? }
  skip_before_action :verify_authenticity_token, if: lambda { request.format.json? }
  before_filter :authenticate_user!, unless: lambda { request.headers["Content-Type"] == "application/json" }

  # GET /events
  # GET /events.json
  def index
    @events = []

    user = current_user
    user ||= User.find(doorkeeper_token[:resource_owner_id]) if doorkeeper_token

    user.habits.find_each do |habit|
      @events.concat(habit.events)
    end
    @events = @events.sort_by{|e| e.time}

    if params[:page]
      @events = @events.paginate(page: params[:page], per_page: params[:per_page] || 500)
    end
  end

  # GET /events/1
  # GET /events/1.json
  def show
  end

  # GET /events/new
  def new
    @event = Event.new
  end

  # GET /events/1/edit
  def edit
  end

  # POST /events
  # POST /events.json
  def create
    if params[:_json] and params[:_json].kind_of?(Array)
      @events = []
      params[:_json].each{ |event| event[:time] = Time.at(event[:time]) }
      begin
        @events << Event.create(params[:_json])
      rescue SQLite3::ConstraintException => e
        puts e
      end
    else
      @event = Event.new(event_params)
      
      if request.format.json?
        @event.time = Time.at(params[:time])
      end
      
      respond_to do |format|
        if @event.save
          format.html { redirect_to @event, notice: 'Event was successfully created.' }
          format.json { render action: 'show', status: :created, location: @event }
        else
          format.html { render action: 'new' }
          format.json { render json: @event.errors, status: :unprocessable_entity }
        end
      end
    end
  end

  # PATCH/PUT /events/1
  # PATCH/PUT /events/1.json
  def update
    respond_to do |format|
      if @event.update(event_params)
        format.html { redirect_to @event, notice: 'Event was successfully updated.' }
        format.json { head :no_content }
      else
        format.html { render action: 'edit' }
        format.json { render json: @event.errors, status: :unprocessable_entity }
      end
    end
  end

  # DELETE /events/1
  # DELETE /events/1.json
  def destroy
    @event.destroy
    respond_to do |format|
      format.html { redirect_to events_url }
      format.json { head :no_content }
    end
  end

  private
    # Use callbacks to share common setup or constraints between actions.
    def set_event
      @event = Event.find(params[:id])
    end

    # Never trust parameters from the scary internet, only allow the white list through.
    def event_params
      params.require(:event).permit(:habit_id, :time, :description)
    end
end
