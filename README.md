# Entitydemo README

To start the application in development mode, import it into your IDE and run the `Application` class. 
You can also start the application from the command line by running: 

```bash
./mvnw
```

## Purpose

This project exists primarily to allow me to create a working UI with the [Vaadin](https://vaadin.com/docs/latest/)
UI framework. I was also looking to do it in a Vaadin way as much as possible, hence the use of Maven, rather than
Gradle for building - which I am much more familiar with. I also sought to use Spring Boot tools as much as possible,
in part because that is part of the Vaadin way of doing things, and also because it is useful for me to gain more
experience with the Spring framework.

## The Application

This Application presents the User with a Map with an Entity Management panel floating over it. The Entity's in the
list may be selected, then their details will appear in the Entity fields, and the map will fly to the location
of the Entity.

Entity values may be edited, except for the Entity Id, and saved back to the Repository (in this case an in memory
`h2` DB). The Entity fields may be cleared, or a new Entity started, via the buttons. Double Clicking on the map
will also start a new Entity, with it's location fields populated to the Double Clicked location. Markers for existing
Entity's are added to the map.

### Use of @Async in the EntityService

The `EntityService` is used to save and retrieve `Entity`s from the repository. It uses Spring `@Async` annotation on
each of it's functions. As the repository is an in memory `h2` DB, with limited data, this is obviously unnecessary
from a performance point of view. This has been done as an educational exercise, as I have not previously needed to use
this annotation.

If the application was extended to move the repository to an external DB, and have it contain large numbers of
`Entity`s (ie: millions) then this kind of asynchronous access becomes truly necessary.

#### I would not use @Async

Having used it here, I found that `@Async` does not provide any benefit over the static `CompletableFuture.runAsync()`
or `CompletableFuture.supplyAsync()`. I understand that Spring `@Async` was once very helpful, as I believe it predates
the introduction of the `CompletableFuture` class to Java, but with `CompletableFuture` available `@Async` appears
to be obsolete.

### No Geospatial Index in DB

The `findByBounds()` function defined in `EntityRepository` could be made to work much better if the DB had a
Geospatial index. When working with large amounts of Geographic data, I have certainly found significant performance
improvements using Geospatial indexes, even for data sets of only a few tens of thousands of items.

In the context of this project, a Geospatial index is unnecessary, and so has not been added.

### Vaadin lessons

Part of the purpose of this project was to help me understand the [Vaadin](https://vaadin.com/docs/latest/) framework.
There were a number of things I discovered which are very helpful in understanding the framework, and how to use it.

#### Threading

As with all UI frameworks, the front end needs to be single threaded. I was unclear about how that would work in the
Vaadin framework, and could not find a clear explanation of the Vaadin threading practice. Like most UI frameworks,
the UI components are NOT thread safe, and need to be accessed in a thread safe way. Where Swing and JavaFX have a
defined UI Thread, which commands need to be dispatched to which enforces thread safety, Vaadin does not appear to have
such a thread, but does need to acquire a lock on the UI before attempting to update/modify it. Functionally this ends
up looking very similar to the developer to dispatching commands to the Swing or JavaFX UI Thread:

```java
    UI.getCurrent().access(() -> {
        entityList.clear();
        entityForm.setEntity(newEntity);
    });
```

Compared to dispatching to the Event Dispatch Thread in Swing:

```java
    SwingUtilities.invokeLater(() -> {
        entityList.clear();
        entityForm.setEntity(newEntity);
    });
```

#### Client Communication

A somewhat unique part of what Vaadin does it maintain constant communication between the back end application server,
and the front end Web Page, in order to keep them synchronised. This is not a task which is normally seen in UI
frameworks, and it does introduce some additional things which need to be managed.

As a result the `@Push` annotation on the projects `Application` class (as it implements `AppShellConfigurator`) is
necessary to ensure/allow the Vaadin application server to push changes to the Web front end.

My understanding is that Vaadin applications are run in some very secure environments, which don't allow some of the
mechanisms used to push values from the application server to the Web page - so it needs to be explicitly allowed,
with fall backs to alternatives being available for configuration. Basically I believe some environments won't allow
the the server to open a WebSocket to the browser, which is the preferred way of handling such pushes, so polling by
the web page is sometimes necessary instead.

See [Vaadin Server Push Configuration](https://vaadin.com/docs/latest/flow/advanced/server-push) for the actual
documentation.
