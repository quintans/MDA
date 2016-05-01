package pt.quintans.mda.core;

public interface IM2MTransformer {
	public void create(Model model, String objectName);
	public void allCreated(Model model);
	public void relate(Model model, String objectName);
	public void allRelated(Model model);
}
