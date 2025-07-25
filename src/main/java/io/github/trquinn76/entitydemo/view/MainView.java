package io.github.trquinn76.entitydemo.view;

import java.time.Duration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import io.github.trquinn76.entitydemo.entity.Entity;
import io.github.trquinn76.entitydemo.entity.EntityService;
import reactor.core.publisher.Sinks;
import software.xdev.vaadin.maps.leaflet.MapContainer;
import software.xdev.vaadin.maps.leaflet.basictypes.LLatLng;
import software.xdev.vaadin.maps.leaflet.layer.raster.LTileLayer;
import software.xdev.vaadin.maps.leaflet.layer.ui.LMarker;
import software.xdev.vaadin.maps.leaflet.layer.ui.LMarkerOptions;
import software.xdev.vaadin.maps.leaflet.map.LMap;
import software.xdev.vaadin.maps.leaflet.registry.LComponentManagementRegistry;
import software.xdev.vaadin.maps.leaflet.registry.LDefaultComponentManagementRegistry;

/**
 * This view shows up when a user navigates to the root ('/') of the application.
 */
@Route("")
public final class MainView extends Main {
    
    private static final String ID = "entity-demo-view";
    
    private EntityService entityService;
    
    private UI ui;
    
    private EntityForm entityForm;
    private ListBox<Entity> entityList;
    
    private LComponentManagementRegistry mapRegistry;
    private LMap map;
    private Set<LMarker> markers = new HashSet<>();
    
    private Sinks.Many<MouseMoveData> mouseMovementSink;
    
    private Span coordinateLabel;
    private Span pointLabel;

    MainView(EntityService entityService) {
        this.entityService = Objects.requireNonNull(entityService);
        this.ui = UI.getCurrent();
        
        this.setId(ID);
        this.setSizeFull();
        
        Div entityManagementDiv = initEntityManagementDiv();
        Div mapDiv = initMapDiv();
        Div movementDiv = initMovementDiv();
        
        add(mapDiv, entityManagementDiv, movementDiv);
        
        initMapEventCallbacks();
    }
    
    /**
     * Called on a double click event on the map.
     * 
     * @param lat the latitude of the double click (in degrees).
     * @param lng the longitude of the double click (in degrees).
     */
    @ClientCallable
    public void mapDblClicked(double lat, double lng)
    {
        lng = normaliseLongitude(lng);
        
        Entity newEntity = new Entity();
        newEntity.setName("New Entity");
        newEntity.setLatitudeDegrees(lat);
        newEntity.setLongitudeDegrees(lng);
        
        ui.access(() -> {
            entityList.clear();
            entityForm.setEntity(newEntity);
        });
    }
    
    /**
     * Called on a move end event on the map.
     * 
     * @param northDegrees the northern bound of the map view at the end of the move.
     * @param westDegrees the western bound of the map view at the end of the move.
     * @param southDegrees the southern bound of the map view at the end of the move.
     * @param eastDegrees the eastern bound of the map view at the end of the move.
     */
    @ClientCallable
    public void moveEnd(double northDegrees, double westDegrees, double southDegrees, double eastDegrees) {
        westDegrees = normaliseLongitude(westDegrees);
        eastDegrees = normaliseLongitude(eastDegrees);
        
        repopulateMarkers(northDegrees, westDegrees, southDegrees, eastDegrees);
    }
    
    /**
     * Reports the current lat and lng, and x and y of each mouse move event.
     * 
     * @param lat Latitude in Degrees.
     * @param lng Longitude in Degrees.
     * @param x pixels from the left hand side of the component.
     * @param y pixels from the top of the component.
     */
    @ClientCallable
    public void mouseMove(double lat, double lng, double x, double y) {
        MouseMoveData data = new MouseMoveData(lat, lng, x, y);
        mouseMovementSink.tryEmitNext(data);
    }
    
    private void repopulateMarkers(double northDegrees, double westDegrees, double southDegrees, double eastDegrees) {
        this.entityService.getEntities(northDegrees, southDegrees, westDegrees, eastDegrees).thenAccept(entityList -> {
            ui.access(() -> {
                markers.forEach(marker -> marker.remove());
                markers.clear();

                for (Entity entity : entityList) {
                    LMarker marker = createMarkerForEntity(entity);
                    marker.addTo(map);
                    markers.add(marker);
                }
            });
        });
    }
    
    private LMarker createMarkerForEntity(Entity entity) {
        LMarkerOptions options = new LMarkerOptions().withTitle(entity.getName());
        LLatLng latLng = new LLatLng(mapRegistry, entity.getLatitudeDegrees(), entity.getLongitudeDegrees());
        LMarker marker = new LMarker(mapRegistry, latLng, options).bindPopup(entity.getName());
        return marker;
    }
    
    private double normaliseLongitude(double longitudeDegrees) {
        while (longitudeDegrees < -180.0) longitudeDegrees += 360.0;
        while (longitudeDegrees > 180.0) longitudeDegrees -= 360.0;
        return longitudeDegrees;
    }
    
