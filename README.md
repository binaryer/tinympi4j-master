# tinympi4j-master
a micro java offline distributed computation framework for fun, DO NOT use in production environment !
微型java分布式离线计算框架, 学习作品， 勿用于生产环境 !

```java
public static void main(String[] args) {

		final int masterport = 8086;
		final String masterurl = "http://192.168.1.100:" + masterport;

		TomcatTool.startMasterTomcat(masterport);

		final BigTask<Integer> bigtask = BigTask.create(masterurl);

		bigtask.addTask2Slave("http://192.168.1.101:1234", PrimeSplitedtask.class, new Integer[] { 2, 5000 });
		bigtask.addTask2Slave("http://192.168.1.102:1234", PrimeSplitedtask.class, new Integer[] { 5001, 10000 });

		final Collection<Integer> resultset = bigtask.executeAndWait();
		
		for (int n : resultset){
			//System.out.println(n);
		}

	}
```
