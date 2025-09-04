package org.javaee7.jaspic.jaccpropagation.jacc;

import static java.util.logging.Level.SEVERE;

import java.util.logging.Logger;

import javax.security.auth.Subject;
import jakarta.security.jacc.PolicyContext;
import jakarta.security.jacc.PolicyFactory;
import jakarta.security.jacc.WebResourcePermission;

/**
 * 
 * @author Arjan Tijms
 * 
 */
public class JACC {
    
    private final static Logger logger = Logger.getLogger(JACC.class.getName());
    
    public static Subject getSubject() {
        try {
            return (Subject) PolicyContext.getContext("javax.security.auth.Subject.container");
        } catch (Exception e) {
            logger.log(SEVERE, "", e);
        }
        
        return null;
    }

    public static boolean hasAccess(String uri, Subject subject) {
        return PolicyFactory
                .getPolicyFactory()
                .getPolicy()
                .implies(new WebResourcePermission(uri, "GET"), subject);
    }
}
