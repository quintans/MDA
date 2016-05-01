package pt.quintans.mda.custom;

import java.util.HashMap;
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
		
		Work wrk = WorkerStore.get();
		
		Map<String, Object> pipeline = wrk.getPipeline();
		Map<String, Object> properties = new HashMap<String, Object>(pipeline);
		properties.putAll( getMap());
		String destinationFolder = Tools.applyKeys(getDestination(), properties);
		String destinationFile = Tools.applyKeys(getFilename(), properties);

	    putInPipe(PipelineKeys.DESTINATION_FOLDER, destinationFolder);

	    if(dumpToFile(destinationFolder, destinationFile) && !wrk.isQuiet())
			System.out.println(String.format("A gerar o(s) modelo(s) [%s] => %s/%s", getTemplate(), destinationFolder, destinationFile));  		
	}
}
