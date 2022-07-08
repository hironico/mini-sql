package ch.ubp.pms.entities.aaamaindb;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class NotepadPK implements Serializable {

    private Long entityDictId;

    private Long objectId;

    private Long typeId;

    private Long userId;

    private Date noteDate;

    public NotepadPK(Long entityDictId, Long objectId, Long typeId, Long userId, Date noteDate) {
        this.entityDictId = entityDictId;
        this.objectId = objectId;
        this.typeId = typeId;
        this.userId = userId;
        this.noteDate = noteDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotepadPK)) return false;
        NotepadPK notepadPK = (NotepadPK) o;
        return entityDictId.equals(notepadPK.entityDictId) &&
                objectId.equals(notepadPK.objectId) &&
                typeId.equals(notepadPK.typeId) &&
                userId.equals(notepadPK.userId) &&
                noteDate.equals(notepadPK.noteDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityDictId, objectId, typeId, userId, noteDate);
    }


}
