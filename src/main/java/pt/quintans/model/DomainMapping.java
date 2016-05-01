package pt.quintans.model;

import java.util.Map;

public class DomainMapping {
	private String name;
	private Map<String, Object> map;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<String, Object> getMap() {
		return map;
	}
	public void setMap(Map<String, Object> map) {
		this.map = map;
	}
	
	
}
