package io.github.trquinn76.entitydemo.view;

import org.springframework.lang.Nullable;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;

import io.github.trquinn76.entitydemo.entity.Entity;

public class EntityForm extends Composite<FormLayout> {
    
    private static final long serialVersionUID = -6413538782305151613L;

    private final Binder<Entity> binder;
    private @Nullable Entity currentEntity = null;

    IntegerField idField;
    TextField nameField;
    TextField descriptionField;
    NumberField latitudeDegreesField;
    NumberField longitudeDegreesField;

    public EntityForm(String id) {
        setId("entity-form-" + id);

        idField = new IntegerField("ID");
        idField.setEnabled(false);
        nameField = new TextField("Name");
        descriptionField = new TextField("Description");

        latitudeDegreesField = new NumberField("Latitude");
        latitudeDegreesField.setMin(-90.0);
        latitudeDegreesField.setMax(90);
        latitudeDegreesField.setWidthFull();
        Div latitudeSuffix = new Div();
        latitudeSuffix.setText("degrees");
        latitudeDegreesField.setSuffixComponent(latitudeSuffix);

        longitudeDegreesField = new NumberField("Longitude");
        longitudeDegreesField.setMin(-180);
        longitudeDegreesField.setMax(180);
        longitudeDegreesField.setWidthFull();
        Div longitudeSuffix = new Div();
        longitudeSuffix.setText("degrees");
        longitudeDegreesField.setSuffixComponent(longitudeSuffix);

        FormLayout layout = getContent();
        layout.setAutoResponsive(true);
        layout.addFormRow(idField);
        layout.addFormRow(nameField);
        layout.addFormRow(descriptionField);
        layout.addFormRow(latitudeDegreesField, longitudeDegreesField);
        layout.setExpandColumns(true);
        layout.setExpandFields(true);
        
        binder = new Binder<>();
        binder.forField(idField).withConverter(new Converter<Integer, Long>() {

            private static final long serialVersionUID = 2255899645936654764L;

            @Override
            public Result<Long> convertToModel(Integer value, ValueContext context) {
                Long retval = null;
                if (value != null) {
                    retval = Long.valueOf(value.longValue());
                }
                return Result.ok(retval);
            }

            @Override
            public Integer convertToPresentation(Long value, ValueContext context) {
                Integer retval = null;
                if (value != null) {
                    retval = Integer.valueOf(value.intValue());
                }
                return retval;
            }
            
        }).bindReadOnly(Entity::getId);
        binder.forField(nameField).bind(Entity::getName, Entity::setName);
        binder.forField(descriptionField).bind(Entity::getDescription, Entity::setDescription);
        binder.forField(latitudeDegreesField).bind(Entity::getLatitudeDegrees, Entity::setLatitudeDegrees);
        binder.forField(longitudeDegreesField).bind(Entity::getLongitudeDegrees, Entity::setLongitudeDegrees);
        
        updateEnabledState();
    }
    
    public void setEntity(@Nullable Entity entity) {
        this.currentEntity = entity;
        binder.readBean(entity);
        updateEnabledState();
    }
    
    public @Nullable Entity getEntity() {
        if (currentEntity != null && binder.writeBeanIfValid(currentEntity)) {
            return currentEntity;
        }
        return null;
    }
    
    protected void updateEnabledState() {
        if (currentEntity == null) {
            this.getContent().setEnabled(false);
        }
        else {
            this.getContent().setEnabled(true);
        }
    }
}