    private Div initEntityManagementDiv() {
        Div entityManagementDiv = new Div();
        entityManagementDiv.setId("entityManagementDiv");
        
        addClassName(LumoUtility.Padding.MEDIUM);
        entityList = new ListBox<>();
        entityList.setRenderer(new ComponentRenderer<>(entity -> {
            Div div = new Div();
            div.setText(entity.getName());
            return div;
        }));
        
        this.entityService.getAll().thenAccept((list) -> {
            ui.access(() -> {
                entityList.setItems(list);
            });
        });
        
        entityForm = new EntityForm("main");
        
        entityList.addValueChangeListener(event -> {
            if (!event.getHasValue().isEmpty()) {
                ui.access(() -> {
                    entityForm.setEntity(event.getValue());
                });
            }
        });
        
        HorizontalLayout buttonLayout = new HorizontalLayout();
        Button newEntityButton = new Button("New");
        Button saveEntityButton = new Button("Save");
        Button clearEntityButton = new Button("Clear");
        buttonLayout.add(newEntityButton, saveEntityButton, clearEntityButton);
        
        newEntityButton.addClickListener(event -> {
            ui.access(() -> {
                entityList.clear();
                entityForm.setEntity(new Entity());
            });
        });
        saveEntityButton.addClickListener(event -> {
            // the returned upserted Entity may have it's ID set. 
            this.entityService.upsertEntity(entityForm.getEntity()).thenAccept((upsertedEntity) -> {
                ui.access(() -> {
                    entityList.getListDataView().addItem(upsertedEntity);
                    entityList.setValue(upsertedEntity);
                    // setting the list selection will cause the entity form to be updated.
                });
            });
        });
        clearEntityButton.addClickListener(event -> {
            ui.access(() -> {
                entityList.clear();
                entityForm.setEntity(null);
            });
        });
        
        VerticalLayout lhsComponents = new VerticalLayout();
        lhsComponents.add(entityForm, buttonLayout);
        
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.add(entityList, lhsComponents);
        entityManagementDiv.add(horizontalLayout);
        
        return entityManagementDiv;
    }
    
    private Div initMapDiv() {
        Div mapDiv = new Div();
        mapDiv.setId("mapDiv");
        mapDiv.setSizeFull();
        
        mapRegistry = new LDefaultComponentManagementRegistry(this);
        final MapContainer mapContainer = new MapContainer(mapRegistry);
        mapContainer.setSizeFull();
        map = mapContainer.getlMap();
        map.addLayer(LTileLayer.createDefaultForOpenStreetMapTileServer(mapRegistry));
        map.setView(new LLatLng(mapRegistry, -34.9285277296271, 138.59990575775), 14);
        
        mapDiv.add(mapContainer);
        
        entityList.addValueChangeListener(event -> {
            if (!event.getHasValue().isEmpty()) {
                ui.access(() -> {
                    LLatLng latLng = new LLatLng(mapRegistry, event.getValue().getLatitudeDegrees(), event.getValue().getLongitudeDegrees());
                    map.flyTo(latLng);
                });
            }
        });
        
        return mapDiv;
    }
    
    private Div initMovementDiv() {
        Div movementDiv = new Div();
        movementDiv.setId("movementDiv");
        
        VerticalLayout vertLayout = new VerticalLayout();
        coordinateLabel = new Span();
        coordinateLabel.setHeight("20px");
        coordinateLabel.setWidth("420px");
        coordinateLabel.setText("none");
        pointLabel = new Span();
        pointLabel.setHeight("20px");
        pointLabel.setWidth("420px");
        pointLabel.setText("none");
        vertLayout.add(coordinateLabel, pointLabel);
        vertLayout.setWidth("420px");
        vertLayout.setHeight("80px");
        
        movementDiv.add(vertLayout);
        
        mouseMovementSink = Sinks.many().multicast().onBackpressureBuffer();
        // sampling every 50 milliseconds. Found longer sampling times personally annoying in my local development
        // environment, so set it to this speed. In a real deployment a longer sampling time of 100 to 250 milliseconds
        // might be preferred to manage/prevent flooding of network messages.
        mouseMovementSink.asFlux().sample(Duration.ofMillis(50)).subscribe(mouseMoveData -> {
            ui.access(() -> {
                coordinateLabel.setText("Coordinate: " + mouseMoveData.lat() + ", " + mouseMoveData.lng());
                pointLabel.setText("X: " + mouseMoveData.x() + ", Y: " + mouseMoveData.y());
            });
        });
        
        return movementDiv;
    }
    
    private void initMapEventCallbacks() {
        ui.access(() -> {
            map.on("dblclick", "e => document.getElementById('" + ID + "').$server.mapDblClicked(e.latlng.lat, e.latlng.lng)");
            map.on("moveend", "e => { "
                    + "const bounds = e.target.getBounds(); "
                    + "document.getElementById('" + ID + "').$server.moveEnd(bounds._northEast.lat, bounds._southWest.lng, bounds._southWest.lat, bounds._northEast.lng); "
                    + "}");
            
            map.on("mousemove", "e => { "
                    + "document.getElementById('" + ID + "').$server.mouseMove(e.latlng.lat, e.latlng.lng, e.containerPoint.x, e.containerPoint.y); "
                    + "}");
        });
    }
    
    static record MouseMoveData(double lat, double lng, double x, double y) {}
}
