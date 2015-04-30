package search;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class SearchServlet extends HttpServlet {
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		
	}
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response){
		PrintWriter out;
		System.out.println("here");
		try {
			out = response.getWriter();
			out.println("<html><body>");
			out.println("<h1> Search page </h1>");
	    	out.println("<form action='user' method='POST'>");
	    	out.println("Username:<br>");
	    	out.println("<input type='text' name='Search:' value=''>");
	    	out.println("<br>");
	    	out.print("<input type='submit' value='Submit'>");
	    	out.println("</body></html>");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
