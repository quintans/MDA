package pt.quintans.mda.custom;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pt.quintans.mda.core.CopyMode;
import pt.quintans.mda.core.MRoot;
import pt.quintans.mda.core.Model2TextAbstract;
import pt.quintans.mda.core.Tools;
import pt.quintans.mda.core.Work;
import pt.quintans.mda.core.WorkerStore;
import pt.quintans.mda.transformers.PipelineKeys;

public class AllModel2Text extends Model2TextAbstract {
	@SuppressWarnings("unchecked")
	@Override
	public void transform(List<Object> mappings) {
		loadMapping(mappings);
		prepare();

		Map<String, Object> pipeline = WorkerStore.get().getPipeline();

		// coloca todas as strings encontradas em propreties, para transformar
		Map<String, Object> properties = new LinkedHashMap<String, Object>();
		for (Map.Entry<String, Object> entry : pipeline.entrySet()) {
			if (entry.getValue() instanceof String)
				properties.put(entry.getKey(), (String) entry.getValue());
		}

		// traverse all entities of main model (index=0)
		pt.quintans.mda.core.Model allModels[] = getFromPipe(PipelineKeys.ALL_MODELS);
		for (Object obj : allModels[0].getTransformedObjectList(getStereotype())) {
			MRoot element = (MRoot) obj;
			String location = element.getLocation();
			if(location != null)
				location = location.replaceAll("\\\\", "/");
			String subFolder = getSubModelFolder() != null ? getSubModelFolder() + "/" : null;
			if (subFolder == null || location != null && location.startsWith(subFolder)) {
				properties.put("modelfolder", location);
				properties.put("name", element.getName());

				putInPipe(element.getStereotype().toLowerCase(), element);
				prepareElement(properties, element);

				// SUBLIST
				List<Object> list = null;
				if (getSublist() != null)
					list = (List<Object>) Tools.eval(element, getSublist());

				String filternamespace = getOptionalFromPipe(PipelineKeys.FILTERNAMESPACE);
				if(filternamespace != null && (element.getNamespace() == null || !element.getNamespace().startsWith(filternamespace))){
					filternamespace = null;
				}
				if (list != null) {
					for (Object o : list) {
						putInPipe(PipelineKeys.SUBLISTITEM, o);
						transformSublist(properties, filternamespace, element.getName());
					}

				} else
					transformSublist(properties, filternamespace, element.getName());
				
			}
		}
	}
	
	/**
	 * To be overriden, if we want to add more pre processing
	 * 
	 * @param properties
	 * @param element
	 */
	protected void prepareElement(Map<String, Object> properties, MRoot element){
		
	}

	protected void transformSublist(Map<String, Object> properties, String ignoreNamespace, String objectName) {
		Work wrk = WorkerStore.get();
		Map<String, Object> pipeline = wrk.getPipeline();

		String destinationFile = Tools.processTemplate(pipeline, getDestination());
		destinationFile = Tools.applyKeys(destinationFile, properties);

		putInPipe(PipelineKeys.DESTINATION_FILE, destinationFile);

		if (ignoreNamespace == null) {
			// para opercoes de append so apaga que sejam anteriores ao arranque da geracao
			if (getCopyMode().equals(CopyMode.APPEND)) {
				File fx = new File(wrk.getWorkflowFolder() + File.separator + destinationFile);
				if (fx.lastModified() < wrk.getStartupTime().getTime()) {
					if (!wrk.isQuiet())
						System.out.println(String.format("A apagar %s", destinationFile));
					fx.delete();
				}
			}

			if (dumpToFile(destinationFile) && !wrk.isQuiet())
				System.out.println(String.format("A %s <<%s>> %s [%s] => %s", getCopyMode().equals(CopyMode.APPEND) ? "concatenar"
						: "gerar", getStereotype(), objectName, getTemplate(), destinationFile));
		} else if (!wrk.isQuiet()) {
			System.out.println(String.format("A ignorar %s <<%s>>: nao esta no subnamespace \"%s\"", objectName,
					getStereotype(), ignoreNamespace));
			// System.out.println(String.format("Hashcode de \"%s\" = %s", element.getName(), DbNameHash.encode(element.getName(),
			// 14)));
		}
	}

}
