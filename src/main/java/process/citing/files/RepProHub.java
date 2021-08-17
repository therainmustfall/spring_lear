package process.citing.files;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.docx4j.openpackaging.exceptions.Docx4JException;

import org.springframework.stereotype.Component;
import process.citing.files.shared.ButtonMethod;
import process.citing.files.shared.ProBase;

public class RepProHub extends ProBase {
	
    @Override
	public void copyFromSession(Object sessionHub) {
        if (sessionHub.getClass() == this.getClass()) {
        	RepProHub rp = (RepProHub) sessionHub;
            rndfd = rp.getRndfd();
            upload_path = rp.getUpldP();
            papers_path = rp.getPprP();
            auplist     = rp.getAupl();
            report_path = rp.getRprt();
            jiFile      = rp.getJ();
        }

    }
   
    private   Map<String, String> reportType;
    private   String              rndfd;
    private   File                upload_path;    
    private   File 	              papers_path;
    private   File                auplist;
    private   File                report_path;
    private   File                jiFile;
	public RepProHub() {}
	public RepProHub(HttpServlet srvlet, HttpServletRequest req, HttpServletResponse resp) {
    	super(srvlet, req, resp);
    	this.reportType = new HashMap<>();
    	this.rndfd      = "";
	}
	public String getRndfd() {
		return rndfd;
	}
	public File getUpldP() {
		return upload_path;
	}
	public File getPprP() {
		return papers_path;
	}
	public File getAupl() {
		return auplist;
	}
	public File getRprt() {
		return report_path;
	}
	public File getJ() {
		return jiFile;
	}

	public void refreshPaths() throws IOException {	
		String root = servlet.getServletContext().getRealPath("/WEB-INF");
		jiFile = new File(root + "/report/jif.txt");
		rndfd = root + "/sdata/" + (int) (Math.random()*100);
		File rdf = new File(rndfd);
		while(rdf.exists()){
			rndfd = root + "/sdata/" + (int) (Math.random()*100);
			rdf = new File(rndfd);
		}
		rdf.mkdirs();
		
		upload_path = new File(rndfd + "/uploads");
		if(!upload_path.exists()){
			upload_path.mkdirs();
		}

		papers_path = new File(rndfd + "/papers" );
		if(!papers_path.exists()){
			papers_path.mkdirs();
		}
		auplist = new File(rndfd + "/papers/savedrecs.txt");
		
		report_path = new File(rndfd + "/report");
		if(!report_path.exists()) {
			report_path.mkdirs();
			System.out.println(report_path);
		}
		
		logger.info("new paths setted.");
	}
	
	protected void doPost() throws ServletException, IOException {
		// TODO Auto-generated method stub
		addHubToSession("hub", SessionData.READ);
		if (rndfd.equals("")) refreshPaths();
		
        int citing_files_number = upload_path.list().length;
		if (auplist.exists())	req.getSession().setAttribute("paperlist", 1);
		else					req.getSession().setAttribute("paperlist", 0);
		req.getSession().setAttribute("citing_files", citing_files_number);
		String add = executeButtonMethod();
		RequestDispatcher dispatcher = req.getRequestDispatcher(add);
		dispatcher.forward(req, resp);
	}
	
	@ButtonMethod(buttonName = "homePage")
	public String home()
	{
		return "FileUpload.jsp";
	}
	
	@ButtonMethod(buttonName = "uploadRecords",isDefault = true)
	public String upload()
	{
//		System.out.println("uploading...");
		String result = "";
		boolean isMultipart = ServletFileUpload.isMultipartContent(req);
		resp.setContentType("text/html");
		if (isMultipart){
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			String upload_message = "文件成功上传";
			try {
				List<FileItem> items = upload.parseRequest(req);
				Iterator<FileItem> iterator = items.iterator();
				while (iterator.hasNext()){
					FileItem item = iterator.next();
					if(!item.isFormField()){
						String fileName = item.getName().substring(item.getName().lastIndexOf("\\") + 1);
						if (fileName.contains("savedrecs.txt")) 
						{
							File upload_paperlist = new File(papers_path + "/" + fileName);
							if(fileName != "") item.write(upload_paperlist);
							else               upload_message = "没有找到文件。";
						}else{
							File uploadedFile = new File(upload_path + "/" + fileName);
							
							if(fileName != ""){
									logger.info("UPloaded " +  fileName);
								    item.write(uploadedFile);
								    upload_message = "文件已上传。";
							}else	upload_message = "文件没有上传，需确认已提前上传";
						}
					}else  logger.info("不是表单中的字段。");
				}
				req.getSession().setAttribute("upload_message", upload_message);
				result = "Process.jsp";
				
			} catch (FileUploadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			result = "FileUpload.jsp";
			list_report(req, report_path);
		}
		return result;
	}
	
	@ButtonMethod(buttonName = "process")
	public String process() throws IOException
	{
		String result = "";
		PnRc m = new PnRc(jiFile.getAbsolutePath(), auplist, upload_path.getAbsolutePath());
		if(req.getParameterMap().containsKey("jif"))				reportType.put("jif","good");
		if(req.getParameterMap().containsKey("corre_author"))		reportType.put("corre_author","good");		
		if(req.getParameterMap().containsKey("detail_list"))		reportType.put("detail_list","good");
		if(req.getParameterMap().containsKey("first_author")) 		reportType.put("first_author","good");

		if(req.getParameter("apply_author") != null && !req.getParameter("apply_author").equals(""))	
		{
			reportType.put("apply_author",req.getParameter("apply_author"));
			System.out.println("需要委托作者过滤"+ req.getParameter("apply_author").length());
		}
		if(req.getParameter("resear_group") != null && !req.getParameter("resear_group").equals(""))	
		{
			reportType.put("resear_group",req.getParameter("resear_group"));
			System.out.println("需要课题组过滤"+ req.getParameter("resear_group").length());
		}
		m.getData(reportType);
		try {
			ListGen updatelist = new ListGen(m, report_path.getAbsolutePath());
			updatelist.generateList(m, reportType);
		} catch (Docx4JException e) {
			e.printStackTrace();
		}
		list_report(req, report_path);
		logger.info("Process completed");
		result = "Download.jsp";
		return result;
	}
	
	@ButtonMethod(buttonName = "clearReports")
	public String clear()
	{
		for(File f: report_path.listFiles()){
			if (f.getName().contains("jif") || f.getName().contains("template"))	continue;
			else  f.delete();
		}
		list_report(req, report_path);
		
		for(File uFile : upload_path.listFiles()){
			uFile.delete();
		}
		if(auplist.exists()) auplist.delete();
		
		String add = "FileUpload.jsp";
		logger.info("results folder cleared");
		return add;
	}
	void list_report(HttpServletRequest req, File report_path){
		List<String> list = new ArrayList<>();
		System.out.println(report_path);
		if (report_path.list().length > 0) {
			for(File f: report_path.listFiles()){
				if(f.getName().contains("jif") || f.getName().contains("template")){
				continue;
			}
			list.add(f.getName());
		}
		}
		req.getSession().setAttribute("reports", list);
		logger.info("session reports info setted");
	}
}
