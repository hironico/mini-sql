package ch.ubp.pms.entities.tapdta;

import javax.persistence.*;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

@Entity(name = "BLK_POSITION_MAX")
@Table(name = "BLK_POSITION_MAX", schema = "TAPDTA")
@IdClass(BlkPositionMaxId.class)
@NamedQueries({
        @NamedQuery(name = BlkPositionMax.FIND_Max_BY_PtfInstrDepOpeDate,
                    query = "SELECT mm from BLK_POSITION_MAX mm where mm.seqno = (SELECT MAX(m.seqno) from BLK_POSITION_MAX m WHERE m.portfolioCode = :portfolioCode AND m.instrumentCode = :instrumentCode AND m.depositCode = :depositCode AND m.operationDate <= :operationDate)"),
        @NamedQuery(name = BlkPositionMax.FIND_BY_PortfolioInstrumentDeposit,
                    query = "select m from BLK_POSITION_MAX m where m.portfolioCode = :portfolioCode and m.instrumentCode = :instrumentCode and m.depositCode = :depositCode"),
        @NamedQuery(name = BlkPositionMax.FIND_After,
                    query = "select m FROM BLK_POSITION_MAX m where m.portfolioCode = :portfolioCode and m.instrumentCode = :instrumentCode and m.depositCode = :depositCode and m.operationDate > :operationDate")
})
public class BlkPositionMax implements Serializable {
    public static final String FIND_Max_BY_PtfInstrDepOpeDate = "BlkPositionMax.FIND_Max_BY_PtfInstrDepOpeDate";
    public static final String FIND_BY_PortfolioInstrumentDeposit = "BlkPositionMax.FIND_BY_PortfolioInstrumentDeposit";
    public static final String FIND_After = "BlkPositionMax.FIND_After";

    /**
     * Must be compliant with the date format setup in the Operation_BLK.template file
     * located in the resources of the subscription maven module.
     */
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");

    @Id
    @Column(name = "PORTFOLIO_CODE", length = 60)
    private String portfolioCode;

    @Id
    @Column(name = "INSTRUMENT_CODE", length = 60)
    private String instrumentCode;

    @Id
    @Column(name = "DEPOSIT_CODE", length = 60)
    private String depositCode;

    @Id
    @Column(name = "SEQNO", precision = 22)
    @SequenceGenerator(schema = "TAPDTA", name = "BLK_POSITION_MAX_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BLK_POSITION_MAX_SEQ")
    private Long seqno;

    @Column(name = "OPERATION_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date operationDate;

    @Column(name = "SOURCE_FAMILY_ID", precision = 14)
    private Long sourceFamilyId;

    @Column(name = "BLK_QUANTITY", precision = 23, scale = 9)
    private Double blkQuantity;

    @Column(name = "CREATION_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @Column(name = "MODIFICATION_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationDate;

    @Transient
    private Double diffWithCurrent = 0.0d;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlkPositionMax)) return false;
        BlkPositionMax that = (BlkPositionMax) o;
        return getPortfolioCode().equals(that.getPortfolioCode()) &&
                getInstrumentCode().equals(that.getInstrumentCode()) &&
                getDepositCode().equals(that.getDepositCode()) &&
                getSeqno().equals(that.getSeqno());
    }

    private String getLockNature() {
        return this.diffWithCurrent >= 0.0d ? "1" : "2";
    }

    public String getOperationType() {
        return "1".equals(getLockNature()) ? "BLK" : "UBK";
    }

    /**
     * Get the GWP data row for inserting into an imp file.
     * <strong>The data row is using the diff with current property as a locking quantity !</strong>
     * @param separator field separator. If null then ';' is used.
     * @return the DAT row for inserting into the IMP file.
     */
    public String toGWPDataRow(String separator, String operationCode) {
        separator = separator == null ? ";" : separator;
        String lockNature = getLockNature();
        String nature = "12";
        String operationStatus = "90";
        String lockType = "BLK_ENGINE";
        String operationType = getOperationType();
        String operationSubType = ""; // could be 'BLK_BLK'
        String udOpePrAnonym = "0"; // needed ?

        StringBuilder sb = new StringBuilder();
        sb.append("DAT ")
                .append(operationCode).append(separator)
                .append(getDepositCode()).append(separator)
                .append(getInstrumentCode()).append(separator)
                .append(lockType).append(separator)
                .append(lockNature).append(separator)
                .append(nature).append(separator)
                .append(BlkPositionMax.dateFormat.format(this.getOperationDate())).append(separator)
                .append(getPortfolioCode()).append(separator)
                .append(Math.abs(getDiffWithCurrent())).append(separator)
                .append("Generated by TAPDOCTOR's BLK_ENGINE").append(separator)
                .append(operationStatus).append(separator)
                .append(operationSubType).append(separator)
                .append(udOpePrAnonym).append(separator)
                .append(this.getSourceFamilyId()).append(separator)
                .append(operationType).append(separator)
                .append("\n");

        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPortfolioCode(), getInstrumentCode(), getDepositCode());
    }

    public String getPortfolioCode() {
        return portfolioCode;
    }

    public void setPortfolioCode(String portfolioCode) {
        this.portfolioCode = portfolioCode;
    }

    public String getInstrumentCode() {
        return instrumentCode;
    }

    public void setInstrumentCode(String instrumentCode) {
        this.instrumentCode = instrumentCode;
    }

    public String getDepositCode() {
        return depositCode;
    }

    public void setDepositCode(String depositCode) {
        this.depositCode = depositCode;
    }

    public Long getSourceFamilyId() {
        return sourceFamilyId;
    }

    public void setSourceFamilyId(Long sourceFamilyId) {
        this.sourceFamilyId = sourceFamilyId;
    }

    public Double getBlkQuantity() {
        return blkQuantity;
    }

    public void setBlkQuantity(Double blkQuantity) {
        this.blkQuantity = blkQuantity;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    public Double getDiffWithCurrent() {
        return diffWithCurrent;
    }

    public void setDiffWithCurrent(Double diffWithCurrent) {
        this.diffWithCurrent = diffWithCurrent;
    }

    public Date getOperationDate() {
        return operationDate;
    }

    public void setOperationDate(Date operationDate) {
        this.operationDate = operationDate;
    }

    public Long getSeqno() {
        return seqno;
    }

    public void setSeqno(Long seqno) {
        this.seqno = seqno;
    }
}
