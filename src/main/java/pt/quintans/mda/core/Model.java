package pt.quintans.mda.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Model {
	private Map<String, IM2MTransformer> transformers = new LinkedHashMap<String, IM2MTransformer>();
		
	private Map<String, List<ModelElement>> stereotypes = new LinkedHashMap<String, List<ModelElement>>();	
	private Map<String, ModelElement> objects = new LinkedHashMap<String, ModelElement>();
	
	public void setTransformer(String stereotype, IM2MTransformer transformer){
		transformers.put(stereotype, transformer);
	}
	
	public IM2MTransformer getTransformer(String stereotype){
		return transformers.get(stereotype);
	}
	
	public void saveObject(String name, String stereotype, Object source){
		saveObject(name, stereotype, source, null);
	}
	
	public void saveObject(String name, String stereotype, Object source, Object transformed){		
		
		if(objects.get(name) != null)
			throw new RuntimeException("Object " + name + " already defined.");
				
		ModelElement me = new ModelElement();
		me.setName(name);
		me.setStereotype(stereotype);
		me.setSource(source);
		if(transformed != null)
			me.setTransformed(transformed);
				
		objects.put(name, me);
		
		List<ModelElement> mes = stereotypes.get(stereotype);
		if(mes == null){
			mes = new ArrayList<ModelElement>();
			stereotypes.put(stereotype, mes);
		}
		
		mes.add(me);
	}
	
	public ModelElement getModelElement(String name){
		ModelElement me = objects.get(name);
		if(me == null)
			throw new RuntimeException(String.format("Undefined model element %s", name));
		
		return me;
	}
	
	public ModelElement remove(String name){
		return objects.remove(name);
	}
	
	public List<ModelElement> getModelElementList(String stereotype){
		return stereotypes.get(stereotype);
	}
	
	public List<Object> getTransformedObjectList(){
		List<Object> list = new ArrayList<Object>();
		Collection<ModelElement> mes = objects.values();
		if(mes != null){
			for(ModelElement me : mes){
				if(me.getTransformed() == null)
					throw new RuntimeException(String.format("Element <<%s>> %s not yet transformed", me.getStereotype(), me.getName()));
				
				list.add(me.getTransformed());
			}
		}
		return list;
	}

	public List<Object> getTransformedObjectList(String stereotype){
		List<Object> list = new ArrayList<Object>();
		List<ModelElement> mes = stereotypes.get(stereotype);
		if(mes != null){
			for(ModelElement me : mes){
				if(me.getTransformed() == null)
					throw new RuntimeException(String.format("Element <<%s>> %s not yet transformed", me.getStereotype(), me.getName()));
				
				list.add(me.getTransformed());
			}
		}
		return list;
	}
	
	public Map<String, Object> getTransformedObjectMap(){
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		Collection<ModelElement> mes = objects.values();
		if(mes != null){
			for(ModelElement me : mes){
				if(me.getTransformed() == null)
					throw new RuntimeException(String.format("Element <<%s>> %s not yet transformed", me.getStereotype(), me.getName()));
				
				map.put(me.getName(), me.getTransformed());
			}
		}
		return map;
	}

	public void transformAll(){
		// processa todos os elementos, agrupados por stereotype, para usar o mesmo transformador
		for(Map.Entry<String, List<ModelElement>> entry : stereotypes.entrySet()){
			IM2MTransformer transf = transformers.get(entry.getKey());
			for(ModelElement me : entry.getValue())
				transf.create(this, me.getName());
			transf.allCreated(this);
		}
			
		for(Map.Entry<String, List<ModelElement>> entry : stereotypes.entrySet()){
			IM2MTransformer transf = transformers.get(entry.getKey());
			for(ModelElement me : entry.getValue()){
				transf.relate(this, me.getName());
			}
			transf.allRelated(this); // sitio para configurar os listeners
		}		
	}
			
	public <T> T getTransformedObject(String name){
		ModelElement me = objects.get(name);
		if(me != null){
			if(me.getTransformed() == null)
				throw new RuntimeException(String.format("Element <<%s>> %s not yet transformed", me.getStereotype(), me.getName()));
			
			return me.getTransformed();
		}else
			throw new RuntimeException(String.format("Undefined model element %s", name));
	}

	public <T> T getBasicObject(String name){
		ModelElement me = objects.get(name);
		if(me != null)
			return me.getSource();
		else
			throw new RuntimeException(String.format("Undefined model element %s", name));
	}
	
	public List<Object> getBasicObjectList(String stereotype){
		List<Object> list = new ArrayList<Object>();
		for(ModelElement me : stereotypes.get(stereotype)){
			list.add(me.getSource());
		}
		return list;
	}
	
	public boolean hasObject(String objectName){
		return objects.containsKey(objectName);
	}
}
