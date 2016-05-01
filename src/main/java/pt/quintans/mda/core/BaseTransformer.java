package pt.quintans.mda.core;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pt.quintans.mda.raw.ListType;
import pt.quintans.mda.raw.MapType;

public abstract class BaseTransformer implements ITransformer {
	private LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
	
	/*
	public LinkedHashMap<String, Object> getMap() {
		return map;
	}
	*/
	
	private Map<String, Object> getPipeline() {
		return WorkerStore.get().getPipeline();
	}

	@Override
	public void map(String name, Object value, boolean save) {
		if(value instanceof String) {
			value = Tools.applyKeys((String) value, getPipeline());
		}
		map.put(name, value);
		if(save)
			putInPipe(name, value);
	}
	
	public LinkedHashMap<String, Object> getMap() {
		return map;
	}

	@SuppressWarnings("unchecked")
	public <T> T getMandatory(String name){
		Object obj = map.get(name);
		
		if(obj == null)
			obj = getPipeline().get(name);
		
		if(obj == null)
			throw new RuntimeException(String.format("A chave \"%s\" não foi definida no transformador!", name));
		
		return (T) obj;
	}

	@SuppressWarnings("unchecked")
	public <T> T getOptional(String name){
		Object obj = map.get(name);
		
		if(obj == null)
			obj = getPipeline().get(name);

		return (T) obj;
	}

	@SuppressWarnings("unchecked")
	public <T> T getOptionalFromPipe(String name){
		return (T) getPipeline().get(name);
	}

	@SuppressWarnings("unchecked")
	public <T> T getFromPipe(String name){
		Object obj = getPipeline().get(name);
		if(obj == null)
			throw new RuntimeException(String.format("The key \"%s\" wasn't found in the pipeline!", name));
		return (T) obj;
	}
	
	public void putInPipe(String name, Object value){
		getPipeline().put(name, value);
	}

	public void loadMapping(List<Object> mappings){
		for(Object obj : mappings){
			if(obj instanceof MapType){
				MapType mt = (MapType) obj;
				map(mt.getName(), mt.getValue(), mt.isSave());
			} else {
				ListType lt = (ListType) obj;
				LinkedHashMap<String, String> lhm = new LinkedHashMap<String, String>();
				map(lt.getName(), lhm, lt.isSave());
				List<MapType> maps = lt.getMap();
				for(MapType mt : maps){
					lhm.put(mt.getName(), mt.getValue());
					if(mt.isSave()){
						map(mt.getName(), mt.getValue(), true);
					}
				}
			}
		}
	}
}
