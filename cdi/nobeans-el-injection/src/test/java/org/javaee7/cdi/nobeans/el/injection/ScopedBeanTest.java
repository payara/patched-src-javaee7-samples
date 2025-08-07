package org.javaee7.cdi.nobeans.el.injection;

import java.io.File;
import java.net.URL;

import org.htmlunit.NicelyResynchronizingAjaxController;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author Arun Gupta
 */
@RunWith(Arquillian.class)
public class ScopedBeanTest {

    private static final String WEBAPP_SRC = "src/main/webapp";

    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive deploy() {
        return ShrinkWrap.create(WebArchive.class)
            .addClass(ScopedBean.class)
            .addAsWebInfResource((new File(WEBAPP_SRC + "/WEB-INF", "web.xml")))
            .addAsWebResource((new File(WEBAPP_SRC, "index.xhtml")));
    }

    @Test
    public void checkRenderedPage() throws Exception {
        try (WebClient webClient = new WebClient()) {
            webClient.getOptions().setJavaScriptEnabled(true);
            // Make sure to wait for AJAX requests in the foreground
            webClient.setAjaxController(new NicelyResynchronizingAjaxController());
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            HtmlPage page = webClient.getPage(base + "/faces/index.xhtml");
            assert (page.asNormalizedText().contains("Hello there!"));
        }
    }
}
