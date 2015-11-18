# jenkins-slack-webhook-plugin

[![Build Status](https://travis-ci.org/dpires/jenkins-slack-webhook-plugin.png?branch=master)](https://travis-ci.org/dpires/jenkins-slack-webhook-plugin)

## Installation

### Slack Configuration
1. In your slack integration settings create a new outgoing webhook
2. In the URL field add <YOUR_JENKINS_HOST>/webhook (The webhook endpoint is off root)
3. Copy the Token value

### Jenkins Configuration
1. Under configure system, add the Slack token you copied to Global Slack Webhook Settings


# Developer Instructions

This plugin uses gradle wrapper, so the only dependency is a working JDK (7/8).

1. To build the .hpi plugin (build/libs/slack-webhook-plugin)
```
./gradlew jpi
```
2. To build and install in a local jenkins server running at http://localhost:8080/
```
./gradlew server
```

## License

[MIT](LICENSE)
