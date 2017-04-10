/**
 * 
 */
package cn.edu.scnu.TempMT_Index.web.action;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.edu.scnu.TempMT_Index.service.CreateService;

/**
 * @author CXH
 *
 */
// @WebServlet(name="/CreateLOBIndexServlet")
public class CreateLOBIndexServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8811417820033480241L;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding("UTF-8");
		req.setCharacterEncoding("UTF-8");

		resp.getWriter().println(CreateService.createLOBIndex());

		// resp.getWriter().println("创建LOB索引" + "成功！\n" + "总开销：" + total +
		// "ms");

	}

}
