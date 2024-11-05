package mis.files;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.apache.poi.util.SystemOutLogger;

//import mis.files.shared.ButtonMethod;
//import mis.files.shared.ProBase;
import process.citing.files.shared.ProBase;
import process.citing.files.shared.ButtonMethod;

public class UpMisFiles extends ProBase {
	
   String uploadDir = servlet.getServletContext().getRealPath("/WEB-INF/uploads/");
   File   uploadFolder = new File(uploadDir);
   

	public UpMisFiles(HttpServlet srvlet, HttpServletRequest req, HttpServletResponse resp) {
    	super(srvlet, req, resp);
	}

	@Override
	protected void copyFromSession(Object obj) { return; }
	
	protected void doPost() throws ServletException, IOException {
		// TODO Auto-generated method stub
//		System.out.println(uploadFolder.getAbsolutePath());
		req.setCharacterEncoding("utf-8");
		resp.setCharacterEncoding("utf-8");
		resp.setContentType("text/html; charset=UTF-8");
        int files_number = uploadFolder.list() == null? 0: uploadFolder.list().length;
		req.getSession().setAttribute("filesnumber", files_number);
		String add = executeButtonMethod();
		RequestDispatcher dispatcher = req.getRequestDispatcher(add);
		dispatcher.forward(req, resp);
	}
	
	@ButtonMethod(buttonName = "homePage")
	public String home()
	{
		return "upload.jsp";
	}
	
	@ButtonMethod(buttonName = "uploadfiles",isDefault = true)
	public String upload() throws UnsupportedEncodingException
	{
		if (!uploadFolder.exists()) uploadFolder.mkdir();
		String result = "";
		boolean isMultipart = ServletFileUpload.isMultipartContent(req);
		resp.setContentType("text/html");
		if (isMultipart){
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			String upload_message = "";
			try {
				List<FileItem> items = upload.parseRequest(req);
				Iterator<FileItem> iterator = items.iterator();
				while (iterator.hasNext()){
					FileItem item = iterator.next();
					if(!item.isFormField()){
						String fileName = item.getName().substring(item.getName().lastIndexOf("\\") + 1);
						File upload_paperlist = new File(uploadDir + "/" + fileName);
						if(fileName != "") item.write(upload_paperlist);
						else               upload_message = "未上传文件";
					 }else  logger.info("未上传文件。");
				}
				req.getSession().setAttribute("upload_message", upload_message);
				result = "upload.jsp";
				
			} catch (FileUploadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			result = "upload.jsp";
		}
		list_files(req, uploadFolder);
		return result;
	}
	@ButtonMethod(buttonName = "clearReports")
	public String clear() throws UnsupportedEncodingException
	{
		File uploadFolder = new File(uploadDir);
		for(File f: uploadFolder.listFiles()){
			f.delete();
		}
		list_files(req, uploadFolder);
		String add = "upload.jsp";
		logger.info("文件夹已清理");
		return add;
	}
	void list_files(HttpServletRequest req, File report_path) throws UnsupportedEncodingException{
		List<String> list = new ArrayList<>();
		if (report_path.list() != null && report_path.list().length > 0) {
			for(File f: report_path.listFiles()){
				System.out.println(new String(f.getName().getBytes("ISO-8859-1"),"utf-8"));
				list.add(f.getName());
		}
		}
		req.getSession().setAttribute("files", list);
		logger.info("session reports info setted");
	}
}

