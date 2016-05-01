package pt.quintans.mda.transformers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import pt.quintans.mda.raw.Mapping;
import pt.quintans.mda.raw.Mappings;
import pt.quintans.mda.core.BaseTransformer;
import pt.quintans.mda.core.Work;
import pt.quintans.mda.core.WorkerStore;
import pt.quintans.model.DomainMapping;
import pt.quintans.model.Item;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;

public class EmptyTransformer extends BaseTransformer {

	/**
	 * metodo para obter/carregar o domain mapping file.
	 * @param path o caminho relativo do ficheiro
	 * @return
	 */
	private List<DomainMapping> getDomainMapping(String path){
		Work wrk = WorkerStore.get();
		
		String[] paths = path.split("\\,");
		List<DomainMapping> list = new ArrayList<DomainMapping>();
		for(String p : paths){
			p = p.trim();
			// see if it is cached
			DomainMapping map = null;
			File file = null;
			try {
				JAXBContext jaxbContext = JAXBContext.newInstance(Mappings.class);
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
				file = new File(wrk.getWorkflowFolder() + File.separator + p);
				Mappings xMapings = (Mappings) unmarshaller.unmarshal(file);
				map = new DomainMapping();
				Map<String, Object> m = new LinkedHashMap<String, Object>();
				map.setName(xMapings.getName());
				map.setMap(m);
				for(Mapping xMap : xMapings.getMapping()){
				    Item item = new Item();
				    item.setDomain(xMap.getDomain());
				    item.setType(xMap.getType());
				    item.setInstance(xMap.getInstance());
				    m.put(xMap.getModel(), item);
				    
					//m.put(xMap.getModel().value(), xMap.getDomain());
					//m.put(xMap.getModel().value() + ".type", xMap.getType() != null ? xMap.getType() : xMap.getDomain());
					//m.put(xMap.getModel().value() + ".map", xMap);
				}
				
				list.add(map);
			} catch (JAXBException e) {
				e.printStackTrace();
			} catch (RuntimeException e) {
				System.err.println("There was an error while processing the model " + file.getAbsolutePath());
				throw e;
			}			
		}
		
		return list;
	}	
	
	public static String PROPERTIES_FILE = "properties";	
	public static String TEMPLATE_FOLDER = "templateFolder";	
	public static String DOMAIN_MAPPINGS = "domainmappings";	
	
	@Override
	public void transform(List<Object> list) {
		loadMapping(list);
		
		String propertiesFile = getOptional(PROPERTIES_FILE);
		if(propertiesFile != null) {
			File fx = new File(WorkerStore.get().getWorkflowFolder() + File.separator + propertiesFile);
			if(fx.exists()){
				Properties props = new Properties();
				try {
					props.load(new FileInputStream(fx));
					
					for(Entry<?, ?> entry : props.entrySet()){
						putInPipe((String) entry.getKey(), (String) entry.getValue());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		//String templateFolder = (String) getMap().get("templateFolder");
		String dom_maps = getOptional(DOMAIN_MAPPINGS);
		if(dom_maps != null){
			 List<DomainMapping> mappings = getDomainMapping((String)getOptional(DOMAIN_MAPPINGS));
			 // registers all domain mappings found
			 for(DomainMapping dm : mappings){
				 putInPipe(dm.getName(), dm.getMap());
			 }
		}

		// prepares generation
		String templateFolder = getMandatory(TEMPLATE_FOLDER);

		Configuration templateConfig = new Configuration();
		// Specify the data source where the template files come from.
		// Here I set a file directory for it:
		try {
			templateConfig.setDirectoryForTemplateLoading(new File(WorkerStore.get().getWorkflowFolder() + File.separator + templateFolder));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		// Specify how templates will see the data-model. This is an advanced topic...
		// but just use this:
		templateConfig.setObjectWrapper(new DefaultObjectWrapper());
		putInPipe(PipelineKeys.TEMPLATE_CONFIG, templateConfig);
	}

}
