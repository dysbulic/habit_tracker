class Ability
  include CanCan::Ability
  
  def initialize(user)
    if user
      can :manage, :all
    end

    user ||= User.new # guest user


    can :read, Event
  end
end
