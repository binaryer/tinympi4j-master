package lcy.tinympi4j.master;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;

import jodd.util.SystemUtil;

public class TomcatTool {
	
	 private static final Logger logger = Logger.getLogger(TomcatTool.class.getName()); 
	
	public static void startMasterTomcat(int port) {

		//fixed slow SessionIdGeneratorBase.createSecureRandom
		if(SystemUtil.isHostLinux())
			System.setProperty("java.security.egd", "file:/dev/./urandom");
		
		final Tomcat tomcat = new Tomcat();
		tomcat.setPort(port);
		tomcat.setBaseDir(System.getProperty("java.io.tmpdir")); 
		Context ctx = tomcat.addContext("", System.getProperty("java.io.tmpdir"));
		tomcat.getConnector().setAttribute("maxThreads", 30);
		tomcat.getConnector().setAttribute("acceptCount", 10);
		tomcat.getConnector().setAttribute("connectionTimeout", 5000);
		tomcat.getConnector().setAttribute("minSpareThreads", 2);
		tomcat.getConnector().setAttribute("maxSpareThreads", 5);
		{
			Tomcat.addServlet(ctx, "ok", new HttpServlet() {
				private static final long serialVersionUID = 2865185776464487549L;

				@Override
				protected void doPut(HttpServletRequest req, HttpServletResponse resp)
						throws ServletException, IOException {

					logger.info(String.format("subtask finished, id = %s", req.getHeader("slavetaskid")));
					
					BigTask.findBigmap(req.getHeader("slavetaskid")).onSlaveOk(req.getHeader("slavetaskid"),
							(Collection<?>) SerializationUtils.deserialize(IOUtils.toByteArray(req.getInputStream())));

					final Writer w = resp.getWriter();
					w.write("ok\n");
					w.flush();
					IOUtils.closeQuietly(w);
				}
			});
			ctx.addServletMappingDecoded("/ok", "ok");
		}
		
		try {
			tomcat.start();
		} catch (LifecycleException e) {
			e.printStackTrace();
		}
		
		
		logger.info(String.format("master started at port %d", port));
		new Thread(new Runnable() {

			@Override
			public void run() {
				tomcat.getServer().await();
			}
		}).start();
	}

}
