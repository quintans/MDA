package pt.quintans.mda.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import freemarker.core.Environment;
import freemarker.template.SimpleScalar;
import freemarker.template.Template;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class SubTemplateDirective implements TemplateDirectiveModel {
	private File root;

	public SubTemplateDirective(File root){
		this.root = root;
	}

	@Override
    public void execute(Environment env,
            Map params, TemplateModel[] loopVars,
            TemplateDirectiveBody body)
            throws TemplateException, IOException {
		SimpleScalar ssTpl = (SimpleScalar)params.get("tpl");
        if (ssTpl == null) {
            throw new TemplateModelException(
                    "Missing template location.");
        }
		SimpleScalar ssOut = (SimpleScalar)params.get("out");
        if (ssOut == null) {
            throw new TemplateModelException(
                    "Missing output file location.");
        }

        boolean over = false;
		SimpleScalar ssOver = (SimpleScalar)params.get("over");
        if (ssOver == null) {
            throw new TemplateModelException(
                    "Missing output file location.");
        } else
        	over = "true".equals(ssOver.getAsString());

        File fx = null;
        if(ssOut.getAsString().startsWith("."))
        	fx = new File(root + File.separator + ssOut.getAsString());
        else
        	fx = new File(ssOut.getAsString());
        
        if(over || !fx.exists()){
	        fx.getParentFile().mkdirs();
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fx, false), "UTF8"));
	        Template template = env.getConfiguration().getTemplate(ssTpl.getAsString());
	        template.process(env.getDataModel(), out);
        }
    }
}
