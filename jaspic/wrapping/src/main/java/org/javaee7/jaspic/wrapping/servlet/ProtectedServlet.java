package org.javaee7.jaspic.wrapping.servlet;

import java.io.IOException;
import java.io.Writer;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This Servlet tests that the request and response objects it receives are the ones marked as wrapped by the SAM that executed
 * before the Servlet was called.
 * 
 * @author Arjan Tijms
 * 
 */
@WebServlet(urlPatterns = "/protected/servlet")
public class ProtectedServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Writer writer = response.getWriter();

        writer.write("servlet request isWrapped: " + request.getAttribute("isWrapped"));
        writer.write("\n");
        writer.write("servlet response isWrapped: " + response.getHeader("isWrapped"));
    }

}
