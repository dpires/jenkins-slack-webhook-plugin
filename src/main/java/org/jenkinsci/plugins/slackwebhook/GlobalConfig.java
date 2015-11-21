package org.jenkinsci.plugins.slackwebhook;


import hudson.Extension;

import net.sf.json.JSONObject;

import jenkins.model.GlobalConfiguration;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.util.FormValidation;

import hudson.model.Descriptor.FormException;





@Extension
public class GlobalConfig extends GlobalConfiguration {

    private String slackOutgoingWebhookToken;

    public GlobalConfig() { 
        load(); 
    }

    public String getSlackOutgoingWebhookToken() {
        return slackOutgoingWebhookToken;
    }

    public void setSlackOutgoingWebhookToken(String slackOutgoingWebhookToken) {
        this.slackOutgoingWebhookToken = slackOutgoingWebhookToken;
    }

    public FormValidation doCheckSlackOutgoingWebhookToken(@QueryParameter String value) {
        if (value == null || value.trim().isEmpty())
            return FormValidation.warning("Please set a Slack outgoing webhook token");

        return FormValidation.ok();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        req.bindJSON(this, json);
        save();
        return true;
    }
}
