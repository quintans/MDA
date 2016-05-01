package pt.quintans.mda.core;

import java.io.File;
import java.io.FileFilter;

public class ModelFileFilter implements FileFilter {
	private String stereotype;
	
	public ModelFileFilter(String stereotype) {
		super();
		this.stereotype = stereotype;
	}

	public boolean accept(File f) {
		return (f.isDirectory() || f.toString().endsWith(stereotype + ".xml"));
	}	
}
