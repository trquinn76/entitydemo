package io.github.trquinn76.entitydemo.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * A Service of managing {@link Entity}'s.
 * 
 * The use of Spring {@code @Async} in this service is obviously not really
 * necessary in the context of this demo project, as the repository is held in
 * memory, and will not have that much data in it. {@code @Async} has been used
 * primarily for the purpose of learning about Spring's asynchronous API, and
 * partly to understand how Vaadin interacts with it.
 */
@Service
public class EntityService {

    private final EntityRepository repository;

    public EntityService(EntityRepository repo) {
        this.repository = Objects.requireNonNull(repo);

        populateDefaults();
    }

    /**
     * Upserts the given {@link Entity} to the repository.
     * 
     * If the {@link Entity} is new to the repository, then it's ID will be set by
     * the repository as it is added.
     * 
     * @param entity the {@link Entity} to upsert. May not be null.
     * @return the {@link Entity} after it has been saved to the repository. If the
     *         {@link Entity} is new, then it's ID field will have been populated by
     *         the repository.
     */
    @Async
    public CompletableFuture<Entity> upsertEntity(Entity entity) {
        Objects.requireNonNull(entity);
        return CompletableFuture.completedFuture(repository.save(entity));
    }

    /**
     * Removed the given {@link Entity} from the repository.
     * 
     * @param entity the {@link Entity} to remove from the repository. Mat not be
     *               null.
     */
    @Async
    public void removeEntity(Entity entity) {
        Objects.requireNonNull(entity);
        repository.delete(entity);
    }

    /**
     * Gets the {@link Enitty} with the given Id.
     * 
     * @param id the Id of the {@link Entity} to get. May not be null.
     * @return the {@link Entity} with the given Id, or null if no such
     *         {@link Entity} exists.
     */
    @Async
    public CompletableFuture<Entity> getEntity(Long id) {
        Objects.requireNonNull(id);
        return CompletableFuture.completedFuture(repository.findById(id).orElse(null));
    }

    /**
     * Gets all the {@link Entity}'s in the repository.
     * 
     * @return a list of all the {@link Entity}'s in the repository. May be empty.
     */
    @Async
    public CompletableFuture<List<Entity>> getAll() {
        return CompletableFuture.completedFuture(repository.findAll());
    }

    /**
     * A list of {@link Entity}'s which fall inside the specified latitude and
     * longitude bounds.
     * 
     * Bounds which cross the antimeridian cause the search to be performed with an
     * antimeridan cut. Unfortunately the map has not been set up to repeat entities
     * which fall outside the initial copy of the world.
     * 
     * @param northLat the northern bound as a latitude in degrees.
     * @param southLat the southern bound as a latitude in degrees.
     * @param westLon  the western bound as a longitude in degrees.
     * @param eastLon  the eastern bound as a longitude in degrees.
     * @return the list of {@link Entity}'s which fall within the bounds. May be
     *         empty.
     */
    @Async
    public CompletableFuture<List<Entity>> getEntities(double northLat, double southLat, double westLon,
            double eastLon) {
        List<Entity> retList = new ArrayList<>();
        if (eastLon < westLon) {
            // then crossing the antimeridian, query with an antimeridian cut.
            retList.addAll(repository.findByBounds(northLat, westLon, southLat, 180.0));
            retList.addAll(repository.findByBounds(northLat, -180.0, southLat, eastLon));
        } else {
            retList.addAll(repository.findByBounds(northLat, westLon, southLat, eastLon));
        }

        return CompletableFuture.completedFuture(retList);
    }

    private void populateDefaults() {
        Entity entity = new Entity();
        entity.setName("Adelaide");
        entity.setDescription("Capital of South Australia");
        entity.setLatitudeDegrees(-34.92702910954714);
        entity.setLongitudeDegrees(138.59959423542026);
        upsertEntity(entity);

        entity = new Entity();
        entity.setName("Melbourne");
        entity.setDescription("Capital of Victoria");
        entity.setLatitudeDegrees(-37.8182089349);
        entity.setLongitudeDegrees(144.9665951728821);
        upsertEntity(entity);

        entity = new Entity();
        entity.setName("Sydney");
        entity.setDescription("Capital of New South Wales");
        entity.setLatitudeDegrees(-33.86774306280644);
        entity.setLongitudeDegrees(151.21018230915072);
        upsertEntity(entity);

        entity = new Entity();
        entity.setName("Perth");
        entity.setDescription("Capital of Western Australia");
        entity.setLatitudeDegrees(-31.951898241576533);
        entity.setLongitudeDegrees(115.85959017276765);
        upsertEntity(entity);

        entity = new Entity();
        entity.setName("Brisbane");
        entity.setDescription("Capital of Queensland");
        entity.setLatitudeDegrees(-27.46576529566184);
        entity.setLongitudeDegrees(153.0230659246445);
        upsertEntity(entity);
    }
}
