import java.util.Collection;

import lcy.tinympi4j.demo.PrimeSplitedtask;
import lcy.tinympi4j.master.BigTask;
import lcy.tinympi4j.master.TomcatTool;

public class TestMaster {

	public static void main(String[] args) {

		final int masterport = 8086;
		final String masterurl = "http://192.168.1.101:" + masterport;

		TomcatTool.startMasterTomcat(masterport);

		final BigTask<Integer> bigtask = BigTask.create(masterurl);

		bigtask.addTask2Slave("http://127.0.0.1:1234", PrimeSplitedtask.class, 
				new Integer[] { 2, 50 });
		bigtask.addTask2Slave("http://127.0.0.1:1235", PrimeSplitedtask.class, 
				new Integer[] { 51, 100 });
		//bigtask.addTask2Slave("http://192.168.1.103:12346", PrimeSplitedtask.class, new Integer[] { 101, 150 });
		//bigtask.addTask2Slave("http://192.168.1.2:1234", PrimeSplitedtask.class, new Integer[] { 201, 300 });
		final Collection<Integer> resultset = bigtask.executeAndWait();

		for (int n : resultset){
			System.out.println(n);
		}

	}

}
