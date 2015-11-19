package org.jenkinsci.plugins.slackwebhook.model;


import java.util.List;
import java.util.ArrayList;




public class SlackAttachmentMessage {
    private List<SlackAttachment>  attachments = new ArrayList<SlackAttachment>();

    public SlackAttachmentMessage(SlackAttachment attachment) {
        this.attachments.add(attachment);
    }

    public void addAttachment(SlackAttachment attachment) {
        attachments.add(attachment);
    }
    
    public List<SlackAttachment> getAttachments() {
        return this.attachments;
    }
    
    public void setAttachments(List<SlackAttachment> attachments) {
        this.attachments = attachments;
    }
}
