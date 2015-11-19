package org.jenkinsci.plugins.slackwebhook;


import org.junit.Rule;
import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.assertEquals;

import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebRequestSettings;

import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.jenkinsci.plugins.slackwebhook.model.SlackTextMessage;




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
            new WebRequestSettings(client.createCrumbedUrl("webhook/"),
            com.gargoylesoftware.htmlunit.HttpMethod.POST);

        WebResponse response = client.loadWebResponse(request);

        assertEquals(java.net.HttpURLConnection.HTTP_OK, response.getStatusCode());

        SlackTextMessage a = new SlackTextMessage("Slack token not set");

        SlackTextMessage r = new ObjectMapper()
            .readValue(response.getContentAsString(), SlackTextMessage.class);

        assertEquals(a.getText(), r.getText());
    }
}
