package soadev.bpm.view.backing;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import javax.faces.event.ActionEvent;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.ValueChangeEvent;

import javax.faces.model.SelectItem;

import oracle.adf.controller.TaskFlowId;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;

import oracle.adf.view.rich.component.rich.input.RichInputFile;
import oracle.adf.view.rich.component.rich.layout.RichShowDetailHeader;

import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.adf.view.rich.event.DialogEvent;

import oracle.bpel.services.common.util.XMLUtil;
import oracle.bpel.services.workflow.StaleObjectException;
import oracle.bpel.services.workflow.WorkflowException;

import oracle.bpel.services.workflow.client.IWorkflowServiceClient;
import oracle.bpel.services.workflow.client.IWorkflowServiceClientConstants;
import oracle.bpel.services.workflow.client.WorkflowServiceClientFactory;
import oracle.bpel.services.workflow.client.util.WorkflowAttachmentUtil;
import oracle.bpel.services.workflow.metadata.IPrivilege;
import oracle.bpel.services.workflow.metadata.ITaskMetadataService;
import oracle.bpel.services.workflow.metadata.TaskMetadataServiceException;
import oracle.bpel.services.workflow.query.ITaskQueryService;

import oracle.bpel.services.workflow.task.ITaskAssignee;
import oracle.bpel.services.workflow.task.ITaskService;

import oracle.bpel.services.workflow.task.impl.TaskAssignee;

import oracle.bpel.services.workflow.task.model.ActionType;

import oracle.bpel.services.workflow.task.model.AnyType;
import oracle.bpel.services.workflow.task.model.AttachmentType;
import oracle.bpel.services.workflow.task.model.CommentType;

import oracle.bpel.services.workflow.task.model.ObjectFactory;

import oracle.bpel.services.workflow.task.model.PreActionUserStepType;
import oracle.bpel.services.workflow.task.model.PreActionUserStepsType;
import oracle.bpel.services.workflow.task.model.Task;

import oracle.bpel.services.workflow.verification.IWorkflowContext;

import oracle.bpel.services.workflow.worklist.adf.ALEComponentBean;

import oracle.bpel.services.workflow.worklist.adf.TaskFlowReassignBean;
import oracle.bpel.worklistapp.dc.idbrowser.beans.model.RequestInfoModel;
import oracle.bpel.worklistapp.dc.idbrowser.beans.model.RequestInfoVO;
import oracle.bpel.worklistapp.dc.idbrowser.beans.view.IdentityBrowserView;

import oracle.bpel.worklistapp.dc.idbrowser.beans.view.RequestInfoView;


import oracle.bpm.client.BPMServiceClientFactory;

import org.apache.myfaces.trinidad.context.RequestContext;
import org.apache.myfaces.trinidad.event.DisclosureEvent;
import org.apache.myfaces.trinidad.event.SelectionEvent;
import org.apache.myfaces.trinidad.model.UploadedFile;

import org.apache.myfaces.trinidad.render.ExtendedRenderKitService;
import org.apache.myfaces.trinidad.util.Service;

import org.w3c.dom.Element;


import soadev.bpm.view.helper.AbstractPayloadHandler;
import soadev.bpm.workflow.task.utils.PayloadUtil;

import soadev.view.utils.ADFUtils;
import soadev.view.utils.JSFUtils;

public class TaskDetailsForm implements Serializable{
    private static ADFLogger _logger =
        ADFLogger.createADFLogger(TaskDetailsForm.class);
    private transient BPMServiceClientFactory factory;
    private transient IWorkflowServiceClient client;
    private transient IWorkflowContext ctx;
    private transient Task task;
    private String action;
    private String comment;
    private String commentScope = "BPM";
    private String attachmentScope = "BPM";
    private String selectedAttachmentType = "url";
    private String URIname;
    private String URI;
    private transient UploadedFile file;
    private String docComments;
    private Map dummyMap;
    private static List<String> excludedActions;
    private transient List<ActionType> systemActions;
    private transient List<ActionType> customActions;
    private List<String> allActions;
    private transient RichPopup reassignPopup;
    private transient RichPopup requestInfoPopup;
    private transient RichPopup routeTaskPopup;
    private transient RichTable attachmentsTable;
    private transient RichPopup attachmentPopup;
    private transient RichTable userCommentTable;
    private transient RichInputFile richFile;
    private transient AttachmentType selectedAttachment;
    private transient RichPopup deleteAttachmentPopup;
    private String blankTaskFlowId = "/WEB-INF/blank.xml#blank";
    private static List<SelectItem> priorityItems;
//init excluded actions
    static {
        _logger.info("init excludedActions");
        excludedActions = new ArrayList<String>();
        excludedActions.add("UPDATE_COMMENT");
        excludedActions.add("VIEW_SUB_TASKS");
        excludedActions.add("UPDATE");
        excludedActions.add("UPDATE_ATTACHMENT");
        excludedActions.add("SUSPEND_TIMERS");
        excludedActions.add("VIEW_PROCESS_HISTORY");
        excludedActions.add("VIEW_TASK");
        excludedActions.add("CUSTOM");
        excludedActions.add("VIEW_TASK_HISTORY");
    }
    
