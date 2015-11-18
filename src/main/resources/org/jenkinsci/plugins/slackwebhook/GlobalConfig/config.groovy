package org.jenkinsci.plugins.slackwebhook.GlobalConfig;

f = namespace('/lib/form')

f.section(title: _('Global Slack Webhook Settings')) {
    f.entry(field: 'slackToken', title: _('Integration Token')) {
        f.textbox()
    }
}
