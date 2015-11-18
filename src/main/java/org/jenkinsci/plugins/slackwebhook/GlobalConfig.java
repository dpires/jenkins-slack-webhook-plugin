package org.jenkinsci.plugins.slackwebhook;

import hudson.Extension;

import net.sf.json.JSONObject;

import jenkins.model.GlobalConfiguration;

import org.kohsuke.stapler.StaplerRequest;

import hudson.model.Descriptor.FormException;


@Extension
public class GlobalConfig extends GlobalConfiguration {

    private String slackToken;

    public GlobalConfig() { 
        load(); 
    }

    public String getSlackToken() {
        return slackToken;
    }

    public void setSlackToken(String slackToken) {
        this.slackToken = slackToken;
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        req.bindJSON(this, json);
        save();
        return true;
    }
}
