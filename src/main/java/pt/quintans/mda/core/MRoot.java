package pt.quintans.mda.core;

import java.util.Collection;
import java.util.Set;

public class MRoot implements Comparable<MRoot>{
	protected String name;
	protected String namespace;
	protected String[] subnamespaces;
	protected String stereotype;
	protected String location;
	protected MRoot parent;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getNamespace() {
		return namespace;
	}

	public String[] getSubnamespaces() {
		if (subnamespaces == null)
			return new String[] {};

		return subnamespaces;
	}

	public void setSubnamespaces(String[] subnamespaces) {
		this.subnamespaces = subnamespaces;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
		String[] tmp = namespace.split("\\.");
		int len = tmp.length;
		subnamespaces = new String[len];
		for (int i = 0; i < len; i++) {
			subnamespaces[len - i - 1] = tmp[i];
		}
	}

	public String getStereotype() {
		return stereotype;
	}

	public void setStereotype(String stereotype) {
		this.stereotype = stereotype;
	}

	public MRoot getParent() {
		return parent;
	}

	public void setParent(MRoot parent) {
		this.parent = parent;
	}

    private int weight = 0;
    
    public int getWeight() {
        return weight;
    }
    
    public void setWeight(int weight) {
        this.weight = weight;
    }
    
    protected void incWeight(){
        weight++;
    }

    @Override
    public int compareTo(MRoot mroot) {
        if(weight < mroot.weight) return -1;
        else if(weight > mroot.weight) return 1;
        else return 0;
    }
    
    public void pushWeight(Set<Object> set){
    }

    protected void pushWeight(Set<Object> set, Collection<? extends MRoot> mroots){
        if(mroots != null) {
            for(MRoot mroot : mroots) {
                pushWeight(set, mroot);
            }
        }
    }
    
    protected void pushWeight(Set<Object> set, MRoot mroot){
        if(mroot != null && !set.contains(mroot)) {
            set.add(mroot);
            mroot.pushWeight(set);
        }
    }
}
