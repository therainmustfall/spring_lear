package mis.files.shared;

// import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.layout.PatternLayout;
// import org.apache.logging.log4j.core.appender.RollingFileAppender;

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
	
	private FileAppender getFileAppender(String fileName) {
		FileAppender appender = null;
		
		PatternLayout pLayout = PatternLayout.newBuilder().withPattern("%-5p %c %t%n%29d - %m%n").build();
		appender = FileAppender.newBuilder()
						.setLayout(pLayout).withFileName(getServletContext().getRealPath(fileName)).withAppend(true)
						.build();
			// appender = new RollingFileAppender(pLayout, getServletContext().getRealPath(fileName), true);
			// appender.setMaxBackupIndex(5);
			// appender.setMaxFileSize("1MB");
		return appender;
	}
	
	private void initLogger(String name, FileAppender appender, Level level){
		Logger logger;
		if (name == null){
			logger = LogManager.getRootLogger();
		}else{
			logger = LogManager.getLogger(name);
		}
		logger.atLevel(level);
		// logger.addAppender(appender);
		logger.info("Starting " + logger.getName());
	}
	

}