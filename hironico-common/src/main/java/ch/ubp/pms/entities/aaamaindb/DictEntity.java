package ch.ubp.pms.entities.aaamaindb;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

@Entity(name = "DICT_ENTITY")
@Table(schema = "AAAMAINDB", name = "DICT_ENTITY")
@NamedQueries({
        @NamedQuery(name = DictEntity.FIND_BY_SQLNAME, query = "SELECT d FROM DICT_ENTITY d WHERE d.sqlname = :sqlname")
})
public class DictEntity implements Serializable {
    public static final String FIND_BY_SQLNAME = "dict_entity.find_by_sqlname";

    @Id
    @Column(name = "DICT_ID", precision = 14)
    private Long id;

    @Column(name = "NAME", length = 60)
    private String name;

    @Column(name = "SQLNAME_C", length = 30)
    private String sqlname;

    @Column(name = "SHORT_SQLNAME_C", length = 30)
    private String shortSqlName;

    @Column(name = "NATURE_E", precision = 3)
    private Long nature;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "LAST_MODIF_D")
    private Date lastModifDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSqlname() {
        return sqlname;
    }

    public void setSqlname(String sqlname) {
        this.sqlname = sqlname;
    }

    public String getShortSqlName() {
        return shortSqlName;
    }

    public void setShortSqlName(String shortSqlName) {
        this.shortSqlName = shortSqlName;
    }

    public Long getNature() {
        return nature;
    }

    public void setNature(Long nature) {
        this.nature = nature;
    }

    public Date getLastModifDate() {
        return lastModifDate;
    }

    public void setLastModifDate(Date lastModifDate) {
        this.lastModifDate = lastModifDate;
    }
}
