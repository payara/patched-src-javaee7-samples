package org.javaee7.jacc.contexts.servlet;

import static java.util.Collections.list;

import java.io.IOException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.Subject;

import jakarta.security.jacc.PolicyContext;
import jakarta.security.jacc.PolicyContextException;
import jakarta.security.jacc.PolicyFactory;
import jakarta.security.jacc.WebRoleRefPermission;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This Servlet demonstrates both how to obtain the Subject and then how to retrieve the roles from
 * this Subject.
 * 
 * @author Arjan Tijms
 * 
 */
@WebServlet(urlPatterns = "/subjectServlet")
public class SubjectServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            Subject subject = (Subject) PolicyContext.getContext("javax.security.auth.Subject.container");

            if (subject != null) {
                response.getWriter().print("Obtained subject from context.\n");

                // Get the permissions associated with the Subject we obtained
                PermissionCollection permissionCollection = getPermissionCollection(subject);

                // Resolve any potentially unresolved permissions
                permissionCollection.implies(new WebRoleRefPermission("", "nothing"));

                // Filter just the roles from all the permissions, which may include things like 
                // java.net.SocketPermission, java.io.FilePermission, and obtain the actual role names.
                Set<String> roles = filterRoles(request, permissionCollection);

                for (String role : roles) {
                    response.getWriter().print("User has role " + role + "\n");
                }
            }
        } catch (PolicyContextException e) {
            e.printStackTrace(response.getWriter());
        }
    }

    private PermissionCollection getPermissionCollection(Subject subject) {
        return PolicyFactory.getPolicyFactory().getPolicy().getPermissionCollection(subject);
    }

    private Set<String> filterRoles(HttpServletRequest request, PermissionCollection permissionCollection) {
        Set<String> roles = new HashSet<>();
        for (Permission permission : list(permissionCollection.elements())) {
            if (permission instanceof WebRoleRefPermission) {
                String role = permission.getActions();

                // Note that the WebRoleRefPermission is given for every Servlet in the application, even when
                // no role refs are used anywhere. This will also include Servlets like the default servlet and the
                // implicit JSP servlet. So if there are 2 application roles, and 3 application servlets, then 
                // at least 6 WebRoleRefPermission elements will be present in the collection.
                if (!roles.contains(role) && request.isUserInRole(role)) {
                    roles.add(role);
                }
            }
        }

        if (!roles.contains("**") && request.isUserInRole("**")) {
            roles.add("**");
        }

        return roles;
    }

}
