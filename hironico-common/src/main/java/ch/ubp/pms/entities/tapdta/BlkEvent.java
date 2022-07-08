package ch.ubp.pms.entities.tapdta;

import ch.ubp.pms.entities.aaamaindb.SubscriptionEvent;
import ch.ubp.pms.entities.aaamaindb.SubscriptionEventStatus;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;
import javax.persistence.*;


import static ch.ubp.pms.utils.StringUtils.isNULL;

@Entity(name = "BLK_EVENT")
@Table(name = "BLK_EVENT", schema = "TAPDTA")
@NamedQueries({
        @NamedQuery(name = BlkEvent.FIND_BY_OPECODE,
            query = "SELECT e FROM BLK_EVENT e WHERE e.opeCode = :opeCode"),
        @NamedQuery(name = BlkEvent.FIND_AFTER_PtfInstDepOpeDate,
            query = "SELECT e FROM BLK_EVENT e WHERE e.portfolioCode = :portfolio AND e.instrumentCode = :instrument AND e.depositCode = :deposit AND e.operationDate > :operationDate ORDER BY e.operationDate"),
        @NamedQuery(name = BlkEvent.FIND_BY_PortfolioInstrumentDeposit,
            query = "SELECT e FROM BLK_EVENT e WHERE e.portfolioCode = :portfolioCode AND e.instrumentCode = :instrumentCode AND e.depositCode = :depositCode ORDER BY e.operationDate")
})
public class BlkEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(BlkEvent.class.getName());

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public static final String FIND_BY_OPECODE = "BlkEvent.FIND_BY_OPERATION_CODE";
    public static final String FIND_AFTER_PtfInstDepOpeDate = "BlkEvent.FIND_AFTER_PtfInstDepOpeDate";
    public static final String FIND_BY_PortfolioInstrumentDeposit = "BlkEvent.FIND_BY_PortfolioInstrumentDeposit";

    public static final int CANCEL = 0;
    public static final int LOCKING = 1;
    public static final int UNLOCKING = 2;

    @Id
    @Column(name = "ID", precision = 14)
    protected Long id;

    @Column(name = "PORTFOLIO_CODE", length = 60)
    protected String portfolioCode;

    @Column(name = "INSTRUMENT_CODE", length = 60)
    protected String instrumentCode;

    @Column(name = "DEPOSIT_CODE", length = 60)
    protected String depositCode;

    @Column(name = "FAMILY_ID", precision = 14)
    protected Long familyId;

    @Column(name = "BLK_QUANTITY", precision = 23, scale = 9)
    protected Double blkQuantity;

    @Column(name = "BLK_OPE_TYPE", precision = 2)
    protected Long blkOpeType;

    @Column(name = "BLK_EVENT_STATUS", precision = 2)
    protected Long blkEventStatus;

    @Column(name = "START_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    protected Date startDate;

    @Column(name = "MODIFICATION_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    protected Date modificationDate;

    @Column(name = "END_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    protected Date endDate;

    @Column(name = "OPE_CODE")
    protected String opeCode;

    @Column(name = "SOURCE_OPE_CODE")
    protected String sourceOperationCode;

    @Column(name = "OPE_DATE")
    protected Date operationDate;

    private static Date parseDateProp(Properties props, String propName) {
        String dateProp = props.getProperty(propName);
        if ("NULL".equalsIgnoreCase(dateProp) || dateProp == null) {
            LOGGER.warning(String.format("Date property is null: %s", propName));
            return null;
        } else {
            try {
                return dateFormat.parse(dateProp);
            } catch (ParseException pe) {
                LOGGER.warning(String.format("Invalid date property. Format error? : %s = '%s'", propName, dateProp));
                return null;
            }
        }
    }

    public static BlkEvent of(SubscriptionEvent event) throws NumberFormatException {
        Properties props = event.parseEventProperties();
        if (props == null) {
            return null;
        }
        BlkEvent blk = new BlkEvent();
        blk.setId(System.currentTimeMillis());

        blk.setPortoflioCode(props.getProperty("portfolio"));

        blk.setIntrumentCode(props.getProperty("instr"));

        blk.setDepositCode(props.getProperty("deposit"));

        blk.setFamilyId(isNULL(props.getProperty("source_code")) ? null : Long.parseLong(props.getProperty("source_code")));

        if (event.getOperationStatus() == 0 || event.getOperationStatus() == 10) {
            blk.setBlkOpeType((long)BlkEvent.CANCEL);
            blk.setSourceOperationCode(event.getOperationCode());
        } else {
            blk.setBlkOpeType(isNULL(props.getProperty("lock_nat_e")) ? null : Long.parseLong(props.getProperty("lock_nat_e")));
            blk.setSourceOperationCode(isNULL(props.getProperty("lock_oper_code")) ? null : props.getProperty("lock_oper_code"));
        }

        blk.setBlkQuantity(isNULL(props.getProperty("quantity_n")) ? null : Double.parseDouble(props.getProperty("quantity_n").replaceAll(",", "")));

        blk.setOpeCode(props.getProperty("code"));

        Date opeDate = BlkEvent.parseDateProp(props, "operation_d");
        blk.setOperationDate(opeDate);

        Date startDate = BlkEvent.parseDateProp(props, "value_d");
        blk.setStartDate(startDate);

        Date endDate = BlkEvent.parseDateProp(props, "end_d");
        blk.setEndDate(endDate);

        blk.setModificationDate(new Date());

        blk.setBlkEventStatus(SubscriptionEventStatus.UNTREATED.getId());

        return blk;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPortfolioCode() {
        return this.portfolioCode;
    }

    public void setPortoflioCode(String portoflioCode) {
        this.portfolioCode = portoflioCode;
    }

    public String getInstrumentCode() {
        return this.instrumentCode;
    }

    public void setIntrumentCode(String instrumentCode) {
        this.instrumentCode = instrumentCode;
    }

    public String getDepositCode() {
        return this.depositCode;
    }

    public void setDepositCode(String depositCode) {
        this.depositCode = depositCode;
    }

    public Long getFamilyId() {
        return this.familyId;
    }

    public void setFamilyId(Long familyId) {
        this.familyId = familyId;
    }

    public Long getBlkOpeType() {
        return this.blkOpeType;
    }

    public void setBlkOpeType(Long blkOpeType) {
        this.blkOpeType = blkOpeType;
    }

    public Double getBlkQuantity() {
        return this.blkQuantity;
    }

    public void setBlkQuantity(Double blkQuantity) {
        this.blkQuantity = blkQuantity;
    }

    public String getOpeCode() {
        return this.opeCode;
    }

    public void setOpeCode(String opeCode) {
        this.opeCode = opeCode;
    }

    public Date getStartDate() {
        return this.startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getModificationDate() {
        return this.modificationDate;
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    public Long getBlkEventStatus() {
        return this.blkEventStatus;
    }

    public void setBlkEventStatus(Long blkEventStatus) {
        this.blkEventStatus = blkEventStatus;
    }

    public Date getOperationDate() {
        return this.operationDate;
    }

    public void setOperationDate(Date opeDate) {
        this.operationDate = opeDate;
    }

    public String getSourceOperationCode() {
        return sourceOperationCode;
    }

    public void setSourceOperationCode(String sourceOperationCode) {
        this.sourceOperationCode = sourceOperationCode;
    }

    public Date getEndDate() {
        return this.endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}