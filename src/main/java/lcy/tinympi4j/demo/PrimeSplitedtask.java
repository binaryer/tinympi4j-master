package lcy.tinympi4j.demo;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import lcy.tinympi4j.common.SplitableTask;

public class PrimeSplitedtask implements SplitableTask {

	
	@Override
	public Serializable execute(Serializable[] params) {
		
		final int fromnumber = (Integer) params[0];
		final int tonumber = (Integer) params[1];
		final Set<Integer> resultset = new LinkedHashSet<Integer>();

		for (int i = fromnumber; i <= tonumber; i++) {
			if(Thread.currentThread().isInterrupted())
				return null;
			if (isprime(i))
				resultset.add(i);
		}
		return (Serializable) resultset;
	}
	
	private boolean isprime(int number) {
		int n = 2;
		while (true) {
			if (number % n == 0 && number!=n)
				return false;
			n++;
			if (n > Math.sqrt(number))
				return true;
		}
	}

}
