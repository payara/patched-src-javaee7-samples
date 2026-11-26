package org.javaee7;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * The embedded Payara server doesn't run Mojarra's FacesInitializer for the deployed applications, so Faces is never
 * initialized and the Faces Servlet fails to load. For the embedded server only, this registers Mojarra's
 * ConfigureListener in the web.xml of web archives that declare the Faces Servlet.
 */
public class EmbeddedFacesListenerExtension implements LoadableExtension {

    private static final String WEB_XML = "WEB-INF/web.xml";
    private static final String FACES_SERVLET = "javax.faces.webapp.FacesServlet";
    private static final String CONFIGURE_LISTENER = "com.sun.faces.config.ConfigureListener";

    @Override
    public void register(ExtensionBuilder builder) {
        if ("payara-embedded".equals(System.getProperty("javaEEServer"))) {
            builder.observer(FacesListenerObserver.class);
        }
    }

    /**
     * Observes the deployment rather than using an ApplicationArchiveProcessor, as the latter is not called for
     * archives that are not testable (i.e. deployed with testable = false).
     */
    public static class FacesListenerObserver {

        public void beforeDeploy(@Observes BeforeDeploy event) {
            Archive<?> archive = event.getDeployment().getArchive();
            if (!(archive instanceof WebArchive)) {
                return;
            }

            Node node = archive.get(WEB_XML);
            if (node == null || node.getAsset() == null) {
                return;
            }

            String webXml = read(node);
            if (!webXml.contains(FACES_SERVLET) || webXml.contains(CONFIGURE_LISTENER)) {
                return;
            }

            int servlet = webXml.indexOf("<servlet>");
            if (servlet < 0) {
                return;
            }

            String listener = "<listener><listener-class>" + CONFIGURE_LISTENER + "</listener-class></listener>\n    ";
            archive.delete(WEB_XML);
            archive.add(new StringAsset(webXml.substring(0, servlet) + listener + webXml.substring(servlet)), WEB_XML);
        }

        private static String read(Node node) {
            try (InputStream in = node.getAsset().openStream()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                return new String(out.toByteArray(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new IllegalStateException("Could not read " + WEB_XML, e);
            }
        }
    }
}
