package pt.quintans.mda.core;

public class Event {
	private String type;
	private Object data;
	
	public Event(String type, Object data) {
		super();
		this.type = type;
		this.data = data;
	}
	

	public String getType() {
		return type;
	}


	public Object getData() {
		return data;
	}
}
