package ch.ubp.pms.entities.aaamaindb;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Partial mapping of the ext_operation table.
 * At least used in the subscription deamon to retreive parent operations when receiving unlock operations events.
 */
@Entity(name = "EXT_OPERATION")
@Table(name = "EXT_OPERATION", schema = "AAAMAINDB")
@NamedQueries({
        @NamedQuery(name = ExtOperation.FIND_BY_CODE, query = "select o from EXT_OPERATION o where o.code = :code")
})
public class ExtOperation implements Serializable {

    public static final String FIND_BY_CODE = "FIND_BY_CODE";

    @Id
    @Column(name = "ID", precision = 14)
    private Long id;

    @Column(name = "NATURE_E", precision = 3)
    private Long nature;

    @Column(name = "CODE", length = 60)
    private String code;

    @Column(name = "SOURCE_CODE", length = 60)
    private String sourceCode;

    @Column(name = "INSTR_ID", precision = 14)
    private Long instrumentId;

    @Column(name = "FUNCTION_RESULT_ID", precision = 14)
    private Long functionResultId;

    @Column(name = "PARENT_OPERATION_CODE", length = 60)
    private String parentOperationCode;

    @Column(name = "PARENT_EXT_OP_ID", precision = 14)
    private Long parentExtOpId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNature() {
        return nature;
    }

    public void setNature(Long nature) {
        this.nature = nature;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public Long getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(Long instrumentId) {
        this.instrumentId = instrumentId;
    }

    public Long getFunctionResultId() {
        return functionResultId;
    }

    public void setFunctionResultId(Long functionResultId) {
        this.functionResultId = functionResultId;
    }

    public String getParentOperationCode() {
        return parentOperationCode;
    }

    public void setParentOperationCode(String parentOperationCode) {
        this.parentOperationCode = parentOperationCode;
    }

    public Long getParentExtOpId() {
        return parentExtOpId;
    }

    public void setParentExtOpId(Long parentExtOpId) {
        this.parentExtOpId = parentExtOpId;
    }
}
