package process.citing.files.shared;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

public abstract class ProBase {
	
	protected HttpServlet         servlet;
	protected HttpServletRequest  req;
	protected HttpServletResponse resp;
	private   Method              methodDefault = null;
	protected Logger logger;
	public ProBase() {}
	public ProBase(HttpServlet srvlet, HttpServletRequest rqt, HttpServletResponse rsp)
	{
		this.servlet = srvlet;
		this.req     = rqt;
		this.resp    = rsp;
		initLogger();
	}
	
	protected void initLogger(){
		String logName = "info.reportprocess";
		String initName = servlet.getInitParameter("logName");
		if(initName != null) logName = initName;
		
		Level logLevel = Level.DEBUG;
		String strLevel = servlet.getInitParameter("logLevel");
		if(strLevel != null){
			logLevel = Level.toLevel(strLevel);
		}
		
		logger = Logger.getLogger(logName);
		logger.setLevel(logLevel);
	}
	
	protected enum SessionData {
        READ,
        IGNORE
    };

    public void addHubToSession(String name,
            SessionData state) {
        if (SessionData.READ == state) {
            Object sessionObj =
                    req.getSession().getAttribute(name);
            if (sessionObj != null) {
                copyFromSession(sessionObj);
            }
        }
        req.getSession().setAttribute(name, this);
    }
    
    protected abstract void  copyFromSession(Object seessionObj);
    
    protected String executeButtonMethod() throws IOException, ServletException
    {
//    	System.out.println(req.getParameterMap().keySet());
    	String result = "";
    	methodDefault = null;
    	Class<?> clazz = this.getClass();
    	Class<?> enclosingClass = clazz.getEnclosingClass();
    	while (enclosingClass != null)
    	{
    		clazz = this.getClass();
    		enclosingClass = clazz.getEnclosingClass();
    	}
    	
    	try {
    		result = executeButtonMethod(clazz, true);
    	} catch (Exception ex)
    	{
    		writeError(req, resp, "Button Method Error.", ex);
    		return "";
    	}
    	return result;
    }
    protected String executeButtonMethod(Class<?> clazz, boolean srchFdft) throws IllegalAccessException, InvocationTargetException
    {
    	String result = "";
    	if (clazz == null) return "";
    	Method[] methods = clazz.getDeclaredMethods();
    	for (Method method : methods)
    	{
    		ButtonMethod ann = method.getAnnotation(ButtonMethod.class);
    		if (ann != null)
    		{
    			if (srchFdft && ann.isDefault())	methodDefault = method;
    			if (req.getParameter(ann.buttonName()) != null)
    			{
    				result = invokeButtonMethod(method);
    				break;
    			}
    		}
    	}
    	
    	if (result.equals(""))
    	{
    		Class<?> superClass = clazz.getSuperclass();
    		result = executeButtonMethod(superClass, methodDefault == null);
    		if (result.equals(""))
    		{
    			if (methodDefault != null)
    			{
    				result  = invokeButtonMethod(methodDefault);
    			} else {
    				logger.error("(executeButtonMethod) No Default method was specified, but one was needed.");
    				result = "No Default Method Specified.";
    			}
    		}
    	}
    	return result;
    }
    
    protected String invokeButtonMethod(Method method) throws IllegalAccessException, InvocationTargetException
    {
    	String result = "";
    	try {
    		result = (String) method.invoke(this, (Object[]) null);
    	} catch (IllegalAccessException iae) {
    		throw iae;
    	} catch (InvocationTargetException ite) {
    		throw ite;
    	}
    	return result;
    }

    static public void writeError(
            javax.servlet.http.HttpServletRequest request,
            javax.servlet.http.HttpServletResponse response,
            String title,
            Exception ex)
            throws IOException, ServletException {
        java.io.PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        out.println("<html>");
        out.println("  <head>");
        out.println("    <title>" + title + "</title>");
        out.println("  </head>");
        out.println("  <body>");
        out.println("<h2>" + title + "</h2>");
        if (ex.getMessage() != null) {
            out.println("    <h3>" + ex.getMessage() + "</h3>");
        }
        if (ex.getCause() != null) {
            out.println("    <h4>" + ex.getCause() + "</h4>");
        }
        StackTraceElement[] trace = ex.getStackTrace();
        if (trace != null && trace.length > 0) {
            out.print("<pre>");
        }
        ex.printStackTrace(out);
        out.println("</pre>");
        out.println("  </body>");
        out.println("</html>");
        out.close();
    }
}
