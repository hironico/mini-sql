package ch.ubp.pms.entities.tapdta;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class BlkPositionMaxId implements Serializable {

    private String portfolioCode;

    private String instrumentCode;

    private String depositCode;

    private Long seqno;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlkPositionMaxId)) return false;
        BlkPositionMaxId that = (BlkPositionMaxId) o;
        return getPortfolioCode().equals(that.getPortfolioCode()) &&
                getInstrumentCode().equals(that.getInstrumentCode()) &&
                getDepositCode().equals(that.getDepositCode()) &&
                getSeqno().equals(that.getSeqno());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPortfolioCode(), getInstrumentCode(), getDepositCode(), getSeqno());
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

    public Long getSeqno() {
        return seqno;
    }

    public void setSeqno(Long seqno) {
        this.seqno = seqno;
    }
}
