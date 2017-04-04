# tinympi4j-master
a micro java offline distributed computation framework __for fun, DO NOT use in production environment !__  
微型java分布式离线计算框架  

## 原理
`tinympi4j-master`创建任务并提交到`tinympi4j-slave`执行， 执行完毕后把结果汇总到`tinympi4j-master`  
`tinympi4j-slave`可动态加载执行class文件，如需增加新功能，只需在`tinympi4j-master`端新增任务类，而无需修改`tinympi4j-slave`端代码  

![](https://raw.githubusercontent.com/binaryer/tinympi4j-master/master/mapreduce.jpg)

## 特性
+ 简单直观, 没有任何学习难度
+ slave支持多个任务并发/并行执行
+ 使用HTTP协议通信
+ 场景: 找素数/grep/wordcount/超大文件或大量小文件处理
+ 不支持复杂数据类型
+ 没有进度监控，健康监控，无容错功能


## 使用流程
1. 在多个计算节点启动 [tinympi4j-slave](https://github.com/binaryer/tinympi4j-slave)  
`java -jar tinympi4j-slave-0.1.jar {port}`

2. (在tinympi4j-master端) 编写任务类, 实现`SplitableTask`接口

3. (在tinympi4j-master端) 参考下面代码，把任务提交到计算节点执行

4. (在tinympi4j-master端) 等待所有计算节点执行完毕，获取结果

__注意java class版本: 如master上java7编译的class，slave上的java版本要>=7__

## 例子
#### 分布式计算10000以内的素数

```java

	public static void main(String[] args) {
	
		//启动master上的tomcat
		final int masterport = 8086;
		final String masterurl = "http://192.168.1.100:" + masterport;
		TomcatTool.startMasterTomcat(masterport);
	
		//创建任务
		final BigTask<Integer> bigtask = BigTask.create(masterurl);
	
		//添加任务到两台计算节点， 请确保计算节点上的 tinympi4j-slave 已启动
		//关于计算节点: https://github.com/binaryer/tinympi4j-slave
		bigtask.addTask2Slave("http://192.168.1.101:1234", PrimeSplitedtask.class, new Integer[] { 2, 5000 });
		bigtask.addTask2Slave("http://192.168.1.102:1234", PrimeSplitedtask.class, new Integer[] { 5001, 10000 });
	
		//等待所有节点执行完毕
		final Collection<Integer> resultset = bigtask.executeAndWait();
			
		//打印结果
		for (int n : resultset){
			//System.out.println(n);
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

## 后续完善
+ 子任务进度查询
+ slave端更多的设置选项: 如线程池大小
+ 单个子任务完成异步回调
+ 总任务完成异步回调
+ 暂停/继续/取消执行中的任务
+ 支持所有数据类型
+ 支持压缩传输
+ 支持未完成的任务回传已完成结果
+ 支持子节点故障转移


## Author
林春宇@深圳  
chunyu_lin@163.com  
