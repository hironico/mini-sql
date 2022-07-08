package ch.ubp.pms.entities.aaamaindb;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

import javax.persistence.*;

@Entity(name = "EVENT")
@Table(name = "EVENT", schema = "AAAMAINDB")
@NamedQueries({
        @NamedQuery(name = SubscriptionEvent.FIND_BY_DESTINATIONCODE_STATUS,
                query = "SELECT e FROM EVENT e WHERE e.destinationCode = :destinationCode AND e.status = :status ORDER BY e.creationDate"),
        @NamedQuery(name = SubscriptionEvent.FIND_BY_ID,
                query = "SELECT e FROM EVENT e WHERE e.id = :id"),
        @NamedQuery(name = SubscriptionEvent.FIND_BY_STATUS,
                query = "SELECT e FROM EVENT e WHERE e.status = :status"),
        @NamedQuery(name = SubscriptionEvent.FIND_BY_OPERATION_CODE,
                query = "SELECT e FROM EVENT e WHERE e.operationCode = :operationCode ORDER BY e.creationDate DESC")

})
public class SubscriptionEvent implements Serializable {

    public static final long STATUS_EXPIRED = 5L;
    public static final long STATUS_CANCEL = 10L;
    public static final long STATUS_PENDING_VALIDATION = 25L;
    public static final long STATUS_VALIDATED = 88L;

    public static final String FIND_BY_DESTINATIONCODE_STATUS = "SubscriptionEvent.findByDestinationCodeStatus";
    public static final String FIND_BY_ID = "SubscriptionEvent.findById";
    public static final String FIND_BY_STATUS = "SubscriptionEvent.findByStatus";
    public static final String FIND_BY_OPERATION_CODE = "SubscriptionEvent.findByOperationCode";

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(SubscriptionEvent.class.getName());

    public SubscriptionEvent() {
        super();
    }

    @Id
    @Column(name = "ID", precision = 14)
    private Long id;

    @Column(name = "FUNCTION_RESULT_ID", precision = 14)
    private Long functionResultId;

    @Column(name = "HOSTNAME_C", length = 30)
    private String hostname;

    @Column(name = "USER_C", length = 30)
    private String user;

    @Column(name = "STATUS_E", precision = 3)
    @Convert(converter = SubscriptionEventStatusConverter.class)
    private SubscriptionEventStatus status = SubscriptionEventStatus.of(0L);

    @Column(name = "NATURE_E", precision = 3)
    private Long nature = 0L;

    @Column(name = "ENTITY_SQLNAME_C", length = 30)
    private String entitySQLName;

    @Column(name = "FUNCTION_SQLNAME_C", length = 30)
    private String functionSQLName;

    @Column(name = "ACTION_E", precision = 3)
    private Long action = 0L;

    @Column(name = "MODULE_E", precision = 3)
    private Long module = 0L;

    @Column(name = "CREATION_D")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @Column(name = "EXECUTION_D")
    @Temporal(TemporalType.TIMESTAMP)
    private Date executionDate;

    @Column(name = "UPDATE_STATUS_C", length = 60)
    private String updateStatusScript;

    @Column(name = "MAP_STORAGE_C")
    private String mapStorage;

    @Column(name = "FORMAT_ID", precision = 14)
    private Long formatId;

    @Column(name = "DESTINATION_C", length = 60)
    private String destinationCode;

    @Column(name = "PRIORITY_N", precision = 23, scale = 9)
    private Double priority = 0.0d;

    @Column(name = "OPERATION_STATUS_E", precision = 3)
    private Long operationStatus = 0L;

    @Column(name = "OPERATION_C", length = 60)
    private String operationCode;

    @Column(name = "SUBSCRIPTION_C", length = 60)
    private String subscriptionCode;

    @Column(name = "OP_TIMESTAMP", precision = 20)
    private Long opTimestamp;

    @Column(name = "DATA_T", length = 4000, columnDefinition = "CLOB NOT NULL")
    @Basic(fetch = FetchType.EAGER)
    @Lob
    private String data;

    @Column(name = "BUSINESS_ENTITY_CD", length = 60)
    private String businessEntityCode;

