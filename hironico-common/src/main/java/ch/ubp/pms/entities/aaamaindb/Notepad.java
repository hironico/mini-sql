package ch.ubp.pms.entities.aaamaindb;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

@Entity(name ="NOTEPAD")
@IdClass(NotepadPK.class)
@Table(schema = "AAAMAINDB", name = "NOTEPAD")
@NamedQueries({
        @NamedQuery(name = Notepad.FIND_BY_ENTITY_OBJECT_IDS, query = "SELECT n FROM NOTEPAD n WHERE n.entityDictId = :entityDictId AND n.objectId = :objectId")
})
public class Notepad implements Serializable {

    public static final String FIND_BY_ENTITY_OBJECT_IDS = "notepad.find_by_entity_object_ids";

    @Id
    @Column(name = "ENTITY_DICT_ID", precision = 14)
    private Long entityDictId;

    @Id
    @Column(name = "OBJECT_ID", precision = 14)
    private Long objectId;

    @Id
    @Column(name = "TYPE_ID", precision = 14)
    private Long typeId;

    @Id
    @Column(name  = "USER_ID", precision = 14)
    private Long userId;

    @Id
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "NOTE_D")
    private Date noteDate;

    @Column(name = "LAST_USER_ID", precision = 14)
    private Long lastUserId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "LAST_MODIF_D")
    private Date lastModifDate;

    @Column(name = "TITLE_C", length = 60)
    private String title;

    @Column(name = "NOTE_C", length = 255)
    private String note;

    public Long getEntityDictId() {
        return entityDictId;
    }

    public void setEntityDictId(Long entityDictId) {
        this.entityDictId = entityDictId;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getNoteDate() {
        return noteDate;
    }

    public void setNoteDate(Date noteDate) {
        this.noteDate = noteDate;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
