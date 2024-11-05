package mis.files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

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

import connRe.Person;
import connRe.RepRet;
//import mis.files.shared.ButtonMethod;
//import mis.files.shared.ProBase;
import process.citing.files.shared.ProBase;
import process.citing.files.shared.ButtonMethod;

public class TitleSearch extends ProBase {
	
   String uploadDir = servlet.getServletContext().getRealPath("/WEB-INF/uploads/");
   File   uploadFolder = new File(uploadDir);
   

	public TitleSearch(HttpServlet srvlet, HttpServletRequest req, HttpServletResponse resp) {
    	super(srvlet, req, resp);
	}
	
	@Override 
	protected void copyFromSession(Object sessionObj) { return;}
	protected void doPost() throws ServletException, IOException {
		// TODO Auto-generated method stub
//		System.out.println(uploadFolder.getAbsolutePath());
		req.setCharacterEncoding("utf-8");
		resp.setCharacterEncoding("utf-8");
		resp.setContentType("text/html; charset=UTF-8");
        int files_number = uploadFolder.list() == null? 0: Objects.requireNonNull(uploadFolder.list()).length;
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
	public String upload()
	{
		if (!uploadFolder.exists()) uploadFolder.mkdir();
		String result = "";
		boolean isMultipart = ServletFileUpload.isMultipartContent(req);
		resp.setContentType("text/html");
		
		List<String> uts_id       = new ArrayList<>();
		List<Person> psn_if       = new ArrayList<>();
		String  	 sYear  = "";
		String       eYear   = "";
		
		if (isMultipart){
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			String upload_message = "";
					
			try {
				List<FileItem> items = upload.parseRequest(req);
				logger.info("items size: " + items.size());
				for (FileItem item : items) {
					logger.info("field name 0: " + item.getName() + " is formfield: " + item.isFormField());
					if (!item.isFormField()) {
						logger.info("field name: "+ item.getFieldName());
						String fileName = item.getName().substring(item.getName().lastIndexOf("\\") + 1);
						if (!fileName.isEmpty())
						{
							if (item.getFieldName().equals("uts_file")) {
								File upload_paperlist = new File(uploadDir + fileName);
								logger.info("uts file name: " + uploadDir + fileName);
								logger.info("uts file isFile: " + upload_paperlist.isFile());
								// if (!upload_paperlist.exists()) upload_paperlist.createNewFile();
								// logger.info("uts file isFile: " + upload_paperlist.isFile());
								item.write(upload_paperlist);
								logger.info("uts file isFile: " + upload_paperlist.isFile());
								uts_id = Files.readAllLines(Path.of(upload_paperlist.getAbsolutePath()),
										StandardCharsets.UTF_8);
								logger.info("读取入藏号文件: " + uploadDir + fileName + " " + uts_id.size() + "条");
								Files.deleteIfExists(Path.of(upload_paperlist.getAbsolutePath()));
							}
							if (item.getFieldName().equals("resear_group_file")) {
								File upload_paperlist = new File(uploadDir + fileName);

								item.write(upload_paperlist);
								List<String> persons = Files.readAllLines(Path.of(upload_paperlist.getAbsolutePath()),
										StandardCharsets.UTF_8);
								
								psn_if = new ArrayList<Person>();
								// lines.stream().forEach(rec -> { psn.add()});
								// Scanner scanner = new Scanner(new File(args[0]));
								// while (scanner.hasNextLine())
								for (String rec : persons) {
									System.out.println(rec);
									String[] recs = rec.split("\t");
									System.out.println(recs[0]);
									Person person = new Person();
									person.setYear(recs[0].trim());
									person.setName(recs[1].trim());
									person.setStat(recs[2].trim());
									person.setDept(recs[3].trim());
									person.setGender(recs[4].trim());
									person.setBirth(recs[5].trim());
									person.setEmail(recs[6].trim());
									psn_if.add(person);
								}
								Files.deleteIfExists(Path.of(upload_paperlist.getAbsolutePath()));
								logger.info("读取入人员信息文件: " + uploadDir + fileName);
							}		
						}	
						
					} else {
						logger.info("读取表单值 -" + item.getFieldName() + ": " + item.getString("utf8"));
						if (item.getFieldName().equals("year_range") && !item.getString("utf8").isEmpty()) {
							String[] years = item.getString("utf8").split("-");
							sYear = years[0].trim();
							eYear = years[1].trim();
							System.out.println(sYear);
							System.out.println(eYear);
						}
					}
				}
				
				logger.info("人员信息： "  + (psn_if.isEmpty() ? 0 : psn_if.size()));
				logger.info("入藏号信息: " + (uts_id.isEmpty() ? 0 : uts_id.size()));
				System.out.println("起始年: " + sYear + ", 结束年： " + eYear);
				logger.info("起始年: " + sYear + ", 结束年： " + eYear);
				
				RepRet repret = new RepRet(psn_if, uts_id, sYear, eYear);
				repret.getTable(uploadDir);
				logger.info("查询读取数据库");
				logger.info("保存文件至" + uploadDir);
				
				req.getSession().setAttribute("upload_message", upload_message);
				result = "upload.jsp";
				
			} catch (FileUploadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.info(e.getMessage());
				e.printStackTrace();
			}
			
		} else{
			result = "upload.jsp";
		}
		return result;
	}
	@ButtonMethod(buttonName = "clearReports")
	public String clear()
	{
		File uploadFolder = new File(uploadDir);
		for(File f: Objects.requireNonNull(uploadFolder.listFiles())){
			f.delete();
		}
		String add = "upload.jsp";
		logger.info("文件夹已清理");
		return add;
	}
}

