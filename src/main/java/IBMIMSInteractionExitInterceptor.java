import com.appdynamics.agent.api.AppdynamicsAgent;
import com.appdynamics.agent.api.ExitCall;
import com.appdynamics.agent.api.Transaction;
import com.appdynamics.instrumentation.sdk.Rule;
import com.appdynamics.instrumentation.sdk.SDKClassMatchType;

import com.appdynamics.instrumentation.sdk.template.AGenericInterceptor;
import com.appdynamics.instrumentation.sdk.toolbox.reflection.IReflector;
import com.appdynamics.instrumentation.sdk.toolbox.reflection.ReflectorException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IBMIMSInteractionExitInterceptor extends AGenericInterceptor {
    IReflector enableTracking = this.makeInvokeInstanceMethodReflector("enableTracking");
    IReflector setTrackingID = this.makeInvokeInstanceMethodReflector("setTrackingID", String.class.getCanonicalName());

    @Override
    public Object onMethodBegin(Object objectIntercepted, String className, String methodName, Object[] params) {
        this.getLogger().info("IBM IMSInteraction Created, setting tracking enabled and injecting an exitcall correlation string");
        HashMap propertyMap = new HashMap();
        ExitCall exitCall = AppdynamicsAgent.getTransaction().startExitCall(propertyMap, "IBM IMS Exit Call", "CUSTOM", true);
        return exitCall;
    }

    @Override
    public void onMethodEnd(Object state, Object objectIntercepted, String className, String methodName, Object[] params, Throwable exception, Object returnVal) {
        ExitCall exitCall = (ExitCall)state;
        if (exitCall == null) {
            return;
        }
        try {
            this.enableTracking.execute(objectIntercepted.getClass().getClassLoader(), objectIntercepted);
            this.getLogger().debug("Set IBM IMSInteraction Tracking enabled to true");
        }
        catch (ReflectorException e) {
            this.getLogger().info("Oops, exception enabling tracking, message: " + e.getMessage());
        }
        try {
            this.setTrackingID.execute(objectIntercepted.getClass().getClassLoader(), objectIntercepted, new Object[]{exitCall.getCorrelationHeader()});
            this.getLogger().debug("Set IBM IMSInteraction Tracking ID to: " + exitCall.getCorrelationHeader());
        }
        catch (ReflectorException e) {
            this.getLogger().info("Oops, exception setting tracking id, message: " + e.getMessage());
        }
        exitCall.end();
    }

    @Override
    public List<Rule> initializeRules() {
        ArrayList<Rule> rules = new ArrayList<Rule>();
        rules.add(new Rule.Builder("com.ibm.connector2.ims.ico.IMSInteractionSpec").classMatchType(SDKClassMatchType.MATCHES_CLASS).methodMatchString("<init>").build());
        return rules;
    }

    protected IReflector makeInvokeInstanceMethodReflector(String method, String ... args) {
        if (args.length > 0) {
            return this.getNewReflectionBuilder().invokeInstanceMethod(method, true, args).build();
        }
        return this.getNewReflectionBuilder().invokeInstanceMethod(method, true).build();
    }

}


