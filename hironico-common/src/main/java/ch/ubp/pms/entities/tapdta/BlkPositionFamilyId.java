package ch.ubp.pms.entities.tapdta;

import java.io.Serializable;
import java.util.Objects;

public class BlkPositionFamilyId implements Serializable {

    private String portfolioCode;

    private String instrumentCode;

    private String depositCode;

    private Long familyId;

    private Long seqno;


    public BlkPositionFamilyId(String portfolioCode, String instrumentCode, String depositCode, Long familyId, Long seqno) {
        this.portfolioCode = portfolioCode;
        this.instrumentCode = instrumentCode;
        this.depositCode = depositCode;
        this.familyId = familyId;
        this.seqno = seqno;
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlkPositionFamilyId)) return false;
        BlkPositionFamilyId that = (BlkPositionFamilyId) o;
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
}
