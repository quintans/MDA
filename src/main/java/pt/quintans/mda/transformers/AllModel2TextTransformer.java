package pt.quintans.mda.transformers;

import java.util.List;

import pt.quintans.mda.core.BaseTransformer;

public class AllModel2TextTransformer extends BaseTransformer {

	@Override
	public void transform(List<Object> mappings) {
		loadMapping(mappings);
	}

}
