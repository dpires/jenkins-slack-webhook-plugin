package org.jenkinsci.plugins.slackwebhook.GlobalConfig;

f = namespace('/lib/form')

f.section(title: _('Slack Webhook Settings')) {
    f.entry(field: 'slackOutgoingWebhookToken', title: _('Outgoing Webhook Token')) {
        f.textbox()
    }
}
