package com.synaptian.smoketracker.habits;

public class HabitTime {
	private String name;
	private long time;

	public HabitTime(String name, long time) {
		this.name = name;
		this.time = time;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getTime() {
		return time;
	}
}
