package lcy.tinympi4j.common;

import java.io.Serializable;

public interface SplitableTask {
	
	
	Serializable execute(Serializable[] params);
	
	
}
