package cn.edu.scnu.TempMT_Index.web.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.edu.scnu.TempMT_Index.service.ShowService;

public class ShowServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4086454624887833070L;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("utf-8");
		resp.setContentType("text/html;charset=utf-8");

		System.out.println("ShowServlet");
		ShowService showService=new ShowService();
		String str = showService.tableToJson();
		
		PrintWriter out = resp.getWriter();
		out.print(str);
	}
}
