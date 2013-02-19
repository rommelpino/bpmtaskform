package soadev.bpm.view.helper;

import java.io.Serializable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oracle.adf.share.logging.ADFLogger;

import oracle.bpel.services.common.util.XMLUtil;
import oracle.bpel.services.workflow.metadata.IPrivilege;
import oracle.bpel.services.workflow.task.model.AnyType;

import oracle.bpel.services.workflow.task.model.Task;

import org.w3c.dom.Element;

import soadev.bpm.workflow.task.utils.PayloadUtil;

import soadev.view.utils.ADFUtils;

public abstract class AbstractPayloadHandler implements Serializable{
    private static ADFLogger _logger =
        ADFLogger.createADFLogger(AbstractPayloadHandler.class);
    private static final long serialVersionUID = 1L;
    //    protected AnyType payload;
    protected Task task;
    protected Map<String, IPrivilege> visibilityRules;
    protected Map<String, Object> payloadObjects;
    private Map dummyMap;

    public Map<String, Object> getPayloadObjects() throws Exception {
        _logger.fine("getPayloadObjects begin [task] = "+ task);
        if (task == null) {
            throw new IllegalStateException("Task not set.");
        }
        if (payloadObjects == null) {
            List<Element> elements = task.getPayload().getContent();
            payloadObjects = new HashMap<String, Object>();
            for (Element element : elements) {
                Object obj = unmarshal(element);
                String key = element.getLocalName();
                if (key == null) {
                    key = element.getNodeName();
                }
                _logger.fine("obj: " + key + " - " + obj);
                payloadObjects.put(key, obj);
            }
        }
        _logger.fine("getPayloadObjects end [payloadObjects] = " + payloadObjects);
        return payloadObjects;
    }

    public Map<String, Element> getPayloadElements() throws Exception {
        _logger.fine("AbstractPayloadHandler.getPayloadElements begin");
        Map<String, Element> payloadElements = new HashMap<String, Element>();
        for (Map.Entry<String, Object> entry :
             getPayloadObjects().entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                Element element = marshal(entry.getValue(), entry.getKey());
                _logger.fine(XMLUtil.toString(element));
                payloadElements.put(entry.getKey(), element);
            }
        }
        return payloadElements;
    }

    public void updatePayload(String action) throws Exception {
        _logger.fine("AbstractPayloadHandler.updatePayload begin [action] =" + action);
        beforePayloadUpdate(action);
        Task task = getTask();
        Map<String, Element> payloadElements = getPayloadElements();
        Element payloadElement = task.getPayloadAsElement();
        _logger.fine("PayloadElement before update: " + XMLUtil.toString(payloadElement));
        for (Map.Entry<String, Element> entry : payloadElements.entrySet()) {
            if (hasWriteAccess(entry.getKey())) {
                Element updatedElement = entry.getValue();
                PayloadUtil.replaceOrAppendPayloadElementData(payloadElement,
                                                              updatedElement);
            }
        }
        _logger.fine("PayloadElement after update: " +
                     XMLUtil.toString(payloadElement));
        task.setPayloadAsElement(payloadElement);
        afterPayloadUpdate(action);
    }
    protected void beforePayloadUpdate(String action)throws Exception{
        
    }
    protected void afterPayloadUpdate(String action)throws Exception{
    }

    public boolean hasWriteAccess(String payloadElementName) {
        //TODO replace with proper security check
        return true;
    }

    public abstract Object unmarshal(Element element) throws Exception;

    public abstract Element marshal(Object object,
                                    String elementName) throws Exception;

    public void setVisibilityRules(Map<String, IPrivilege> privileges) {
        this.visibilityRules = privileges;
    }

    public Map<String, IPrivilege> getVisibilityRules() {
        if (visibilityRules == null) {
            throw new IllegalStateException("Visibility Rules not set.");
        }
        return visibilityRules;
    }

    public Map getHasAccess() {
        if (dummyMap == null) {
            dummyMap = new HashMap() {
                    public Object get(Object obj) {
                        //TODO when fix arrived update this code to use visibility rules;
                        return true;
                    }
                };
        }
        return dummyMap;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Task getTask() {
        return task;
    }
}
