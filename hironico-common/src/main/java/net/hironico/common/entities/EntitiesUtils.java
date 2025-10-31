package net.hironico.common.entities;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * Utility class for entity management operations with JPA EntityManager.
 */
public class EntitiesUtils {

    /**
     * Saves the entity into the database using commit transaction if possible. the transaction scope is the whole
     * set of given entities. If one fails then the whole batch will be rollback.
     * @param entityManager properly initialized entity manager
     * @param entities one or more entity to persist in the DB.
     * @return merged entities in a list.
     * @throws Exception in case of any problem
     */
    @SafeVarargs
    public static <T> List<T> saveEntities(EntityManager entityManager, T... entities)
    throws Exception {
        try {
            EntityTransaction tran = entityManager.getTransaction();

            tran.begin();

            List<T> result = new ArrayList<>();
            for(T entity : entities) {
                result.add(entityManager.merge(entity));
            }

            if (tran.isActive()) {
                tran.commit();
            }

            return result;
        } catch (Throwable t) {
            throw new Exception(t);
        }
    }

    /**
     * Saves the entity into the database using commit transaction if possible.
     * @param entityManager properly initialized entity manager
     * @param entity entity object to save into database.
     * @return merged entity
     * @throws Exception in case of any problem
     */
    public static <T> T saveEntity(EntityManager entityManager, T entity)
    throws Exception {
        try {
            EntityTransaction tran = entityManager.getTransaction();

            tran.begin();
            entity = entityManager.merge(entity);

            if (tran.isActive()) {
                tran.commit();
            }

            return entity;
        } catch (Throwable t) {
            throw new Exception(t);
        }
    }

    /**
     * Removes the entity from the database.
     * @param entityManager properly initialized entity manager
     * @param entity entity object to remove from database
     * @throws Exception in case of any problem
     */
    public static void removeEntity(EntityManager entityManager, Object entity)
    throws Exception {
        try {
            EntityTransaction tran = entityManager.getTransaction();

            tran.begin();
            entityManager.remove(entity);

            if (tran.isActive()) {
                tran.commit();
            }
        } catch (Throwable t) {
            throw new Exception(t);
        }
    }

    /**
     * REmoves all provided entities into a SINGLE transaction.
     * @param entityManager the entitiy manager to use
     * @param entities list of entities to delete in the database.
     * @throws Exception in case of problem
     */
    public static void removeEntities(EntityManager entityManager, Object... entities)
    throws Exception {

        if (entities.length == 0) {
            return;
        }

        try {
            EntityTransaction tran = entityManager.getTransaction();

            tran.begin();

            for(Object entity : entities) {
                entityManager.remove(entity);
            }

            if (tran.isActive()) {
                tran.commit();
            }
        } catch (Throwable t) {
            throw new Exception(t);
        }
    }
}
