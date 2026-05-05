package anordine.dao.simplifier.webflux.service;

import anordine.dao.simplifier.webflux.entity.BaseEntity;
import anordine.dao.simplifier.webflux.exception.DefaultEntityNotFoundExceptionFactory;
import anordine.dao.simplifier.webflux.exception.EntityNotFoundExceptionFactory;
import anordine.dao.simplifier.webflux.metadata.EntityMetadata;
import anordine.dao.simplifier.webflux.metadata.EntityMetadataResolver;
import anordine.dao.simplifier.webflux.repository.SimplifiedR2dbcRepository;
import java.util.Collection;
import java.util.Objects;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Base DAO service for lifecycle-aware saves and DAO-owned reads.
 *
 * <p>Soft-delete filtering applies only to methods implemented by this service.
 * Application repository methods, derived queries, and {@code @Query} methods
 * remain application responsibility.
 *
 * @param <E> entity type
 * @param <R> repository type
 * @param <ID> entity identifier type
 */
public abstract class AbstractDaoService<
        E extends BaseEntity<ID>,
        R extends SimplifiedR2dbcRepository<E, ID>,
        ID> {

    protected final R repository;
    protected final R2dbcEntityTemplate template;
    protected final Class<E> entityClass;
    protected final EntityNotFoundExceptionFactory exceptionFactory;
    protected final EntityMetadata metadata;

    protected AbstractDaoService(
            R repository,
            R2dbcEntityTemplate template,
            Class<E> entityClass
    ) {
        this(repository, template, entityClass, new DefaultEntityNotFoundExceptionFactory());
    }

    protected AbstractDaoService(
            R repository,
            R2dbcEntityTemplate template,
            Class<E> entityClass,
            EntityNotFoundExceptionFactory exceptionFactory
    ) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.template = Objects.requireNonNull(template, "template must not be null");
        this.entityClass = Objects.requireNonNull(entityClass, "entityClass must not be null");
        this.exceptionFactory = Objects.requireNonNull(
                exceptionFactory,
                "exceptionFactory must not be null"
        );
        this.metadata = new EntityMetadataResolver(template).resolve(entityClass);
    }

    /**
     * Saves an entity after preparing DAO-managed lifecycle fields.
     */
    @Transactional
    public Mono<E> save(E entity) {
        return save(entity, false);
    }

    /**
     * Saves an entity after preparing DAO-managed lifecycle fields.
     *
     * @param asNewWithId when {@code true}, forces an insert using an already
     * assigned id
     */
    @Transactional
    public Mono<E> save(E entity, boolean asNewWithId) {
        Objects.requireNonNull(entity, "entity must not be null");
        entity.prePersist(asNewWithId);
        return repository.save(entity)
                .doOnNext(BaseEntity::markAsNotNew);
    }

    /**
     * Saves all entities after preparing DAO-managed lifecycle fields.
     */
    @Transactional
    public Flux<E> saveAll(Collection<E> entities) {
        return saveAll(entities, false);
    }

    /**
     * Saves all entities after preparing DAO-managed lifecycle fields.
     *
     * @param asNewWithId when {@code true}, forces inserts using already
     * assigned ids
     */
    @Transactional
    public Flux<E> saveAll(Collection<E> entities, boolean asNewWithId) {
        Objects.requireNonNull(entities, "entities must not be null");
        entities.forEach(entity -> {
            Objects.requireNonNull(entity, "entities must not contain null elements");
            entity.prePersist(asNewWithId);
        });
        return repository.saveAll(entities)
                .doOnNext(BaseEntity::markAsNotNew);
    }

    /**
     * Finds a row by id. Soft-delete entities are filtered by
     * {@code deleted = false}.
     */
    @Transactional(readOnly = true)
    public Mono<E> findById(ID id) {
        Objects.requireNonNull(id, "id must not be null");
        if (!metadata.softDeleteCapable()) {
            return repository.findById(id);
        }
        return template.selectOne(Query.query(visibleByIdCriteria(id)), entityClass);
    }

    /**
     * Finds a row by id or fails through the configured exception factory.
     */
    @Transactional(readOnly = true)
    public Mono<E> findByIdRequired(ID id) {
        Objects.requireNonNull(id, "id must not be null");
        return findById(id)
                .switchIfEmpty(Mono.defer(() -> Mono.error(
                        exceptionFactory.create(entityClass, id))));
    }

    /**
     * Checks row existence by id. Soft-delete entities are filtered by
     * {@code deleted = false}.
     */
    @Transactional(readOnly = true)
    public Mono<Boolean> existsById(ID id) {
        Objects.requireNonNull(id, "id must not be null");
        if (!metadata.softDeleteCapable()) {
            return repository.existsById(id);
        }
        return template.exists(Query.query(visibleByIdCriteria(id)), entityClass);
    }

    /**
     * Counts rows. Soft-delete entities count only rows with
     * {@code deleted = false}.
     */
    @Transactional(readOnly = true)
    public Mono<Long> count() {
        if (!metadata.softDeleteCapable()) {
            return repository.count();
        }
        return template.count(Query.query(visibleCriteria()), entityClass);
    }

    /**
     * Finds all rows. Soft-delete entities return only rows with
     * {@code deleted = false}.
     */
    @Transactional(readOnly = true)
    public Flux<E> findAll() {
        if (!metadata.softDeleteCapable()) {
            return repository.findAll();
        }
        return template.select(Query.query(visibleCriteria()), entityClass);
    }

    /**
     * Finds rows for the given ids. Soft-delete entities return only rows with
     * {@code deleted = false}.
     */
    @Transactional(readOnly = true)
    public Flux<E> findAllByIds(Collection<ID> ids) {
        Objects.requireNonNull(ids, "ids must not be null");
        if (ids.isEmpty()) {
            return Flux.empty();
        }
        if (!metadata.softDeleteCapable()) {
            return repository.findAllById(ids);
        }
        return template.select(Query.query(visibleByIdsCriteria(ids)), entityClass);
    }

    private Criteria visibleByIdCriteria(ID id) {
        return Criteria.where(idPropertyName()).is(id)
                .and(deletedPropertyName()).is(false);
    }

    private Criteria visibleByIdsCriteria(Collection<ID> ids) {
        return Criteria.where(idPropertyName()).in(ids)
                .and(deletedPropertyName()).is(false);
    }

    private Criteria visibleCriteria() {
        return Criteria.where(deletedPropertyName()).is(false);
    }

    private String idPropertyName() {
        return metadata.idProperty().getName();
    }

    private String deletedPropertyName() {
        return metadata.requireSoftDeleteMetadata()
                .deletedColumn()
                .getReference();
    }
}
