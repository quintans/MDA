package pt.quintans.mda.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import freemarker.core.Environment;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class OutputToDirective implements TemplateDirectiveModel {
	private File root;
    
	public OutputToDirective(File root){
		this.root = root;
	}
	
	@Override
    public void execute(Environment env,
            Map params, TemplateModel[] loopVars,
            TemplateDirectiveBody body)
            throws TemplateException, IOException {
		SimpleScalar ssOut = (SimpleScalar)params.get("out");
        if (ssOut == null) {
            throw new TemplateModelException("Missing output file location.");
        }
        
        // If there is non-empty nested content:
        if (body != null) {
        	File fx = null;
        	if(ssOut.getAsString().startsWith("."))
        		fx = new File(root + File.separator + ssOut.getAsString());
        	else
        		fx = new File(ssOut.getAsString());
        		
        	//System.out.println("writing subfile to " + fx.getCanonicalPath());
        	//fx.delete();
        	fx.getParentFile().mkdirs();
			Writer out = new FileWriter(fx, false);      
            body.render(out);
			out.flush();
			out.close();
        } else {
            throw new RuntimeException("missing body");
        }
    }    
}
