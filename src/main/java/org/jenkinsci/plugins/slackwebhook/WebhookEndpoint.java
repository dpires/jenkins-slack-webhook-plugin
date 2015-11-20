package org.jenkinsci.plugins.slackwebhook;


import jenkins.model.Jenkins;
import jenkins.model.GlobalConfiguration;

import hudson.Extension;

import hudson.model.Build;
import hudson.model.Project;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.UnprotectedRootAction;

import hudson.security.ACL;

import javax.servlet.ServletException;

import java.io.IOException;

import java.util.List;
import java.util.ArrayList;

import java.util.logging.Logger;

import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import org.kohsuke.stapler.interceptor.RequirePOST;

import org.jenkinsci.plugins.slackwebhook.model.JsonResponse;
import org.jenkinsci.plugins.slackwebhook.model.SlackPostData;
import org.jenkinsci.plugins.slackwebhook.model.SlackTextMessage;
import org.jenkinsci.plugins.slackwebhook.model.SlackWebhookCause;

import org.jenkinsci.plugins.slackwebhook.exception.CommandRouterException;



@Extension
public class WebhookEndpoint implements UnprotectedRootAction {

    private String slackUser;

    private GlobalConfig globalConfig;

    private static final Logger LOGGER =
        Logger.getLogger("jenkins.SlackWebhookPlugin");

    public WebhookEndpoint() {
        globalConfig = GlobalConfiguration.all().get(GlobalConfig.class);
    }

    @Override
    public String getUrlName() {
        return "webhook";
    }

    @RequirePOST
    public HttpResponse doIndex(StaplerRequest req) throws IOException,
        ServletException {

        if (globalConfig.getSlackToken() == null) {
            return new JsonResponse(new SlackTextMessage("Slack token not set"),
                StaplerResponse.SC_OK); 
        }

        SlackPostData data = new SlackPostData();
        req.bindParameters(data);

        if (!globalConfig.getSlackToken().equals(data.getToken()))
            return new JsonResponse(new SlackTextMessage("Invalid Slack token"),
                StaplerResponse.SC_OK); 
    
        //
        // strip trigger word from command
        // "TRIGGER_WORD run api-build" => "run api-build"
        //
        String command =
            data.getText().replace(data.getTrigger_word(), "").trim();

        //
        // TODO: figure out better way of injecting slack user data into handler
        //
        this.slackUser = data.getUser_name();

        try {
            SlackTextMessage msg = new CommandRouter()
                .addRoute("^list projects", "jenkins list projects", "Lists buildable projects", this, "listProjects")
                .addRoute("^run ([a-zA-Z-\\.]+)", "jenkins run <project_name>", "Schedule run for <project_name>", this, "scheduleJob")
                .addRoute("^get ([a-zA-Z-\\.]+) #([0-9]+) log", "jenkins get <project-name> #<build_number> log", "Returns 25 lines of the log for build <build_number> of <project_name>", this, "getProjectLog")
                .route(command);

            return new JsonResponse(msg, StaplerResponse.SC_OK);
            
        } catch (CommandRouterException ex) {
            return new JsonResponse(new SlackTextMessage(ex.getMessage()), StaplerResponse.SC_OK);
        }
    }

    public SlackTextMessage getProjectLog(String projectName,
        String buildNumber) {

        ACL.impersonate(ACL.SYSTEM);

        Project project =
            Jenkins.getInstance().getItemByFullName(projectName, Project.class);

        if (project == null)
            return new SlackTextMessage("Could not find project ("+projectName+")\n");

        AbstractBuild build =
            project.getBuildByNumber(Integer.parseInt(buildNumber));

        if (build == null)
            return new SlackTextMessage("Could not find build #"+buildNumber+" for ("+projectName+")\n");

        List<String> log = new ArrayList<String>();
        try {
            log = build.getLog(25);
        } catch (IOException ex) {
            return new SlackTextMessage("Error occured returning log: "+ex.getMessage());
        }

        String response = "";
        for (String line : log) {
            response += line + "\n";
        }
        return new SlackTextMessage(response);
    }

    public SlackTextMessage scheduleJob(String projectName) {
        ACL.impersonate(ACL.SYSTEM);
        String response = "";

        Project project =
            Jenkins.getInstance().getItemByFullName(projectName, Project.class);

        boolean success = false;

        if (project != null)
            success = project.scheduleBuild(new SlackWebhookCause(this.slackUser));
        else
            return new SlackTextMessage("Could not find project ("+projectName+")\n");

        if (success) 
            return new SlackTextMessage("Build scheduled for project "+ projectName+"\n");
        else
            return new SlackTextMessage("Build not scheduled due to an issue with Jenkins");
    }

    public SlackTextMessage listProjects() {
        ACL.impersonate(ACL.SYSTEM);
        String response = "";

        for (AbstractProject<?, ?> job : Jenkins.getInstance().getAllItems(AbstractProject.class)) {
            if (job.isBuildable())
                response += job.getDisplayName() + "\n";
        }

        if (response.equals(""))
            response = "No projects found\n";

        return new SlackTextMessage(response);
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
