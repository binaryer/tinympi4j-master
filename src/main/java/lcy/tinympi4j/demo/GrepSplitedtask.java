package lcy.tinympi4j.demo;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import lcy.tinympi4j.common.SplitableTask;

public class GrepSplitedtask implements SplitableTask {

	@Override
	public Serializable execute(Serializable[] params) {

		final String[] lines = (String[]) params[0];
		final String word2grep = (String) params[1];
		final List<String> linelist = new LinkedList<String>();
		
		for(String line : lines){
			if(line.contains(word2grep)){
				linelist.add(line);
			}
		}
		
		return (Serializable) linelist;
		
	}

}
