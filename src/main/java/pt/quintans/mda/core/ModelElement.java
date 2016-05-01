package pt.quintans.mda.core;


public class ModelElement {
	protected String name;
	protected String stereotype;
	protected Object source;
	protected Object transformed;

	public String toString(){
		return String.format("<<%s>> %s", stereotype, getName());
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStereotype() {
		return stereotype;
	}
	public void setStereotype(String stereotype) {
		this.stereotype = stereotype;
	}
	@SuppressWarnings("unchecked")
	public <T> T getSource() {
		return (T) source;
	}
	public void setSource(Object basic) {
		this.source = basic;
	}
	@SuppressWarnings("unchecked")
	public <T> T getTransformed() {
		return (T) transformed;
	}
	public void setTransformed(Object element) {
		this.transformed = element;
	}
	
	
}
