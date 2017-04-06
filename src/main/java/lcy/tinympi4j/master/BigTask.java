package lcy.tinympi4j.master;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.SerializationUtils;

import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.util.ClassLoaderUtil;
import lcy.tinympi4j.common.SplitableTask;


public class BigTask <T>{
	
	private static final Logger logger = Logger.getLogger(BigTask.class.getName()); 

	private String id;
	private String masterurl;
	private final Collection<T> totalresultset = new ArrayList<T>(100000);
	private final Map<String, Object[]> slavemap = new HashMap<String, Object[]>();
	private final Set<String> okslavetaskidset = new HashSet<String>();
	
	private static final Map<String, BigTask<?>> bigmap = new ConcurrentHashMap<String, BigTask<?>>();
	
	
	
	public static BigTask<?> findBigmap(String slavetaskid){
		return bigmap.get(slavetaskid.substring(0, 15));
		
	}
	
	private final ReadWriteLock okslavetaskidset_rwl = new ReentrantReadWriteLock();

	public void addTask2Slave(String slaveurl, Class<? extends SplitableTask> clazz, Serializable[] params) {
		addTask2Slave(slaveurl, clazz, params, 0);
	}
	
	public void addTask2Slave(String slaveurl, Class<? extends SplitableTask> clazz, Serializable[] params, Integer slaveto) {
		final String slavetaskid = String.format("%s-%s", id, RandomStringUtils.random(16, "abcdefghijklmnopqrstuvwxyz"));
		slavemap.put(slavetaskid, new Object[]{slaveurl, clazz, params, slaveto});
		logger.info(String.format("distribute subtask to %s, id = %s", slaveurl, slavetaskid));
	}
	

	public static <T> BigTask<T> create(String masterurl) {
		final BigTask<T> bigtask = new BigTask<T>();
		bigtask.id = RandomStringUtils.random(15, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		logger.info(String.format("create a task, id = %s", bigtask.id));
		bigtask.masterurl = masterurl;
		bigmap.put(bigtask.id, bigtask);
		return bigtask;
	}
	
	
	
	
	public Collection<T> executeAndWait(){
		return executeAndWait(0);
	}
	
	public Collection<T> executeAndWait(final Integer sendandread_to){
		
		final ExecutorService es = Executors.newFixedThreadPool(slavemap.size());
		final CompletionService<Boolean> completionService = new ExecutorCompletionService<Boolean>(es);  
		
		
		for(final String slavetaskid: slavemap.keySet()){
			
			completionService.submit(new Callable<Boolean>() {

				//slavemap.put(slavetaskid, new Object[]{slaveurl, clazz, params, slaveto});
				@Override
				public Boolean call() throws Exception {
					HttpRequest req = HttpRequest.put((String)slavemap.get(slavetaskid)[0]+"/addtask")
							.header("slaveto", slavemap.get(slavetaskid)[3].toString())
							.header("masterurl", masterurl)
							.header("slavetaskid", slavetaskid)
							.header("classbytes", jodd.util.Base64.encodeToString(IOUtils.toByteArray(ClassLoaderUtil.getClassAsStream((Class<?>)slavemap.get(slavetaskid)[1]))))
							.header("classname", ((Class<?>)slavemap.get(slavetaskid)[1]).getName())
							.body(SerializationUtils.serialize((Serializable)(slavemap.get(slavetaskid)[2])), "TINYMPI4J-PARAMS")
							.connectionTimeout(4000);
					
					if (sendandread_to != null && sendandread_to>0){
						req = req.timeout(sendandread_to*1000);
					}
					final HttpResponse res = req.send();
					return res.statusCode() == 200;
				}
			});
			
		}
		es.shutdown();
		
		 for(int i=0;i<slavemap.size();i++){
			try {
				if(!completionService.take().get())		return null;
			} catch ( ExecutionException e) {
				e.printStackTrace();
				return null;
			}catch ( InterruptedException e) {
				e.printStackTrace();
				return null;
			}
		 }
		 
		 
		 while(true){
				try {
					TimeUnit.MILLISECONDS.sleep(1L);
				} catch (InterruptedException e) {
					e.printStackTrace();
					return null;
				}
				
				okslavetaskidset_rwl.readLock().lock();
				try{
					if(okslavetaskidset.size() == slavemap.size())	return totalresultset;
				}finally{
					okslavetaskidset_rwl.readLock().unlock();
				}
				
			}
	            
	}
	
	public void onSlaveOk(String slavetaskid, Object result){
		
		okslavetaskidset_rwl.writeLock().lock();
		
		if(result instanceof Collection)
			totalresultset.addAll((Collection)result);
		else
			totalresultset.add((T) result);
		
		okslavetaskidset.add(slavetaskid);
		okslavetaskidset_rwl.writeLock().unlock();
	}

	public String getId() {
		return id;
	}
	
	
}
