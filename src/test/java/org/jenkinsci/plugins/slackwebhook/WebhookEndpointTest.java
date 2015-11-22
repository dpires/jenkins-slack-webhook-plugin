package org.jenkinsci.plugins.slackwebhook;


import org.junit.Rule;
import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.assertEquals;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.containsString;

import static org.junit.Assert.assertThat;

import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import static com.gargoylesoftware.htmlunit.HttpMethod.POST;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;

import hudson.tasks.Shell;

import org.jvnet.hudson.test.JenkinsRule;

import com.fasterxml.jackson.databind.ObjectMapper;

import static java.net.HttpURLConnection.HTTP_OK;

import org.apache.commons.httpclient.NameValuePair;

import java.util.List;
import java.util.ArrayList;

import hudson.model.FreeStyleProject;
import jenkins.model.GlobalConfiguration;

import org.jenkinsci.plugins.slackwebhook.model.SlackTextMessage;




public class WebhookEndpointTest {

    private JenkinsRule.WebClient client;

    private final String ENDPOINT = "webhook/";

    private List<NameValuePair> data;

    @Rule
    public final JenkinsRule jenkinsRule = new JenkinsRule();

    
    @Before
    public void setUp() throws Exception {
        client = jenkinsRule.createWebClient();
        data = new ArrayList<NameValuePair>();
        data.add(new NameValuePair("token", "GOOD_TOKEN"));
        data.add(new NameValuePair("trigger_word", "jenkins")); 
    }

    @Test
    public void testUnconfiguredSlackToken() throws Exception {
        WebResponse response = makeRequest(null);

        assertThat(response.getStatusCode(), is(HTTP_OK));
        assertThat(getSlackMessage(response).getText(), is("Slack token not set"));
    }

    @Test
    public void testInvalidConfiguredSlackToken() throws Exception {
        GlobalConfig config = GlobalConfiguration.all().get(GlobalConfig.class);        
        assertThat(config.getSlackOutgoingWebhookToken(), is(nullValue()));

        setConfigSettings();

        List<NameValuePair> badData = new ArrayList<NameValuePair>();
        badData.add(new NameValuePair("token", "BAD_TOKEN"));

        WebResponse response = makeRequest(badData);
        
        assertThat(getSlackMessage(response).getText(), is("Invalid Slack token"));
    }

    @Test
    public void testListProjects() throws Exception {
        setConfigSettings(); 
        data.add(new NameValuePair("text", "jenkins list projects")); 
        WebResponse response = makeRequest(data);
        assertThat(getSlackMessage(response).getText(), is("*Projects:*\n>_No projects found_"));

        FreeStyleProject project = jenkinsRule.createFreeStyleProject("PROJECT_1"); 
        Thread.sleep(30);
        response = makeRequest(data);
        assertThat(getSlackMessage(response).getText(), is("*Projects:*\n>*PROJECT_1*\n>*Last Build:* #TBD\n>*Status:* TBD\n\n\n"));

        project.scheduleBuild2(0);
        Thread.sleep(200);
        response = makeRequest(data);
        assertThat(getSlackMessage(response).getText(), is("*Projects:*\n>*PROJECT_1*\n>*Last Build:* #1\n>*Status:* SUCCESS\n\n\n"));
    }

    @Test
    public void testRunNonExistantProject() throws Exception {
        setConfigSettings();
        data.add(new NameValuePair("text", "jenkins run project-1"));
        WebResponse response = makeRequest(data);
        assertThat(getSlackMessage(response).getText(), is("Could not find project (project-1)\n"));
    }

    @Test
    public void testRunProject() throws Exception {
        setConfigSettings();
        FreeStyleProject project = jenkinsRule.createFreeStyleProject("project-1"); 
        Thread.sleep(30);
        data.add(new NameValuePair("text", "jenkins run project-1"));
        WebResponse response = makeRequest(data);
        assertThat(getSlackMessage(response).getText(), is("Build scheduled for project project-1\n"));
    }

    @Test
    public void testGetProjectBuildLogWithNonExistantProject() throws Exception {
        setConfigSettings();
        data.add(new NameValuePair("text", "jenkins get project_1 #1 log"));
        WebResponse response = makeRequest(data);
        assertThat(getSlackMessage(response).getText(), is("Could not find project (project_1)\n"));
    }

    @Test
    public void testGetProjectBuildLog() throws Exception {
        setConfigSettings();
        FreeStyleProject project = jenkinsRule.createFreeStyleProject("project-1"); 
        Thread.sleep(30);
        project.scheduleBuild2(0);
        Thread.sleep(60);
        data.add(new NameValuePair("text", "jenkins get project-1 #1 log"));
        WebResponse response = makeRequest(data);
        assertThat(getSlackMessage(response).getText(), containsString("Building in workspace"));
    }

    private void setConfigSettings() throws Exception {
        HtmlForm form = jenkinsRule.createWebClient().goTo("configure").getFormByName("config");
        form.getInputByName("_.slackOutgoingWebhookToken").setValueAttribute("GOOD_TOKEN");
        jenkinsRule.submit(form);
    }

    private SlackTextMessage getSlackMessage(WebResponse response) throws Exception { 
        return new ObjectMapper().readValue(response.getContentAsString(),
            SlackTextMessage.class);
    }

    private WebResponse makeRequest(List<NameValuePair> postData) throws Exception {
        WebRequestSettings request =
            new WebRequestSettings(client.createCrumbedUrl(ENDPOINT), POST);

        if (postData != null)
            request.setRequestParameters(postData);

        return client.loadWebResponse(request);
    } 
}
