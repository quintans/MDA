package pt.quintans.mda.core;

public abstract class AbstractTransformer implements IM2MTransformer, ModelEventListener {
	protected String stereotype;
	protected String stereotypeAlias;

	public String getStereotype() {
		return stereotype;
	}

	public void setStereotype(String stereotype) {
		this.stereotype = stereotype;
	}

	public String getStereotypeAlias() {
		return stereotypeAlias;
	}

	public void setStereotypeAlias(String stereotypeAlias) {
		this.stereotypeAlias = stereotypeAlias;
	}

	public void allCreated(Model model){
		
	}
	
	public void allRelated(Model model){
		
	}
	
	public void onEvent(Event event) {
		
	}

}
