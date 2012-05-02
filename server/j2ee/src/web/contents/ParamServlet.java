package web.contents;

import java.io.PrintWriter;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author wooyoowaan@gmail.com
 */
public class ParamServlet extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		//showParameter(request, response);
		httpdPage(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		//showParameter(request, response);
		httpdPage(request, response);
	}

	protected void httpdPage(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		PrintWriter writer;
		try {
			writer = response.getWriter();
		} catch (IOException e) {
			throw new ServletException(e);
		}
		writer.println("<html>");
		writer.println("<head><title>httpd</title></head>");
		writer.println("<body><h1>httpd</h1></body></html>");
	}

	protected void showParameter(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		PrintWriter writer;
		try {
			writer = response.getWriter();
		} catch (IOException e) {
			throw new ServletException(e);
		}
		writer.println("<html>");
		writer.println("<head><title>Parameter</title></head>");
		writer.println("<body><h1>Paramter</h1><table>");
		Map<String,String[]> map = request.getParameterMap();

		if (map.size() == 0) {
			writer.println("<tr><td>empty ... </td></tr>");
		} else {
			for (String key : map.keySet()) {
				writer.println("<tr><td>");
				writer.println(key);
				writer.println("</td><td>");
				boolean first = true;
				for (String value : map.get(key)) {
					if (first) {
						first = false;
					} else {
						writer.append(',');
					}
					writer.print(value);
				}
				writer.println("</td></tr>");
			}
		}
		writer.println("</table></body></html>");
	}

}