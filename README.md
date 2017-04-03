# tinympi4j-master
a micro java offline distributed computation framework for fun, DO NOT use in production environment !
微型java分布式离线计算框架, 学习作品， 勿用于生产环境 !


以下例子实现分布式计算10000以内的素数

```java
public class Test{
	public static void main(String[] args) {
	
		//启动master上的tomcat
		final int masterport = 8086;
		final String masterurl = "http://192.168.1.100:" + masterport;
		TomcatTool.startMasterTomcat(masterport);
	
		//创建任务
		final BigTask<Integer> bigtask = BigTask.create(masterurl);
	
		//添加任务到两台计算节点， 请确保计算节点上的tomcat已启动
		bigtask.addTask2Slave("http://192.168.1.101:1234", PrimeSplitedtask.class, new Integer[] { 2, 5000 });
		bigtask.addTask2Slave("http://192.168.1.102:1234", PrimeSplitedtask.class, new Integer[] { 5001, 10000 });
	
		//等待所有节点执行完毕
		final Collection<Integer> resultset = bigtask.executeAndWait();
			
		//打印结果
		for (int n : resultset){
			//System.out.println(n);
		}
	}
}
```


```java

//创建SplitableTask的实现类
public class PrimeSplitedtask implements SplitableTask {

	
	@Override
	public Serializable execute(Serializable[] params) {
		
		final int fromnumber = (Integer) params[0];
		final int tonumber = (Integer) params[1];
		final Set<Integer> resultset = new LinkedHashSet<Integer>();

		for (int i = fromnumber; i <= tonumber; i++) {
			if (isprime(i))
				resultset.add(i);
		}
		return (Serializable) resultset;
	}
	
	
	//判断是否为素数
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

```