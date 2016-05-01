package pt.quintans.mda.transformers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import pt.quintans.mda.core.AbstractTransformer;
import pt.quintans.mda.core.BaseTransformer;
import pt.quintans.mda.core.Event;
import pt.quintans.mda.core.EventMediator;
import pt.quintans.mda.core.WorkerStore;

public abstract class AbstractFileSystemTransformer extends BaseTransformer {	

	private List<String> guids = null;
	
	public void registerGuid(String guid){
		if(guids.contains(guid))
			throw new RuntimeException(String.format("O GUID \"%s\" já foi utilizado. Os GUID são unicos em todo o modelo. Por favor substitua/apague o GUID", guid));
		
		guids.add(guid);
	}


	@Override
	public void transform(List<Object> mappings) {
		loadMapping(mappings);
		
		String modelPath = getMandatory(PipelineKeys.MODEL_PATH);
		String modelOldPath = getOptional(PipelineKeys.MODEL_PATH_OLD);
		// verifica se o pasta existe
		if(modelOldPath != null){
			File old = new File(WorkerStore.get().getWorkflowFolder() + File.separator + modelOldPath);
			if(!old.exists())
				modelOldPath = null;
		}
		LinkedHashMap<String, Object> loaders = getMandatory(PipelineKeys.LOADERS);
		LinkedHashMap<String, Object> transformers = getMandatory(PipelineKeys.TRANSFORMERS);
		LinkedHashMap<String, Object> aliases = getOptional(PipelineKeys.ALIASES);

		Map<String, JAXBContext> stereos = new LinkedHashMap<String, JAXBContext>();
		
		List<String> stereotypeNames = new ArrayList<String>(); 
		
		try {
			for(Map.Entry<String, Object> entry : loaders.entrySet()){
				stereos.put(entry.getKey(), JAXBContext.newInstance(Class.forName((String)entry.getValue())));
				stereotypeNames.add(entry.getKey());
			}
			
			putInPipe(PipelineKeys.STEREOTYPE_NAMES, stereotypeNames);
	
			if(aliases != null){
				for(Map.Entry<String, Object> entry : aliases.entrySet()){
					putInPipe(entry.getKey()+":alias", entry.getKey());
				}
			}
			
			// percorre todas as pastas
			String models[] = ( modelOldPath != null ? new String[]{modelPath, modelOldPath} : new String[]{modelPath} );
			pt.quintans.mda.core.Model allModels[] = getOptionalFromPipe(PipelineKeys.ALL_MODELS);
			if(allModels == null){
				allModels = new pt.quintans.mda.core.Model[modelOldPath != null ? 2 : 1];
				putInPipe(PipelineKeys.ALL_MODELS, allModels);
			}
			int cnt = 0;
			final String stps[] = stereos.keySet().toArray(new String[stereos.size()]);
			for(String commaSeparatedDirs : models){
				guids = new ArrayList<String>();
				
				System.out.println(String.format("************ Working on model(s) \"%s\" ************", commaSeparatedDirs));
	
				pt.quintans.mda.core.Model model = new pt.quintans.mda.core.Model();
				allModels[cnt] = model;
				String[] dirs = commaSeparatedDirs.split("\\,");
				for(String m : dirs){
					m = m.trim();
					File dir = new File(m.startsWith(".") ? WorkerStore.get().getWorkflowFolder() + File.separator + m : m);
					// procura todas as entidades, e carrega os modelos com as entidades encontradas
					loadXMLModel(dir, dir, 
							new FileFilter(){
								@Override
								public boolean accept(File pathname) {
									if(pathname.isDirectory())
										return true;
									else {
										String tmp = pathname.toString();
										for(String s  : stps)
											if(tmp.endsWith(s + ".xml"))
												return true;
									}
									
									return false;
								}
							},
							stereos, model);
				}
				
				// aplicação de todos os transformers a todos os elementos de cada estereotipos
				for(Map.Entry<String, Object> entry : transformers.entrySet()){
					AbstractTransformer transformer = (AbstractTransformer) Class.forName((String)entry.getValue()).newInstance();
					model.setTransformer(entry.getKey(), transformer);
					transformer.setStereotype(entry.getKey());
				}			
				if(aliases != null){
					for(Map.Entry<String, Object> entry : aliases.entrySet()){
						AbstractTransformer transformer = (AbstractTransformer) model.getTransformer(entry.getKey());
						transformer.setStereotypeAlias(entry.getKey());
					}
				}
				
				model.transformAll();
				EventMediator.fire(new Event(EventMediator.EVT_MODEL_CONFIGURED, model));
				
				EventMediator.clear();
				cnt++;
			}
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private void loadXMLModel(File root, File dir, FileFilter ff, Map<String, JAXBContext> stereos, pt.quintans.mda.core.Model model) throws JAXBException, InstantiationException, IllegalAccessException{
		File[] fxs = dir.listFiles(ff);
		Arrays.sort(fxs);
		if(fxs == null){
			try {
				System.out.println(String.format("No file list for %s", dir.getCanonicalPath()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		
		for(File f : fxs){
			if(f.isDirectory())
				loadXMLModel(root, f, ff, stereos, model);
			else {
				String tmp = f.toString();
				for(Map.Entry<String, JAXBContext> entry : stereos.entrySet()){
					String stereotype = entry.getKey();
					if(tmp.endsWith(stereotype + ".xml")){
						JAXBContext startJaxbContext = entry.getValue();
						Unmarshaller unmarshaller = startJaxbContext.createUnmarshaller();
						prepareModel(root, dir, f, model, unmarshaller, stereotype);
					}
				}
			}
		}
	}
	
	/**
	 * For override and apply specific Model pre processing
	 * 
	 * @param root
	 * @param dir
	 * @param file
	 * @param model
	 * @param unmarshaller
	 * @param stereotype
	 * @throws JAXBException
	 */
	protected abstract void prepareModel(File root, File dir, File file, pt.quintans.mda.core.Model model, Unmarshaller unmarshaller, String stereotype) throws JAXBException ;
	
	protected String getNamespaceAlias(String namespace){
		String aliases = getOptionalFromPipe("namespaceAlias");
		if(aliases != null){
			int idx = aliases.indexOf(namespace + "=");
			if(idx > -1){
				int start = idx + namespace.length() + 1;
				int semicolom = aliases.indexOf(";", start);
				if(semicolom > -1) {
					String alias = aliases.substring(start, semicolom);
					return alias;
				} else
					return aliases.substring(start);
			} 
		}
		return namespace;
	}
	
	protected boolean transformFile(File file){
		StringBuffer sb = new StringBuffer();
		
		BufferedReader br = null;
        String line = null;
        boolean transform = false;
		try{
			FileReader fr = new FileReader (file);
            br = new BufferedReader (fr);
        
            while ((line = br.readLine()) != null)
            {
            	int idx = -1;
            	if(line.indexOf("id=\"\"") > -1){
            		String guid = UUID.randomUUID().toString();
            		registerGuid(guid);
            		line = line.replaceFirst("id=\"\"", String.format("id=\"%s\"", guid));
            		transform = true;
            	} else if((idx = line.indexOf("id=\"")) > -1){
            		idx += 4;
            		int idx2 = line.indexOf("\"", idx + 1);
            		String guid = line.substring(idx, idx2);
            		if(guid.length() == 36)
            			registerGuid(guid);
            	}
            	
            	sb.append(String.format("%s\n", line));
            }
        
            // Close the input stream
            br.close();
            if(transform){
        		File tFile = new File(file.toString() + ".tmp");
        		tFile.delete(); // no caso de existir
        		BufferedWriter bw = new BufferedWriter(new FileWriter(tFile));
        		bw.write(sb.toString());
        		bw.close();
        		file.delete();
        		tFile.renameTo(file);
            }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return transform;
	}

}
