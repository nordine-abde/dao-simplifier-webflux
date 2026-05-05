package anordine.dao.simplifier.webflux.entity;

import java.util.UUID;

/**
 * Base hard-delete entity with UUID identifiers.
 */
public abstract class UuidEntity extends BaseEntity<UUID> {

    @Override
    protected UUID generateId() {
        return UUID.randomUUID();
    }
}
