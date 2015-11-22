package org.jenkinsci.plugins.slackwebhook;


import jenkins.model.Jenkins;
import jenkins.model.GlobalConfiguration;

import hudson.Extension;

import hudson.model.Build;
import hudson.model.Result;
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
import org.jenkinsci.plugins.slackwebhook.exception.RouteNotFoundException;




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

        if (globalConfig.getSlackOutgoingWebhookToken() == null) {
            return new JsonResponse(new SlackTextMessage("Slack token not set"),
                StaplerResponse.SC_OK); 
        }

        SlackPostData data = new SlackPostData();
        req.bindParameters(data);

        if (!globalConfig.getSlackOutgoingWebhookToken().equals(data.getToken()))
            return new JsonResponse(new SlackTextMessage("Invalid Slack token"),
                StaplerResponse.SC_OK); 
    
        String triggerWord = data.getTrigger_word();

        //
        // TODO: figure out better way of injecting slack user data into handler
        //
        this.slackUser = data.getUser_name();

        CommandRouter<SlackTextMessage> router =
            new CommandRouter<SlackTextMessage>();

        try {
            router.addRoute("^"+triggerWord+" list projects",
                triggerWord+" list projects", "Return a list of buildable projects",
                this,
                "listProjects")
            .addRoute("^"+triggerWord+" run ([a-zA-Z0-9_\\-\\.]+)",
                triggerWord+" run <project_name>",
                "Schedule a run for <project_name>",
                this,
                "scheduleJob")
            .addRoute("^"+triggerWord+" get ([a-zA-Z0-9_\\-\\.]+) #([0-9]+) log",
                triggerWord+" get <project-name> #<build_number> log",
                "Return a truncated log for build #<build_number> of <project_name>",
                this,
            "getProjectLog");

            SlackTextMessage msg = router.route(data.getText());

            return new JsonResponse(msg, StaplerResponse.SC_OK);
            
        } catch (RouteNotFoundException ex) {

            LOGGER.warning(ex.getMessage());

            String command = ex.getRouteCommand();

            String response = "*Help:*\n";
            if (command.split("\\s+").length > 1)
                response += "`"+command+"` _is an unknown command, try one of the following:_\n\n";
            else
                response += "\n";

            for (CommandRouter.Route route : router.getRoutes()) {
                response += "`"+route.command+"`\n```"+route.commandDescription+"```";
                response += "\n\n";
            }

            return new JsonResponse(new SlackTextMessage(response), StaplerResponse.SC_OK);

        } catch (CommandRouterException ex) {
            LOGGER.warning(ex.getMessage());
            return new JsonResponse(new SlackTextMessage(ex.getMessage()), StaplerResponse.SC_OK);

        } catch (Exception ex) {
            LOGGER.warning(ex.getMessage());
            return new JsonResponse(new SlackTextMessage("An error occured: "+ ex.getMessage()), StaplerResponse.SC_OK);
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

        String response = "*"+projectName+"* *#"+buildNumber+"*\n";
        response += "```";
        for (String line : log) {
            response += line + "\n";
        }
        response += "```";
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
        String response = "*Projects:*\n";

        List<AbstractProject> jobs =
            Jenkins.getInstance().getAllItems(AbstractProject.class);

        for (AbstractProject job : jobs) {
            if (job.isBuildable()) {
                AbstractBuild lastBuild = job.getLastBuild();
                String buildNumber = "TBD";
                String status = "TBD";
                if (lastBuild != null) {

                    buildNumber = Integer.toString(lastBuild.getNumber());

                    if (lastBuild.isBuilding()) {
                        status = "BUILDING";
                    }

                    Result result = lastBuild.getResult();

                    if (result != null) {
                        status = result.toString();
                    }
                }
                response += ">*"+job.getDisplayName() + "*\n>*Last Build:* #"+buildNumber+"\n>*Status:* "+status;
                response += "\n\n\n";
            }
        }

        if (jobs == null || jobs.size() == 0)
            response += ">_No projects found_";

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
