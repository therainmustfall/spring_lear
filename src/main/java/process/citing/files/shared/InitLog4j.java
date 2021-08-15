package process.citing.files.shared;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.springframework.stereotype.Component;

@Component
public class InitLog4j extends HttpServlet{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		String initPath = getInitParameter("logPath");
		String logPath = "/WEB-INF/logs/error.log";
		if(initPath != null) logPath = initPath;
		FileAppender appender = getFileAppender(logPath);
		if(appender == null) return;
		initLogger(null, appender, Level.ERROR);
		initLogger("Log Debug", appender, Level.DEBUG);
		initLogger("org.apache.commons.beanutils", appender, Level.DEBUG);
	}
	
	private FileAppender getFileAppender(String fileName){
		RollingFileAppender appender = null;
		try{
			appender = new RollingFileAppender(new PatternLayout("%-5p %c %t%n%29d - %m%n"), getServletContext().getRealPath(fileName)
					, true);
			appender.setMaxBackupIndex(5);
			appender.setMaxFileSize("1MB");
		}catch(IOException ex){
			System.out.println(
					"Could not create appender for " +
							fileName + ": " + 
							ex.getMessage()
					);
		}
		return appender;
	}
	
	private void initLogger(String name, FileAppender appender, Level level){
		Logger logger;
		if (name == null){
			logger = Logger.getRootLogger();
		}else{
			logger = Logger.getLogger(name);
		}
		logger.setLevel(level);
		logger.setAdditivity(false);
		logger.addAppender(appender);
		logger.info("Starting " + logger.getName());
	}
	

}