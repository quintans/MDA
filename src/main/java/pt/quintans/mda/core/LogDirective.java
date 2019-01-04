package pt.quintans.mda.core;

import java.io.IOException;
import java.util.Map;

import freemarker.core.Environment;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class LogDirective implements TemplateDirectiveModel {
    
	public LogDirective(){
	}
	
	@Override
    public void execute(Environment env,
            Map params, TemplateModel[] loopVars,
            TemplateDirectiveBody body)
            throws TemplateException, IOException {
		SimpleScalar ssOut = (SimpleScalar)params.get("out");
        if (ssOut != null) {
            System.out.format("=====> %s\n", ssOut.toString());
        }
        
    }    
}
