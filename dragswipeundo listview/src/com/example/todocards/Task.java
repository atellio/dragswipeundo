package com.example.todocards;

public class Task {
	
	private String taskName;
	private int status;
	private int id;
	private int position;
	
	public Task()
	{
		this.taskName = null;
		this.status = 0;
	}
	
	public Task(String taskName, int status, int position) {
		super();
		this.taskName = taskName;
		this.status = status;
		this.position = position;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getTaskName() {
		return taskName;
	}
	
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	
	public int getStatus() {
		return status;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}

	public int getPosition() {
		return position;
	}
	
	public void setPosition(int position) {
		this.position = position;
	}
	
}
