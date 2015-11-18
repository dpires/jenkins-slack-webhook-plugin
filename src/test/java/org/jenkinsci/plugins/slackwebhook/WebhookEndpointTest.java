package org.jenkinsci.plugins.slackwebhook;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.WebResponse;
import static org.junit.Assert.assertEquals;
import org.jvnet.hudson.test.JenkinsRule;
import java.io.IOException;
import java.net.URL;
import net.sf.json.JSONObject;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.xml.sax.SAXException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jenkinsci.plugins.slackwebhook.model.SlackAttachment;

public class WebhookEndpointTest {

    private JenkinsRule.WebClient client;

    @Rule
    public final JenkinsRule jenkinsRule = new JenkinsRule();

    
    @Before
    public void setUp() throws IOException {
        client = jenkinsRule.createWebClient();
    }

    @Test
    public void testUnconfiguredSlackToken() throws IOException, SAXException {
        WebRequestSettings request =
            new WebRequestSettings(client.createCrumbedUrl("webhook/"), com.gargoylesoftware.htmlunit.HttpMethod.POST);
        WebResponse response = client.loadWebResponse(request);
        assertEquals(java.net.HttpURLConnection.HTTP_OK, response.getStatusCode());
        SlackAttachment a = new SlackAttachment("ERROR", "Slack token not set");
        SlackAttachment r = new ObjectMapper().readValue(response.getContentAsString(), SlackAttachment.class);
        assertEquals(a.getText(), r.getText());
    }
}