    //init priorityItems
    static {
        _logger.info("init priorityItems");
        priorityItems = new ArrayList<SelectItem>();
        priorityItems.add(new SelectItem(1));
        priorityItems.add(new SelectItem(2));
        priorityItems.add(new SelectItem(3));
        priorityItems.add(new SelectItem(4));
        priorityItems.add(new SelectItem(5));
    }

    public TaskDetailsForm() {
        factory = BPMServiceClientFactory.getInstance(null, null, null);
    }

    @PostConstruct
    public void init() {
        _logger.info("TaskDetailsForm.init()");
        try {
            getWorkflowContext();
            getTask();
            initPayload();
        } catch (Exception e) {
            // TODO: Add catch code
            e.printStackTrace();
        }

    }


    public IWorkflowContext getWorkflowContext() throws WorkflowException {
        ctx = (IWorkflowContext)JSFUtils.getFromSession("WORKLIST_CONTEXT");
        if (ctx == null) {
            String ctxToken =
                (String)ADFUtils.getPageFlowScope().get("bpmWorklistContext");
            _logger.info("Acquiring BPMContext");
            ctx =
getWorkflowServiceClient().getTaskQueryService().getWorkflowContext(ctxToken);
            JSFUtils.storeOnSession("WORKLIST_CONTEXT", ctx);
        }
        return ctx;
    }

    public IWorkflowServiceClient getWorkflowServiceClient() {     
        if (client == null) {
            client = WorkflowServiceClientFactory.getWorkflowServiceClient(WorkflowServiceClientFactory.REMOTE_CLIENT);
        }
        return client;
    }
    
    public void setTask(Task task) {
        this.task = task;
    }

    public Task getTask() throws WorkflowException {
        if (task == null) {
            task = getTaskVersionDetails();
        }
        return task;
    }

    public Task getTaskVersionDetails() throws WorkflowException {
        ITaskQueryService queryService =
            getWorkflowServiceClient().getTaskQueryService();
        IWorkflowContext ctx = getWorkflowContext();
        String taskId = (String)getParameterValue("bpmWorklistTaskId");
        String taskVersion = (String)getParameterValue("bpmWorklistTaskVersion");
        if (taskVersion == null) {
            return queryService.getTaskDetailsById(ctx, taskId);
        } else {
            return queryService.getTaskVersionDetails(ctx, taskId,
                                                      getSafeInt(taskVersion));
        }
    }

    public void initPayload() throws Exception {
        _logger.info("TaskDetailsForm.initPayload()");
        AbstractPayloadHandler handler =
            (AbstractPayloadHandler)ADFUtils.getPageFlowScope().get("payloadHandler");
        if (handler != null) {
//            handler.setPayload(getTask().getPayload());
            handler.setTask(getTask());//pass by reference
            handler.setVisibilityRules(getTaskVisibilityRules());
        }
    }

