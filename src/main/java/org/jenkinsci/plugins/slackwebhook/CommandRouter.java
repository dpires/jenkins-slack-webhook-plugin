package org.jenkinsci.plugins.slackwebhook;


import java.util.List;
import java.util.ArrayList;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.lang.reflect.Method;

import org.jenkinsci.plugins.slackwebhook.model.SlackTextMessage;

import org.jenkinsci.plugins.slackwebhook.exception.CommandRouterException;




public class CommandRouter {

    public CommandRouter() { }

    public List<PatternAction> routes = new ArrayList<PatternAction>();

    public CommandRouter addRoute(String regexRoute,
        String command,
        String commandDescription,
        Object handlerInstance,
        String handlerMethodName) {
        this.routes.add(new PatternAction(regexRoute,
            command,
            commandDescription,
            handlerInstance,
            handlerMethodName));

        return this;
    }

    public SlackTextMessage route(String command) throws CommandRouterException {
        SlackTextMessage message = null;

        for (PatternAction pa : routes) {

            Matcher matcher = pa.pattern.matcher(command);

            boolean matches = matcher.matches();

            if (matches) {
    
                Method handlerMethod = getHandlerMethod(pa.object,
                    pa.action,
                    matcher.groupCount());

                List<Object> parameters = new ArrayList<Object>();

                for (int index = 0; index <= matcher.groupCount(); index++) {
                    parameters.add(matcher.group(index));
                }

                Object[] parametersArray = null;

                //
                // strip the first matched group
                //
                if (parameters.size() > 1) {
                    parameters.remove(0);
                    parametersArray = new Object[parameters.size()];
                    parametersArray = parameters.toArray(parametersArray);
                }
                
                try {
                    message = (SlackTextMessage)handlerMethod.invoke(pa.object, parametersArray);
                } catch (Exception ex) {
                    throw new CommandRouterException(ex.getMessage());
                }
                break;
            }
        }

        if (message == null) {
            String error = "Unknown command ("+command+") try the following:\n";
            for (PatternAction route : routes) {
                error += route.command + " - " + route.commandDescription;
                error += "\n";
            } 
            throw new CommandRouterException(error);
        }

        return message;
    }

    private  Method getHandlerMethod(Object handlerInstance,
        String handlerMethodName,
        int parameterCount) throws CommandRouterException {

        Method returnMethod = null;

        for (Method method : handlerInstance.getClass().getMethods()) {
            if (method.getName().equals(handlerMethodName) &&
                method.getParameterTypes().length == parameterCount) {
                returnMethod = method;
                break;
            }
        }

        if (returnMethod == null)
            throw new CommandRouterException("No Handler method could be found");

        return returnMethod;
    }

    private class  PatternAction {
        public String action;
        public Object object;
        public String command;
        public Pattern pattern;
        public String commandDescription;

        public PatternAction(String regex,
            String command,
            String commandDescription,
            Object object,
            String action) {

            this.pattern = Pattern.compile(regex);
            this.action = action;
            this.object = object;
            this.command = command;
            this.commandDescription = commandDescription;
        }
    }
}