    @Column(name = "REQUEST_STATUS_E", precision = 3)
    private Long requestStatus = 20L;

    @Column(name = "EVENT_STATUS_F", precision = 3)
    private Long eventStatusFlag;

    @Column(name = "GROUPING_CODE", length = 60)
    private String groupingCode;

    public Properties parseEventProperties() {
        return SubscriptionEvent.parseEventProperties(this.getData());
    }

    private static Properties parseEventProperties(String str) {
        // change Record Separator 'RS' chars into '\n'
        str = str.replace(new String(new byte[] { 0x1E}), "\n");

        // change Unit Separator 'US' chars into ''
        str = str.replace(new String(new byte[] { 0x1F}), "");

        Properties result = new Properties();

        Arrays.stream(str.split("\n")).forEach(line -> {
            line = line.trim();
            if (line.isEmpty()) {
                return;
            }

            String[] keyVal = line.split("=");
            if (keyVal.length != 2) {
                LOGGER.warning(String.format("Cannot parse line:'%s'", line));
            } else {
                result.setProperty(keyVal[0], keyVal[1]);
            }
        });

        return result;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFunctionResultId() {
        return functionResultId;
    }

    public void setFunctionResultId(Long functionResultId) {
        this.functionResultId = functionResultId;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public SubscriptionEventStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionEventStatus status) {
        this.status = status;
    }

    public Long getNature() {
        return nature;
    }

    public void setNature(Long nature) {
        this.nature = nature;
    }

    public String getEntitySQLName() {
        return entitySQLName;
    }

    public void setEntitySQLName(String entitySQLName) {
        this.entitySQLName = entitySQLName;
    }

    public String getFunctionSQLName() {
        return functionSQLName;
    }

    public void setFunctionSQLName(String functionSQLName) {
        this.functionSQLName = functionSQLName;
    }

    public Long getAction() {
        return action;
    }

    public void setAction(Long action) {
        this.action = action;
    }

    public Long getModule() {
        return module;
    }

    public void setModule(Long module) {
        this.module = module;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(Date executionDate) {
        this.executionDate = executionDate;
    }

    public String getUpdateStatusScript() {
        return updateStatusScript;
    }

    public void setUpdateStatusScript(String updateStatusScript) {
        this.updateStatusScript = updateStatusScript;
    }

    public String getMapStorage() {
        return mapStorage;
    }

    public void setMapStorage(String mapStorage) {
        this.mapStorage = mapStorage;
    }

    public Long getFormatId() {
        return formatId;
    }

    public void setFormatId(Long formatId) {
        this.formatId = formatId;
    }

    public String getDestinationCode() {
        return destinationCode;
    }

    public void setDestinationCode(String destinationCode) {
        this.destinationCode = destinationCode;
    }

    public Double getPriority() {
        return priority;
    }

    public void setPriority(Double priority) {
        this.priority = priority;
    }

    public Long getOperationStatus() {
        return operationStatus;
    }

    public void setOperationStatus(Long operationStatus) {
        this.operationStatus = operationStatus;
    }

    public String getOperationCode() {
        return operationCode;
    }

    public void setOperationCode(String operationCode) {
        this.operationCode = operationCode;
    }

    public String getSubscriptionCode() {
        return subscriptionCode;
    }

    public void setSubscriptionCode(String subscriptionCode) {
        this.subscriptionCode = subscriptionCode;
    }

    public Long getOpTimestamp() {
        return opTimestamp;
    }

    public void setOpTimestamp(Long opTimestamp) {
        this.opTimestamp = opTimestamp;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getBusinessEntityCode() {
        return businessEntityCode;
    }

    public void setBusinessEntityCode(String businessEntityCode) {
        this.businessEntityCode = businessEntityCode;
    }

    public Long getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(Long requestStatus) {
        this.requestStatus = requestStatus;
    }

    public Long getEventStatusFlag() {
        return eventStatusFlag;
    }

    public void setEventStatusFlag(Long eventStatusFlag) {
        this.eventStatusFlag = eventStatusFlag;
    }

    public String getGroupingCode() {
        return groupingCode;
    }

    public void setGroupingCode(String groupingCode) {
        this.groupingCode = groupingCode;
    }
}