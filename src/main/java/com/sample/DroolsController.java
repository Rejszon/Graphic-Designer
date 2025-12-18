package com.sample;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.definition.type.FactType;
import java.util.ArrayList;
import java.util.List;

public class DroolsController {
    private KieSession kSession;
    private String pkg = "rules"; 

    public DroolsController() {
        try {
            KieServices ks = KieServices.Factory.get();
            KieContainer kContainer = ks.getKieClasspathContainer();
            kSession = kContainer.newKieSession("ksession-rules");
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("B³¹d startu silnika: " + e.getMessage());
        }
    }

    public void fireRules() {
        if(kSession != null) {
            kSession.fireAllRules();
        }
    }

    public void insertAnswer(Object questionContentEnum, String yesOrNo) {
        try {
            FactType answerType = kSession.getKieBase().getFactType(pkg, "Answer");
            Object selectedEnum = Enum.valueOf((Class<Enum>) answerType.getFactClass(), yesOrNo);
            FactType responseType = kSession.getKieBase().getFactType(pkg, "UserResponse");
            Object response = responseType.newInstance();
            responseType.set(response, "questionIdentifier", questionContentEnum);
            responseType.set(response, "usersAnswer", selectedEnum);
            kSession.insert(response);
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }

    public void retractObject(Object fact) {
        if(fact != null) kSession.delete(kSession.getFactHandle(fact));
    }

    // --- GETTERY ---

    public Object getCurrentQuestion() {
        List<Object> list = getObjectsByType("Question");
        return list.isEmpty() ? null : list.get(0);
    }
    
    public Object getCurrentReaction() {
        List<Object> list = getObjectsByType("Reaction");
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Object> getRecommendations() {
        return getObjectsByType("Recommendation");
    }

    public String getContentFromEnum(Object enumObj) {
        try {
            if (enumObj == null) return "";
            FactType type = kSession.getKieBase().getFactType(pkg, enumObj.getClass().getSimpleName());
            return (String) type.get(enumObj, "content");
        } catch (Exception e) { return enumObj.toString(); }
    }
    
    public Object getFieldValue(Object fact, String field) {
        try {
            FactType type = kSession.getKieBase().getFactType(pkg, fact.getClass().getSimpleName());
            return type.get(fact, field);
        } catch (Exception e) { return null; }
    }

    private List<Object> getObjectsByType(String typeName) {
        List<Object> result = new ArrayList<>();
        if (kSession == null) return result;
        try {
            Class<?> cls = kSession.getKieBase().getFactType(pkg, typeName).getFactClass();
            kSession.getObjects(o -> cls.isInstance(o)).forEach(result::add);
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }
    public List<String> getAnswerOptions() {
        List<String> options = new ArrayList<>();
        try {
            FactType answerType = kSession.getKieBase().getFactType(pkg, "Answer");
            Class<?> answerClass = answerType.getFactClass();
            
            Object[] enumConstants = answerClass.getEnumConstants();
            for (Object constant : enumConstants) {
                options.add(constant.toString());
            }
        } catch (Exception e) { e.printStackTrace(); }
        return options;
    }
}