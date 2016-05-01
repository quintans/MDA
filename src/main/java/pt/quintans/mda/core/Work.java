package pt.quintans.mda.core;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import pt.quintans.mda.raw.Transform;
import pt.quintans.mda.raw.Workflow;
import pt.quintans.mda.transformers.PipelineKeys;

public class Work {
	private Date startupTime;	

	public Date getStartupTime() {
		return startupTime;
	}
	
	private LinkedHashMap<String, Object> pipeline = new LinkedHashMap<String, Object>();

	public LinkedHashMap<String, Object> getPipeline() {
		return pipeline;
	}

	private boolean quiet;
	
	public boolean isQuiet() {
		return quiet;
	}

	private String workflowFile = null;

	public String getWorkflowFile() {
		return workflowFile;
	}

	private File workflowFolder;
	
	public File getWorkflowFolder() {
		return workflowFolder;
	}
	
	public void doIt(String workflowFile, String filternamespace, boolean quiet) throws JAXBException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException{
		startupTime = new Date();

		this.quiet = quiet;
		this.workflowFile = workflowFile;
		workflowFolder = new File(workflowFile).getParentFile();

		pipeline.put(PipelineKeys.FILTERNAMESPACE, filternamespace);
		
		//JAXBContext jaxbContext = JAXBContext.newInstance("pt.quintans.mda.workflow");
		JAXBContext jaxbContext = JAXBContext.newInstance(Workflow.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		Workflow workflow = (Workflow) unmarshaller.unmarshal(new File(workflowFile));			
		
		// instancia, define e executa todos os transformadores
		List<Transform> transforms = workflow.getTransformation().getTransform();
		for(Transform transform : transforms){
			if(transform.getDescription() != null){
				System.out.println("==========================================================");
				System.out.format("%s\n", transform.getDescription());
				System.out.println("==========================================================");
			}

			ITransformer transformer = (ITransformer) Class.forName(transform.getType()).newInstance();
			transformer.transform(transform.getMapOrList());
		}		
	}
}


