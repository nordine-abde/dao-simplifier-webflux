package anordine.dao.simplifier.webflux.entity;

import java.util.UUID;

/**
 * Base soft-delete entity with UUID identifiers.
 */
public abstract class SoftDeleteUuidEntity extends SoftDeleteEntity<UUID> {

    @Override
    protected UUID generateId() {
        return UUID.randomUUID();
    }
}
