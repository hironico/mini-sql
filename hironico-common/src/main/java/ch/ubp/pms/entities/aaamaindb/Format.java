package ch.ubp.pms.entities.aaamaindb;

import java.io.Serializable;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

@Entity(name = "FORMAT")
@Table(schema = "AAAMAINDB", name = "FORMAT")
@NamedQueries({
        @NamedQuery(name = Format.FIND_BY_CODE, query = "SELECT f FROM FORMAT f WHERE f.code = :code"),
        @NamedQuery(name = Format.FIND_CUSTOM, query = "SELECT f FROM FORMAT f WHERE f.code like 'UBP0%' or f.code like 'PB360%'")
})
public class Format implements Serializable {
    public static final String FIND_BY_CODE = "format.find_by_code";
    public static final String FIND_CUSTOM = "format.find_custom";

    @Id
    @Column(name = "ID", precision = 14, nullable = false)
    private Long id;

    @Column(name = "CODE", length = 60, nullable = false)
    private String code;

    @Column(name = "NAME", length = 60, nullable = false)
    private String name;

    @Column(name = "DENOM", length = 255)
    private String denom;

    @Column(name = "FUNCTION_DICT_ID", precision = 14, nullable = false)
    private Long functionDictId;

    @Column(name = "PARENT_FORMAT_ID", precision = 14)
    private Long parentFormatId;

    @Column(name = "ENTITY_DICT_ID", precision = 14, nullable = false)
    private Long entityDictId;

    @Column(name = "TYPE_ID", precision = 14)
    private Long typeId;

    @Column(name = "LAST_USER_ID", precision = 14)
    private Long lastUserId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "LAST_MODIF_D")
    private Date lastModifDate;

    @Column(name = "NATURE_E", precision = 3, nullable = false)
    private Long natureE;

    @Transient
    private List<Notepad> notepads = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDenom() {
        return denom;
    }

    public void setDenom(String denom) {
        this.denom = denom;
    }

    public Long getFunctionDictId() {
        return functionDictId;
    }

    public void setFunctionDictId(Long functionDictId) {
        this.functionDictId = functionDictId;
    }

    public Long getParentFormatId() {
        return parentFormatId;
    }

    public void setParentFormatId(Long parentFormatId) {
        this.parentFormatId = parentFormatId;
    }

    public Long getEntityDictId() {
        return entityDictId;
    }

    public void setEntityDictId(Long entityDictId) {
        this.entityDictId = entityDictId;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public Long getLastUserId() {
        return lastUserId;
    }

    public void setLastUserId(Long lastUserId) {
        this.lastUserId = lastUserId;
    }

    public Date getLastModifDate() {
        return lastModifDate;
    }

    public void setLastModifDate(Date lastModifDate) {
        this.lastModifDate = lastModifDate;
    }

    public Long getNatureE() {
        return natureE;
    }

    public void setNatureE(Long natureE) {
        this.natureE = natureE;
    }

    public List<Notepad> getNotepads() {
        return notepads;
    }

    public void setNotepads(List<Notepad> notepads) {
        this.notepads = notepads;
    }
}
