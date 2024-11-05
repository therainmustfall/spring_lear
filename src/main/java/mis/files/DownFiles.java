package mis.files;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/download"})
public class DownFiles extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		resp.setCharacterEncoding("utf-8");
		resp.setContentType("text/html; charset=utf-8");
		req.setCharacterEncoding("utf-8");
		System.out.println(URLDecoder.decode(req.getParameter("filename"),"utf-8"));
		String  fileName = req.getParameter("filename");
		resp.addHeader("content-Type", "application/octet-stream");
		
		String agent = req.getHeader("User-Agent");
		if (agent.toLowerCase().indexOf("chrome") > 0)
		{
			resp.addHeader("content-disposition", "attachment;filename=" + new String(fileName.getBytes("UTF-8"),"ISO8859-1"));
		} else
		{
			resp.addHeader("content-disposition", "attachment; filename=" + URLEncoder.encode(fileName,"utf-8"));
		}
		System.out.println(this.getServletContext().getRealPath("/WEB-INF/uploads/") + fileName);
		FileInputStream fis = new FileInputStream(this.getServletContext().getRealPath("/WEB-INF/uploads/") + fileName);
		ServletOutputStream out = resp.getOutputStream();
		int len = 0;
		byte[] buf = new byte[1024];	
		while((len = fis.read(buf)) != -1){
			out.write(buf, 0, len);
		}
		fis.close();
	}
}
