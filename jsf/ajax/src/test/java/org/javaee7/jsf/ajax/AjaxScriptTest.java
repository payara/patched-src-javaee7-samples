package org.javaee7.jsf.ajax;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;

//import javax.xml.registry.infomodel.User;

import org.htmlunit.NicelyResynchronizingAjaxController;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that a script section of a page can be reloaded via AJAX when containing an empty string
 * See CUSTCOM-253
 */
@RunWith(Arquillian.class)
public class AjaxScriptTest {

    @ArquillianResource
    private URL base;

    private HtmlPage page;

    @Before
    public void setup() throws IOException {
        try (WebClient webClient = new WebClient()) {
            // Make sure to wait for AJAX requests in the foreground
            webClient.setAjaxController(new NicelyResynchronizingAjaxController());
            page = webClient.getPage(base + "/faces/index.xhtml");
        }
    }

    @Test
    public void when_ajax_button_clicked_expect_no_errors() throws IOException, InterruptedException {
        // Click the link
        page.getElementById("form:link").click();
        // Check that the AJAX request was successful
        assertEquals("Successful AJAX request", page.getElementById("message").getTextContent());
    }

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        final String webappDirectory = "src/main/webapp";
        final String webInfDirectory = webappDirectory + "/WEB-INF";
        return ShrinkWrap.create(WebArchive.class)
            .addClass(EmptyValues.class)
            .addAsWebResource(new File(webappDirectory, "index.xhtml"))
            .addAsWebInfResource(new File(webInfDirectory, "web.xml"));
    }
}