    public Map<String, IPrivilege> getTaskVisibilityRules() throws TaskMetadataServiceException,
                                                                   WorkflowException {
        //        ITaskMetadataService metadataService = getWorkflowServiceClient().getTaskMetadataService();
        //        Map<String, IPrivilege> privileges = metadataService.getTaskVisibilityRules(getWorkflowContext(), getTask());
        //        return privileges;
        //Bug. According Oracle Support is fixed in 11.1.1.6
        //Caused by: java.rmi.UnmarshalException: cannot unmarshaling return; nested excep
        // tion is: java.io.NotSerializableException: oracle.bpel.services.workflow.metadata
        // .impl.Privilege
        return null;
    }
    private void updatePayload(String action) throws Exception {
        AbstractPayloadHandler handler = (AbstractPayloadHandler)ADFUtils.getPageFlowScope().get("payloadHandler");
        if (handler != null) {
            handler.updatePayload(action);
        }
    }   

/*     public void updatePayload() throws Exception {
        _logger.fine("TaskDetailsForm.updatePayload begin");
        AbstractPayloadHandler handler =
            (AbstractPayloadHandler)ADFUtils.getPageFlowScope().get("payloadHandler");
        if (handler != null) {
            _logger.fine("handler != null");
            Map<String, Element> payloadElements =
                handler.getPayloadElements();
            Element payloadElement = getTask().getPayloadAsElement();
            _logger.fine("PayloadElement: " +
                         XMLUtil.toString(payloadElement));
            for (Map.Entry<String, Element> entry :
                 payloadElements.entrySet()) {
                if (hasWriteAccess(entry.getKey())) {
                    Element updatedElement = entry.getValue();
                    _logger.fine("updatedElement: " + XMLUtil.toString(updatedElement));
                    _logger.fine("updatedElment.getLocalName(): " + updatedElement.getLocalName());
                    PayloadUtil.replaceOrAppendPayloadElementData(payloadElement,
                                                                  updatedElement);
                }
            }
            _logger.fine("PayloadElement after update: " +
                         XMLUtil.toString(payloadElement));
            getTask().setPayloadAsElement(payloadElement);
        }
    }

    public boolean hasWriteAccess(String payloadElementName) {
        //TODO replace with proper security check
        return true;
    } */

    public String processAction() throws Exception{
        _logger.fine("TaskDetailsForm.processAction begin: [action] = " +action);
        try {        
            if ((isCommentsRequired(action)) && (!isNewComment())) {
                raiseErrorMessage("MESSAGE_EMPTY_COMMENTS_FOR_ACTION");
                return null;
            }
            if ("REASSIGN".equals(action)) {
                ADFUtils.showDialog(reassignPopup);
                return null;
            }
            if ("INFO_REQUEST".equals(action)) {
                ADFUtils.showDialog(requestInfoPopup);
                return null;
            }
            ITaskService taskService =
                getWorkflowServiceClient().getTaskService();
            
            //INFO_SUBMIT should be before updatePayload 
            //to avoid Insufficient privileges to access the task information for this task.
            //Caused by: java.sql.SQLException: ORA-20005: Task is modified
            // ORA-06512: at "DEV_SOAINFRA.WFTASKPKG_111160", line 2932
            
            if ("INFO_SUBMIT".equals(action)) {
                if ((!isNewComment()) && (!isTaskUpdatedWithComments())) {
                    raiseErrorMessage("MESSAGE_EMPTY_COMMENTS_FOR_SUBMIT_INFO");
                    return null;
                }
                taskService.submitInfoForTask(ctx, task);
                return null;
            }
            
            updatePayload(action);
            
            if ("ESCALATE".equals(action)) {
                taskService.escalateTask(ctx, task);
                return "done";
            }
            if ("WITHDRAW".equals(action)) {
                taskService.withdrawTask(ctx, task);
                return "done";
            }
            if ("SUSPEND".equals(action)) {
                taskService.suspendTask(ctx, task);
                return "done";
            }
            if ("RESUME".equals(action)) {
                task = taskService.resumeTask(ctx, task);
                return null;
            }
            if ("PURGE".equals(action)) {
                taskService.purgeTask(ctx, task);
                return "done";
            }
            if ("CLAIM".equals(action)) {
                task = taskService.acquireTask(ctx, task);
                return null;
            }
            
            if ("UPDATE".equals(action)) {
                task = taskService.updateTask(ctx, task);
                return null;
            }
            
            taskService.updateTaskOutcome(ctx, task, action);
         _logger.fine("TaskDetailsForm.processAction end");
            return "done";
        } catch (Exception e) {
            JSFUtils.addMessage(null, e.getMessage(), e.getMessage(),FacesMessage.SEVERITY_ERROR);
            return null;
        }
    }

