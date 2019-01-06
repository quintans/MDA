package pt.quintans.mda.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pt.quintans.mda.core.Model2TextAbstract;
import pt.quintans.mda.core.Tools;
import pt.quintans.mda.core.Work;
import pt.quintans.mda.core.WorkerStore;
import pt.quintans.mda.transformers.PipelineKeys;

public class AllInOneM2T extends Model2TextAbstract {	
	@Override
	public void transform(List<Object> mappings) {
		loadMapping(mappings);
		prepare();
	    
	    // create groups
	    String groupby = getOptional(PipelineKeys.GROUP_BY);
	    if(groupby != null) {
	    	Map<Object, List<Object>> groups = new LinkedHashMap<>();
	    	pt.quintans.mda.core.Model allModels[] = getFromPipe(PipelineKeys.ALL_MODELS);
			for (Object obj : allModels[0].getTransformedObjectList(getStereotype())) {
				Object groupKey = Tools.eval(obj, groupby);
				List<Object> group = groups.computeIfAbsent(groupKey, k -> new ArrayList<>());
				group.add(obj);
			}
			for(Map.Entry<Object, List<Object>> entry : groups.entrySet()) {
				putInPipe(PipelineKeys.GROUP, entry.getValue());
				putInPipe(PipelineKeys.GROUPKEY, entry.getKey());
				dump();
				removeFromPipe(PipelineKeys.GROUP);
			}
		} else {
			dump();
		}
	    
	}
	
	private void dump() {
		Work wrk = WorkerStore.get();
	
		Map<String, Object> pipeline = wrk.getPipeline();
		Map<String, Object> properties = new HashMap<>(pipeline);
		properties.putAll( getMap());
		String destinationFile = Tools.applyKeys(getDestination(), properties);
		destinationFile = Tools.processTemplate(properties, destinationFile);

	    putInPipe(PipelineKeys.DESTINATION_FILE, destinationFile);
		
	    if(dumpToFile(destinationFile) && !wrk.isQuiet()) {
			System.out.println(String.format("Generating the model(s) [%s] => %s", getTemplate(), destinationFile));
	    }
	}
}
