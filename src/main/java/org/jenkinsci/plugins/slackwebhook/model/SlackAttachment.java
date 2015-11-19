package org.jenkinsci.plugins.slackwebhook.model;



public class SlackAttachment extends SlackTextMessage {
    private String title;

    public SlackAttachment() { }

    public SlackAttachment(String text) {
        super(text);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }
}