    public void raiseErrorMessage(String messageKey) {
        String message = getDisplayString(messageKey, ctx.getLocale());
        JSFUtils.addFacesErrorMessage(message);
    }
    private ITaskAssignee parseTaskAssignee(String name_type) {
        String[] tokens = name_type.split("_");
        String type = tokens[tokens.length - 1];
        String name = null;
        if (type.equals("role")) {
            name = name_type.replace("_application_role", "");
        } else if ("group".equals(type)) {
            name = name_type.replace("_group", "");
        } else if ("user".equals(type)) {
            name = name_type.replace("_user", "");
        } else if ("position".equals(type)) {
            name = name_type.replace("_position", "");
        } else {
            throw new IllegalArgumentException("invalid format");
        }
        return new TaskAssignee(name, type);
    }
    private List<ITaskAssignee> parseTaskAssignees(List<String> strIdentities) {
        if (strIdentities == null) {
            return Collections.emptyList();
        }
        List<ITaskAssignee> taskAssignees = new ArrayList<ITaskAssignee>();
        for (String str : strIdentities) {
            taskAssignees.add(parseTaskAssignee(str));
        }
        return taskAssignees;
    }

    public static List getSelectedIdentitiesFromIdentityBrowser(List selectedIdentities) {
        List assignees = new ArrayList();
        int num = selectedIdentities.size();
        for (int i = 0; i < num; i++) {
            String name = (String)selectedIdentities.get(i);
            String idName = name.substring(0, name.lastIndexOf(95));
            String identityType = name.substring(name.lastIndexOf(95) + 1);

            if (identityType.equals("user")) {
                assignees.add(new TaskAssignee(idName, "user"));
            } else if (identityType.equals("group")) {
                assignees.add(new TaskAssignee(idName, "group"));
            } else if (identityType.equals("approle")) {
                assignees.add(new TaskAssignee(idName, "application_role"));
            }
        }
        return assignees;
    }

    public Map getActionAvailable() throws WorkflowException {
        if (dummyMap == null) {
            if (allActions == null) {
                allActions = new ArrayList<String>();
                for (Object obj :
                     getTask().getSystemAttributes().getCustomActions()) {
                    ActionType actionType = (ActionType)obj;
                    allActions.add(actionType.getAction());
                }
                for (Object obj :
                     getTask().getSystemAttributes().getSystemActions()) {
                    ActionType actionType = (ActionType)obj;
                    allActions.add(actionType.getAction());
                }
            }
            dummyMap = new HashMap() {
                    public Object get(Object obj) {
                        return allActions.contains(obj);
                    }
                };
        }
        return dummyMap;
    }

    public List<ActionType> getSystemActions() throws WorkflowException {
        if (systemActions == null) {
            systemActions = new ArrayList<ActionType>();
            for (Object obj :
                 getTask().getSystemAttributes().getSystemActions()) {
                ActionType actionType = (ActionType)obj;
                if (!excludedActions.contains(actionType.getAction())) {
                    systemActions.add(actionType);
                }
            }
        }
        return systemActions;
    }

    public List<ActionType> getCustomActions() throws WorkflowException {
        if (customActions == null) {
            customActions = getTask().getSystemAttributes().getCustomActions();
        }
        return customActions;
    }


    public boolean isActionable() {
        return ADFUtils.getPageFlowScope().get("bpmWorklistVersion") == null;
    }


    public boolean isFyiTask() throws WorkflowException {
        return getTask().getSystemAttributes().getTaskViewContext().equals("FYI");
    }


    public boolean isActionsAvailable() throws WorkflowException {
        return !getCustomActions().isEmpty() || !getSystemActions().isEmpty();
    }

