package anordine.dao.simplifier.webflux.exception;

/**
 * Default factory for generic entity-not-found exceptions.
 */
public final class DefaultEntityNotFoundExceptionFactory implements EntityNotFoundExceptionFactory {

    /**
     * Creates the default exception factory.
     */
    public DefaultEntityNotFoundExceptionFactory() {
    }

    /**
     * Creates an {@link EntityNotFoundException}.
     */
    @Override
    public RuntimeException create(Class<?> entityClass, Object id) {
        return new EntityNotFoundException(entityClass, id);
    }
}
