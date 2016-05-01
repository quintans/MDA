package pt.quintans.mda.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EventMediator {
	public static final String EVT_MODEL_CONFIGURED = "EVT_MODEL_CONFIGURED";
	// ENTITY
	public static final String EVT_ENTITIES_RELATED = "EVT_ENTITIES_RELATED";
	public static final String EVT_ENTITIES_CONFIGURED = "EVT_ENTITIES_CONFIGURED";
	
	private static final Map<String, List<ModelEventListener>> listenersMap = new LinkedHashMap<String, List<ModelEventListener>>();

	public static void clear(){
		listenersMap.clear();
	}
	
	public static void addListener(String type, ModelEventListener listener){
		List<ModelEventListener> listeners = listenersMap.get(type);
		
		if(listeners == null){
			listeners = new ArrayList<ModelEventListener>();
			listenersMap.put(type, listeners);
		}
		
		listeners.add(listener);
	}
	
	public static void fire(Event event){
		if(event.getType() != null){
			if (!WorkerStore.get().isQuiet())
				System.out.println(String.format("=====> firing event %s.", event.getType()));
			
			List<ModelEventListener> listeners = listenersMap.get(event.getType());
			if(listeners != null){
				for(ModelEventListener listener : listeners){
					listener.onEvent(event);
				}
			}
		}
	}
	
	public static void remove(String type, ModelEventListener listener){
		List<ModelEventListener> listeners = listenersMap.get(type);
		if(listeners != null){
			listeners.remove(listener);
		}
	}

	public static boolean isListening(String type, ModelEventListener listener){
		List<ModelEventListener> listeners = listenersMap.get(type);
		if(listeners != null){
			return listeners.contains(listener);
		} else
			return false;
	}
}
