package pt.quintans.mda.core;

import java.util.List;

public interface ITransformer {
	public void map(String name, Object value, boolean save);
	public void transform(List<Object> mappings);
}
