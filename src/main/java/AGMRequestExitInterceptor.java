

import com.appdynamics.instrumentation.sdk.Rule;
import com.appdynamics.instrumentation.sdk.SDKClassMatchType;
import com.appdynamics.instrumentation.sdk.contexts.ISDKUserContext;
import com.appdynamics.instrumentation.sdk.template.AExit;
import com.appdynamics.instrumentation.sdk.toolbox.reflection.IReflector;
import com.appdynamics.instrumentation.sdk.toolbox.reflection.ReflectorException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AGMRequestExitInterceptor extends AExit {
    IReflector setTrackingIDReflector = this.getNewReflectionBuilder().invokeInstanceMethod("setSingularityHeader", true, new String[]{String.class.getCanonicalName()}).build();

    public Map<String, String> identifyBackend(Object invokedObject, String className, String methodName, Object[] paramValues, Throwable thrownException, Object returnValue, ISDKUserContext context) throws ReflectorException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("flow", "flow");
        return map;
    }

    public void marshalTransactionContext(String transactionContext, Object invokedObject, String className, String methodName, Object[] paramValues, Throwable thrownException, Object returnValue, ISDKUserContext context) throws ReflectorException {
        this.getLogger().debug("IBMProducer.marshalTransactionContext() :transactionContext " + transactionContext);
        this.setTrackingIDReflector.execute(invokedObject.getClass().getClassLoader(), invokedObject, new Object[]{transactionContext});
    }

    public boolean isCorrelationEnabled() {
        return true;
    }

    public boolean isCorrelationEnabledForOnMethodBegin() {
        return true;
    }

    public List<Rule> initializeRules() {
        ArrayList<Rule> rules = new ArrayList<Rule>();
        rules.add(new Rule.Builder("com.ibm.zapm.ctg.exit.AGMClientExit")
                .classMatchType(SDKClassMatchType.MATCHES_CLASS)
                .methodMatchString("allowHeaderImplementation")
                .build());
        return rules;
    }
}
