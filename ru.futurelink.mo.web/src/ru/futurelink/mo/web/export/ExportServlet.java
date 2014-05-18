/**
 * 
 */
package ru.futurelink.mo.web.export;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author pavlov
 *
 */
public class ExportServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private boolean isBinaryMode = true; 
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession mHttpSession = request.getSession();
		ExportController ctrl = (ExportController) mHttpSession.getAttribute("exportController");
		if (ctrl != null) {	
			String contentDisposition = ctrl.getView().getContentDisposition();
			String mimeType = ctrl.getView().getMimeType();
			InputStream input = ctrl.getView().getContents();

			if (mimeType != null) {
				response.setContentType(mimeType);
				if (mimeType.contains("text")) {
					isBinaryMode = false;
				}
			}
			if (contentDisposition != null) response.setHeader("Content-Disposition", contentDisposition);

			if (isBinaryMode) {
				int data = input.read();
				while(data != -1) {
					response.getWriter().write(data);
					data = input.read();
				}
				input.close();
			} else {
				BufferedReader in = new BufferedReader(new InputStreamReader(input, "UTF-8"));
				String readLine = null;
				while ((readLine = in.readLine()) != null) {
				    response.getWriter().print(readLine);
				}
			}
			
			response.getWriter().close();

			mHttpSession.removeAttribute("exportController");
		} else {
			response.getWriter().println("No data to export!");
		}
	}
}
