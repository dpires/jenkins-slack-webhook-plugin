package org.jenkinsci.plugins.slackwebhook;


import java.util.List;
import java.util.ArrayList;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.lang.reflect.Method;

import org.jenkinsci.plugins.slackwebhook.exception.CommandRouterException;
import org.jenkinsci.plugins.slackwebhook.exception.RouteNotFoundException;




public class CommandRouter<T> {

    public CommandRouter() { }

    public List<Route> routes = new ArrayList<Route>();

    public CommandRouter addRoute(String regex,
        String command,
        String commandDescription,
        Object handlerInstance,
        String handlerAction) {

        this.routes.add(new CommandRouter.Route(regex,
            command,
            commandDescription,
            handlerInstance,
            handlerAction));

        return this;
    }

    public List<Route> getRoutes() {
        return this.routes;
    }

    public T route(String command) throws CommandRouterException,
        RouteNotFoundException {

        T message = null;

        for (Route pa : routes) {

            Matcher matcher = pa.regex.matcher(command);

            boolean matches = matcher.matches();

            if (matches) {
    
                Method handlerMethod = getHandlerMethod(pa.handlerInstance,
                    pa.handlerAction,
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
                    message =
                        (T)handlerMethod.invoke(pa.handlerInstance, parametersArray);
                } catch (Exception ex) {
                    throw new CommandRouterException(ex.getMessage());
                }
                break;
            }
        }

        if (message == null)
            throw new RouteNotFoundException("No route found for given command", command);

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

    public static class Route {
        public Pattern regex;
        public String command;
        public String commandDescription;
        public Object handlerInstance;
        public String handlerAction;

        public Route(String regex,
            String command,
            String commandDescription,
            Object handlerInstance,
            String handlerAction) {

            this.regex = Pattern.compile(regex);
            this.handlerAction = handlerAction;
            this.handlerInstance = handlerInstance;
            this.command = command;
            this.commandDescription = commandDescription;
        }
    }
}
