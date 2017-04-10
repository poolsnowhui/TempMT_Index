package cn.edu.scnu.TempMT_Index.web.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.edu.scnu.TempMT_Index.service.QueryService;

public class QueryServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3631507824391353938L;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		System.out.println("ok*************************8");
		String atsql=request.getParameter("atsql");
		String lob=request.getParameter("lob");
		String isFromDisk=request.getParameter("isFromDisk");
		System.out.println("atsql="+atsql);
		System.out.println("lob"+lob);
		System.out.println("isFromDisk"+isFromDisk);
		QueryService queryService=new QueryService();
		String str=queryService.translate(atsql, lob,isFromDisk ,null);
		out.print(str);
	}
}