    public void toggle(DisclosureEvent disclosureEvent) {
        Object obj = disclosureEvent.getSource();
        RichShowDetailHeader header = (RichShowDetailHeader)obj;
        boolean isDisclosed = header.isDisclosed();
        if (isDisclosed) {
            ALEComponentBean aleBean =
                (ALEComponentBean)ADFUtils.getPageFlowScope().get("aleComponentBean");
            aleBean.refreshHistoryDiagram();
        }
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public void setReassignPopup(RichPopup reassignPopup) {
        this.reassignPopup = reassignPopup;
    }

    public RichPopup getReassignPopup() {
        return reassignPopup;
    }

    public void setRequestInfoPopup(RichPopup requestInfoPopup) {
        this.requestInfoPopup = requestInfoPopup;
    }

    public RichPopup getRequestInfoPopup() {
        return requestInfoPopup;
    }

    public void setRouteTaskPopup(RichPopup routeTaskPopup) {
        this.routeTaskPopup = routeTaskPopup;
    }

    public RichPopup getRouteTaskPopup() {
        return routeTaskPopup;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setAttachmentsTable(RichTable attachmentsTable) {
        this.attachmentsTable = attachmentsTable;
    }

    public RichTable getAttachmentsTable() {
        return attachmentsTable;
    }

    public void setAttachmentPopup(RichPopup attachmentPopup) {
        this.attachmentPopup = attachmentPopup;
    }

    public RichPopup getAttachmentPopup() {
        return attachmentPopup;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public void setCommentScope(String commentScope) {
        this.commentScope = commentScope;
    }

    public String getCommentScope() {
        return commentScope;
    }

    public int getUserCommentSize() throws WorkflowException {
        return getTask().getUserComment().size();
    }

    public int getAttachmentSize() throws WorkflowException {
        return getTask().getAttachment().size();
    }

    public void setUserCommentTable(RichTable userCommentTable) {
        this.userCommentTable = userCommentTable;
    }

    public RichTable getUserCommentTable() {
        return userCommentTable;
    }

    public void makeCurrent(SelectionEvent selectionEvent) {
        RichTable table = (RichTable)selectionEvent.getSource();
        selectedAttachment = (AttachmentType)table.getSelectedRowData();
    }

    public boolean getCanDeleteAttachment() throws WorkflowException {
        AttachmentType attachment = selectedAttachment;
        if (attachment == null) {
            return false;
        }
        String user = getWorkflowContext().getUser();
        return user.equals(attachment.getUpdatedBy()) &&
            !"BPM".equals(attachment.getAttachmentScope());

    }

    public void hideAttachmentDeleteDialog() {
        ADFUtils.closeDialog(deleteAttachmentPopup);
    }

    public void addComment(String comment,
                           String commentScope) throws WorkflowException {
        ObjectFactory factory = new ObjectFactory();
        CommentType commentType = factory.createCommentType();
        commentType.setComment(comment);
        commentType.setIsSystemComment(false);
        commentType.setUpdatedDate(Calendar.getInstance());
        commentType.setCommentScope(commentScope);
        getTask().addUserComment(commentType);
    }

    public void handleAddCommentDialogReturn(DialogEvent dialogEvent) throws WorkflowException {
        if (dialogEvent.getOutcome() == DialogEvent.Outcome.ok) {
            addComment(comment, commentScope);
            comment = "";
            AdfFacesContext.getCurrentInstance().addPartialTarget(userCommentTable);
        }
    }

    public void clearUploadedFile() {
        richFile.setSubmittedValue(null);
        ADFUtils.closeDialog(attachmentPopup);
        RequestContext.getCurrentInstance().addPartialTarget(richFile);
    }

    public void addURLAttachment() throws Exception {
        AttachmentType attachmentType = createAttachment();
        attachmentType.setName(getURIname());
        attachmentType.setURI(getURI());
        attachmentType.setTitle(getURIname());
        getTask().addAttachment(attachmentType);
    }

    public void uploadFile() throws Exception {
        _logger.info("TaskDetailsForm.uploadFile() begin");
        AttachmentType attachmentType = createAttachment();
        UploadedFile file = getFile();
        attachmentType.setName(file.getFilename());
        attachmentType.setMimeType(file.getContentType());
        attachmentType.setInputStream(file.getInputStream());
        attachmentType = uploadFile(attachmentType);
        getTask().addAttachment(attachmentType);
        _logger.info("TaskDetailsForm.uploadFile() end");
    }

    private AttachmentType createAttachment() {
        AttachmentType attachmentType =
            new ObjectFactory().createAttachmentType();
        String taskId = getWorklistTaskId();
        String taskVersionStr = getWorklistTaskVersion();
        attachmentType.setTaskId(taskId);
        attachmentType.setVersion(getSafeInt(taskVersionStr));
        attachmentType.setAttachmentScope(getAttachmentScope());
        String comment = getDocComments();
        if ((comment != null) && (!comment.equals("")))
            attachmentType.setDescription(comment);
        return attachmentType;
    }

    private AttachmentType uploadFile(AttachmentType attachmentType) throws Exception {
        _logger.info("TaskDetailsForm.uploadFile(AttachmentType) begin");
        String fileName = attachmentType.getName();
        IWorkflowServiceClient wfSvcClient = getWorkflowServiceClient();
        IWorkflowContext context = getWorkflowContext();
        _logger.info("About to invoke WorkflowAttachmentUtil.uploadAttachment(context, wfSvcClient,\n" +
                "                                                       attachmentType, null)");
        String taskString =
            WorkflowAttachmentUtil.uploadAttachment(context, wfSvcClient,
                                                    attachmentType, null);
        Task task =
            (Task)new ObjectFactory().unmarshal(XMLUtil.parseDocumentFromXMLString(taskString).getDocumentElement());
        List attachments = task.getAttachment();
        for (Object obj : attachments) {
            AttachmentType taskAttachment = (AttachmentType)obj;
            if (fileName.equals(taskAttachment.getName())) {
                attachmentType = taskAttachment;
            }
        }
        return attachmentType;
    }

    public void hideAttachmentDialog(ActionEvent actionEvent) {
        ADFUtils.closeDialog(attachmentPopup);
    }

    public int getSafeInt(String value) {
        if (value == null || value.equals("")) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (Throwable t) {
        }
        return 0;
    }


    public void setAttachmentScope(String attachmentScope) {
        this.attachmentScope = attachmentScope;
    }

    public String getAttachmentScope() {
        return attachmentScope;
    }

    public void setURIname(String URLname) {
        this.URIname = URLname;
    }

    public String getURIname() {
        return URIname;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    public String getURI() {
        return URI;
    }

    public void setDocComments(String docComments) {
        this.docComments = docComments;
    }

    public String getDocComments() {
        return docComments;
    }

    public void setRichFile(RichInputFile richFile) {
        this.richFile = richFile;
    }

    public RichInputFile getRichFile() {
        return richFile;
    }


    public void setSelectedAttachment(AttachmentType selectedAttachment) {
        this.selectedAttachment = selectedAttachment;
    }

    public AttachmentType getSelectedAttachment() {
        return selectedAttachment;
    }

    public void setDeleteAttachmentPopup(RichPopup deleteAttachmentPopup) {
        this.deleteAttachmentPopup = deleteAttachmentPopup;
    }

    public RichPopup getDeleteAttachmentPopup() {
        return deleteAttachmentPopup;
    }

    public void deleteAttachment(ActionEvent actionEvent) throws WorkflowException,
                                                                 StaleObjectException {
        _logger.info("TaskDetailsForm.deleteAttachment(ActionEvent actionEvent) begin");
        ITaskService taskService = getWorkflowServiceClient().getTaskService();
        List attachmentsToRemove = new ArrayList();
        attachmentsToRemove.add(selectedAttachment.getName());
        task = taskService.removeAttachment(ctx, task, attachmentsToRemove);
        selectedAttachment = null;
    }

    public TaskFlowId getDynamicTaskFlowId() {
        String taskFlowId =
            (String)ADFUtils.getPageFlowScope().get("payloadTaskFlowId");
        if (taskFlowId != null) {
            return TaskFlowId.parse(taskFlowId);
        }
        return TaskFlowId.parse(blankTaskFlowId);
    }

    public void setSelectedAttachmentType(String selectedAttachmentType) {
        this.selectedAttachmentType = selectedAttachmentType;
    }

    public String getSelectedAttachmentType() {
        return selectedAttachmentType;
    }

    public void toggleAttachmentType(ValueChangeEvent event) {
        setSelectedAttachmentType((String)event.getNewValue());
        FacesContext.getCurrentInstance().renderResponse();
    }

    public void handleReassignPopupDialogReturn(DialogEvent ev) throws Exception {
        if (DialogEvent.Outcome.ok.equals(ev.getOutcome())) {
            IdentityBrowserView identityBrowserView = getIdentityBrowserView();
            List selectedIdentities =
                identityBrowserView.getSelectedIdentities();
            List<ITaskAssignee> assignees = getSelectedIdentitiesFromIdentityBrowser(selectedIdentities);
            ITaskService taskService =
                getWorkflowServiceClient().getTaskService();
            taskService.reassignTask(ctx, task, assignees);
            JSFUtils.handleNavigation(null, "done");
        }
    }

    public void handleRequestInfoPopupDialogReturn(DialogEvent ev) throws StaleObjectException,
                                                                          WorkflowException {
        if (DialogEvent.Outcome.ok.equals(ev.getOutcome())) {
            RequestInfoView requestInfoView =
                (RequestInfoView)ADFUtils.getPageFlowScope().get("requestInfoView");
            RequestInfoModel requestInfoModel =
                requestInfoView.getRequestInfoModel();
            RequestInfoVO requestInfoVO = requestInfoModel.getRequestInfoVO();
            String comment = requestInfoVO.getComments();
            addComment(comment, "BPM");
            
            String identityType = requestInfoVO.getOtherUserIdentityType();
            String requestInfoUser = null;
            if (requestInfoVO.isPastApproversRadio()){
                requestInfoUser= requestInfoVO.getRequestUser(); 
            }else{
                requestInfoUser = requestInfoVO.getRequestUserIDBrowser();
            }
            boolean reapprovalNeeded = requestInfoVO.isIsReapprovalNeeded();
            ITaskAssignee assignee = null;
            if (identityType == null) {
                assignee = new TaskAssignee(requestInfoUser, "user");
            } else {
                assignee = new TaskAssignee(requestInfoUser, identityType);
            }
            ITaskService taskService =
                getWorkflowServiceClient().getTaskService();
            if (reapprovalNeeded) {
                taskService.requestInfoForTaskWithReapproval(ctx, task,
                                                             assignee);
            } else {
                taskService.requestInfoForTask(ctx, task, assignee);
            }
            JSFUtils.handleNavigation(null, "done");
        }
    }

//    public void handleRoutePopupDialogReturn(DialogEvent ev) {
//        if (DialogEvent.Outcome.ok.equals(ev.getOutcome())) {
//            IdentityBrowserView identityBrowserView = getIdentityBrowserView();
//            TaskFlowReassignBean taskFlowReassignBean =
//                getTaskFlowReassignBean();
//            List selectedIdentities =
//                identityBrowserView.getSelectedIdentities();
//            List taskIds = taskFlowReassignBean.getSelectedTaskIds();
//            RouteTaskView routeTaskView =
//                identityBrowserView.getRouteTaskView();
//            RouteTaskModel routeTaskModel = routeTaskView.getRouteTaskModel();
//            routeTaskModel.setWFContext(ctx);
//            RouteTaskVO routeTaskVO = routeTaskModel.getRouteTaskVO();
//            String comments = routeTaskVO.getComments();
//            String action = routeTaskVO.getRoutingAction();
//            String routingType = routeTaskVO.getRoutingType();
//            String defaultOutcome = routeTaskVO.getDefalutActionForGroupVote();
//            Integer votersPercentage =
//                Integer.valueOf(routeTaskVO.getVotersPercentage());
//            if ((comments == null) || (comments.equals(""))) {
//                raiseErrorMessage("ROUTE_EMPTY_COMMENT");
//                return;
//            }
//            try {
//                if (((action == null) || (action.equals(""))) &&
//                    (!getTask().getSystemAttributes().getState().equals("ALERTED"))) {
//                    raiseErrorMessage("ROUTE_EMPTY_ACTION");
//                }
//            } catch (Exception exc) {
//                _logger.severe(exc.getMessage());
//            }
//
//            if (("parallel".equals(routingType)) &&
//                ((defaultOutcome == null) || (defaultOutcome.equals("")) ||
//                 (votersPercentage == null) ||
//                 (votersPercentage.intValue() <= 0))) {
//                raiseErrorMessage("ROUTE_EMPTY_OUTCOME_PERCENT");
//            }
//
//            if ((selectedIdentities != null) &&
//                (selectedIdentities.size() > 0)) {
//                try {
//                    routeTaskModel.takeActionforRoute((String)taskIds.get(0),
//                                                      selectedIdentities);
//                    JSFUtils.handleNavigation(null, "done");
//
//                } catch (Exception ex) {
//                    raiseErrorMessage("MESSAGE_HEADER_REQUEST_FAILED");
//                }
//            } else {
//                raiseErrorMessage("REASSIGN_EMPTY_USER");
//            }
//
//        }
//    }
    //borrowed utility methods

    private boolean isCommentsRequired(String methodName) throws Exception {
        if (methodName.trim().equals("INFO_SUBMIT"))
            return true;
        PreActionUserStepsType stepsType =
            getTask().getSystemAttributes().getPreActionUserSteps();

        if (stepsType == null)
            return false;
        List<PreActionUserStepType> steps = stepsType.getPreActionUserStep();
        if (steps == null)
            return false;
        for (PreActionUserStepType step : steps) {
            if (step.getOutcome().equals(methodName)) {
                if (step.getUserStep().equals("PROVIDE_COMMENTS")) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isNewComment() throws Exception {
        for (Object obj : getTask().getUserComment()) {
            CommentType commentType = (CommentType)obj;
            if (commentType.getUpdatedBy() == null) {
                return true;
            }
        }
        return false;
    }

    public static Object getParameterValue(String name) {
        return ADFUtils.getPageFlowScope().get(name);
    }

    public static String getWorklistTaskId() {
        return (String)getParameterValue("bpmWorklistTaskId");
    }

    public static String getWorklistTaskVersion() {
        return (String)getParameterValue("bpmWorklistTaskVersion");
    }

    private boolean isTaskUpdatedWithComments() throws Exception {
        boolean commentAdded = false;
        try {
            Task task = null;
            String taskId = getWorklistTaskId();
            ITaskQueryService queryService =
                getWorkflowServiceClient().getTaskQueryService();
            IWorkflowContext context = ctx;
            //            task = queryService.getTaskDetailsById(context, taskId);
            task = getTask();
            List taskHistory = queryService.getTaskHistory(context, taskId);
            int taskHistorySize = taskHistory.size();
            Calendar dateInfoRequestedFirst = null;
            for (int i = taskHistorySize - 1; i >= 0; i--) {
                Task tsk = (Task)taskHistory.get(i);
                String tskState = tsk.getSystemAttributes().getState();

                if (tskState.equals("INFO_REQUESTED"))
                    continue;
                dateInfoRequestedFirst =
                        ((Task)taskHistory.get(i)).getSystemAttributes().getUpdatedDate();
                break;
            }

            if (dateInfoRequestedFirst != null) {
                List commentList = task.getUserComment();

                if (commentList != null) {
                    for (int j = 0; j < commentList.size(); j++) {
                        CommentType comment = (CommentType)commentList.get(j);
                        Calendar commentUpdatedDate = comment.getUpdatedDate();
                        if (commentUpdatedDate.getTime().compareTo(dateInfoRequestedFirst.getTime()) <=
                            0) {
                            continue;
                        }
                        commentAdded = true;
                        break;
                    }
                }

            }

        } catch (Throwable t) {
            throw new Exception(t);
        }
        return commentAdded;
    }


    public static String getDisplayString(String key, Locale locale) {
        String result = null;
        if (locale == null) {
            locale = Locale.getDefault();
        }
        try {
            ResourceBundle bundle =
                ResourceBundle.getBundle("oracle.bpel.services.workflow.worklist.resources.worklist",
                                         locale);
            result = bundle.getString(key);
        } catch (MissingResourceException mre) {
            result = key;
        }
        return result;
    }


    private IdentityBrowserView getIdentityBrowserView() {
        return (IdentityBrowserView)ADFUtils.getPageFlowScope().get("identityBrowserView");
    }

    private TaskFlowReassignBean getTaskFlowReassignBean() {
        return (TaskFlowReassignBean)ADFUtils.getPageFlowScope().get("taskFlowReassignBean");
    }

    public List<SelectItem> getPriorityItems() {
        return priorityItems;
    }
    //needed for the global Function keys
    public void registerKeyboardMapping(PhaseEvent phaseEvent) {
        //need render response phase to load JavaScript
        if (phaseEvent.getPhaseId() == PhaseId.RENDER_RESPONSE) {
            FacesContext fctx = FacesContext.getCurrentInstance();
            ExtendedRenderKitService erks =
                Service.getRenderKitService(fctx, ExtendedRenderKitService.class);
            List<UIComponent> childComponents =
                fctx.getViewRoot().getChildren();
            //First child component in an ADF Faces page - and the
            //only child - is af:document. Thus no need to parse the child
            //components and check for their component family type
            String id =
                ((UIComponent)childComponents.get(0)).getClientId(fctx);
            StringBuffer script = new StringBuffer();
            //build JS string to invoke registry function loaded to the
            //page
            script.append("window.registerKeyBoardHandler('keyboardToServerNotify','" +
                          id + "')");
            erks.addScript(fctx, script.toString());
        }
    }
}

