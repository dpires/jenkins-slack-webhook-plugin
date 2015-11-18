package org.jenkinsci.plugins.slackwebhook.model;


public class SlackAttachment {
    private String text;
    private String title;

    public SlackAttachment() { }

    public SlackAttachment(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }
}
