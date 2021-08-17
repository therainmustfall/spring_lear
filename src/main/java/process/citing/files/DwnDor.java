package process.citing.files;

import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/download")
public class DwnDor extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String  fileName = req.getParameter("filename");
		RepProHub rp = (RepProHub) req.getSession().getAttribute("hub");
		String root = rp.getRprt().getAbsolutePath();
		String realPath = root + "/" + fileName;
		
		resp.setHeader("content-disposition", "attachment; filename=" + URLEncoder.encode(fileName,"utf-8"));
		
		FileInputStream fis = new FileInputStream(realPath);
		int len = 0;
		byte[] buf = new byte[1024];	
		while((len = fis.read(buf)) != -1){
			resp.getOutputStream().write(buf, 0, len);
		}
		fis.close();
	}
}
