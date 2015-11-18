package org.jenkinsci.plugins.slackwebhook;

import org.jenkinsci.plugins.slackwebhook.model.JsonResponse;
import org.jenkinsci.plugins.slackwebhook.model.SlackPostData;
import org.jenkinsci.plugins.slackwebhook.model.SlackMessage;
import org.jenkinsci.plugins.slackwebhook.model.SlackAttachment;

import jenkins.model.GlobalConfiguration;
import hudson.model.Descriptor.FormException;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.UnprotectedRootAction;
import hudson.security.ACL;
import jenkins.model.Jenkins;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.io.ByteArrayOutputStream;

import java.io.InputStream;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Flavor;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonParseException;


@Extension
public class WebhookEndpoint implements UnprotectedRootAction {

    private GlobalConfig globalConfig;
    private static final Logger LOGGER = Logger.getLogger(WebhookEndpoint.class.getName());

    public WebhookEndpoint() {
        globalConfig = GlobalConfiguration.all().get(GlobalConfig.class);
    }

    @Override
    public String getUrlName() {
        return "webhook";
    }

    @RequirePOST
    public HttpResponse doIndex(StaplerRequest req) throws IOException, ServletException {
        if (globalConfig.getSlackToken() == null) {
        
            SlackAttachment attachment = new SlackAttachment("BAD_REQUEST", "Slack token not set");
            return new JsonResponse(attachment, StaplerResponse.SC_OK); 
        }

        SlackPostData data = new SlackPostData();
        req.bindParameters(data);

        return new JsonResponse(new SlackAttachment("OK", "SUCCESS"), StaplerResponse.SC_OK);
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }
}
