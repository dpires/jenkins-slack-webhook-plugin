# jenkins-slack-webhook-plugin

[![Build Status](https://travis-ci.org/dpires/jenkins-slack-webhook-plugin.png?branch=master)](https://travis-ci.org/dpires/jenkins-slack-webhook-plugin)

## Usage

Assuming you have your Slack webhook trigger word set to 'jenkins', the following commands are permitted:

```
[You]: jenkins list projects
[SlackWebhookBot]: my-project-one
                   my-projects-two
```

```
[You]: jenkins run my-project-one
[SlackWebhookBot]: Build scheduled for my-project-one
```

## Installation

### Slack Configuration
1. In your Slack integration settings create a new outgoing webhook
2. In the URL field add YOUR_JENKINS_HOST/webhook/ (The webhook endpoint is off root, the final / is required)
3. Copy the Token value

### Jenkins Configuration
1. Under configure system, add the Slack token you copied to Global Slack Webhook Settings


# Developer Instructions

This plugin uses gradle wrapper, so the only dependency is a working JDK (7/8).

1. To build the .hpi plugin (build/libs/slack-webhook-plugin.hpi)
```
./gradlew jpi
```
2. To build and install in a local jenkins server running at http://localhost:8080/
```
./gradlew server
```

## License

[MIT](LICENSE)
