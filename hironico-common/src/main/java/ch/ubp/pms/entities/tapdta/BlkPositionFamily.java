package ch.ubp.pms.entities.tapdta;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity(name = "BLK_POSITION_FAMILY")
@Table(name = "BLK_POSITION_FAMILY", schema = "TAPDTA")
@IdClass(BlkPositionFamilyId.class)
@NamedQueries({
        @NamedQuery(name = BlkPositionFamily.FIND_MAX_SEQNO_BY_PtfInstDepFamOpeDate,
            query = "SELECT MAX(p.seqno) from BLK_POSITION_FAMILY p WHERE p.portfolioCode = :portfolioCode AND p.instrumentCode = :instrumentCode AND p.depositCode = :depositCode AND p.familyId = :familyId AND p.operationDate <= :operationDate"),
        @NamedQuery(name = BlkPositionFamily.FIND_BY_PtfInstDepFamOpeDate,
            query = "SELECT p FROM BLK_POSITION_FAMILY p WHERE p.portfolioCode = :portfolioCode AND p.instrumentCode = :instrumentCode AND p.depositCode = :depositCode AND p.familyId = :familyId AND p.operationDate = :opeDate"),
        @NamedQuery(name = BlkPositionFamily.FIND_BEFORE_BY_PtfInstDepDate,
            query = "SELECT p FROM BLK_POSITION_FAMILY p WHERE p.portfolioCode = :portfolioCode AND p.instrumentCode = :instrumentCode AND p.depositCode = :depositCode AND p.operationDate <= :operationDate ORDER BY p.familyId, p.operationDate DESC"),
        @NamedQuery(name = BlkPositionFamily.FIND_BY_Seqno,
            query = "SELECT p FROM BLK_POSITION_FAMILY p WHERE p.seqno = :seqno"),
        @NamedQuery(name = BlkPositionFamily.FIND_BY_PortfolioInstrumentDeposit,
            query = "SELECT p FROM BLK_POSITION_FAMILY p WHERE p.instrumentCode = :instrumentCode AND p.portfolioCode = :portfolioCode AND p.depositCode = :depositCode"),
        @NamedQuery(name = BlkPositionFamily.FIND_After,
            query = "select f FROM BLK_POSITION_FAMILY f where f.portfolioCode = :portfolioCode and f.instrumentCode = :instrumentCode and f.depositCode = :depositCode and f.operationDate > :operationDate")
})
public class BlkPositionFamily implements Serializable {

    public static final String FIND_MAX_SEQNO_BY_PtfInstDepFamOpeDate = "BlkPositionFamily.FIND_MAX_SEQNO_BY_PtfInstDepFamOpeDate";
    public static final String FIND_BY_PtfInstDepFamOpeDate = "BlkPositionFamily.FIND_BY_PotfInstDepFamOpeDae";
    public static final String FIND_BEFORE_BY_PtfInstDepDate = "BlkPositionFamily.FIND_BEFORE_BY_PtfInstDepDate";
    public static final String FIND_BY_Seqno = "BlkPositionFamily.FIND_BY_Seqno";
    public static final String FIND_BY_PortfolioInstrumentDeposit = "BlkPositionFamily.FIND_BY_PortfolioInstrumentDeposit";
    public static final String FIND_After = "BlkPositionFamily.FIND_After";

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
    @Column(name = "FAMILY_ID", precision = 14)
    private Long familyId;

    @Id
    @Column(name = "SEQNO", precision = 22)
    @SequenceGenerator(schema = "TAPDTA", name = "BLK_POSITION_FAMILY_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BLK_POSITION_FAMILY_SEQ")
    private Long seqno;

    @Column(name = "OPERATION_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date operationDate;

    @Column(name = "BLK_QUANTITY", precision = 23, scale = 9)
    private Double blkQuantity;

    @Column(name = "CREATION_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @Column(name = "MODIFICATION_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlkPositionFamily)) return false;
        BlkPositionFamily that = (BlkPositionFamily) o;
        return getPortfolioCode().equals(that.getPortfolioCode()) &&
                getInstrumentCode().equals(that.getInstrumentCode()) &&
                getDepositCode().equals(that.getDepositCode()) &&
                getFamilyId().equals(that.getFamilyId()) &&
                getSeqno().equals(that.getSeqno());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPortfolioCode(), getInstrumentCode(), getDepositCode(), getFamilyId(), getSeqno());
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

    public Long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(Long familyId) {
        this.familyId = familyId;
    }

    public Long getSeqno() {
        return this.seqno;
    }

    public void setSeqno(Long seqno) {
        this.seqno = seqno;
    }

    public Double getBlkQuantity() {
        return blkQuantity;
    }

    public void setBlkQuantity(Double blkQuantity) {
        this.blkQuantity = blkQuantity;
    }

    public Date getOperationDate() {
        return operationDate;
    }

    public void setOperationDate(Date operationDate) {
        this.operationDate = operationDate;
    }

    //// technical dates

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
}
