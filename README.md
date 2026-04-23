# Hotel API Coursework

## Overview of the API design

This project is a REST API built using JAX-RS.

It has three main parts:

- `Room`
- `Sensor`
- `SensorReading`

A room can have many sensors.
A sensor belongs to one room.
A sensor can have many readings.

The API has these main endpoints:

- `GET /api/v1/` for the discovery endpoint
- `GET /api/v1/rooms` to list rooms
- `POST /api/v1/rooms` to create a room
- `GET /api/v1/rooms/{roomId}` to get one room
- `DELETE /api/v1/rooms/{roomId}` to delete a room
- `GET /api/v1/sensors` to list sensors
- `POST /api/v1/sensors` to create a sensor
- `GET /api/v1/sensors/{sensorId}/readings` to get sensor readings
- `POST /api/v1/sensors/{sensorId}/readings` to add a reading

The data is stored in memory using Java collections.
No database is used.

## How to build and run the project

1. Open the project folder.

```bash
cd /path/to/CLEINTSERVERARCHITECTURECOURSEWORK
```

2. Build the project.

```bash
mvn clean package
```

If Maven is not installed globally, you can also use:

```bash
/Users/Junaidh/Downloads/apache-maven-3.9.15/bin/mvn clean package
```

3. Run the server.

```bash
java -jar target/hotel-api-1.0-SNAPSHOT.jar
```

4. The API will run at:

```text
http://localhost:8080/api/v1
```

## Sample curl commands

### 1. Discovery endpoint

```bash
curl -X GET http://localhost:8080/api/v1/
```

### 2. Create a room

```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{"name":"Room 101","capacity":2}'
```

### 3. List all rooms

```bash
curl -X GET http://localhost:8080/api/v1/rooms \
  -H "Accept: application/json"
```

### 4. Get one room

```bash
curl -X GET http://localhost:8080/api/v1/rooms/1 \
  -H "Accept: application/json"
```

### 5. Create a sensor

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{"type":"temperature","roomId":"1"}'
```

### 6. Filter sensors by type

```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=temperature" \
  -H "Accept: application/json"
```

### 7. Add a sensor reading

```bash
curl -X POST http://localhost:8080/api/v1/sensors/YOUR_SENSOR_ID/readings \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{"value":23.5}'
```

## Coursework Questions and Answers

## Part 1

### Question 1
In your report, explain the default lifecycle of a JAXRS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your inmemory data structures (maps/lists) to prevent data loss or race conditions.

### Answer
By default, JAXRS resource classes are requestscoped. A new instance gets created for each request, not one shared instance reused across all of them. That is actually fine for most things because it stops one client's state from bleeding into another's. The problem is that instance fields vanish the moment the request ends, so they are no use for anything you need to keep around.

That is a real issue here because the API holds rooms, sensors, and readings in memory for its entire lifetime. Storing that data in instance fields would reset everything on every request. To avoid that, the shared state lives in the `DataStore` class using static collections, which survive between requests.

Concurrency is still a concern though. Requestscoped instances do not stop two requests running at the same time and both touching the same data. To deal with that, the project uses `ConcurrentHashMap` for the collections and `AtomicInteger` for room ID generation, which handles concurrent access without needing explicit locks all over the place.

### Question 2
Why is the provision of Hypermedia considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

### Answer
The point of HATEOAS is that the server tells you where you can go next, rather than expecting you to already know. A client reads the response, finds the links, and follows them without needing hardcoded paths. If something moves, a client that navigates by links is less likely to break than one that memorised a URL from a PDF two years ago.

In this project, `GET /api/v1/` returns version info, an admin contact, and pointers to the main collections. Simple, but it means a client can get its bearings just by calling that endpoint.

Static docs go stale too. A discovery endpoint reflects what the live server actually has, not what someone wrote about it six months ago. That makes integration faster and reduces the chance of a client depending on something that quietly changed.

## Part 2

### Question 1
When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and clientside processing.

### Answer
IDs only means smaller payloads. For a big collection that matters because there is less data over the wire and responses come back faster. But if the client needs anything beyond the ID, it now has to make a separate request per room. That adds up fast and makes the client code more complicated than it needs to be.

Full objects are heavier, but the client gets everything in one shot with no chasing extra endpoints and no extra logic to stitch things together.

In this project, the room list returns full objects. Room objects are small anyway, containing the ID, name, capacity, and linked sensors, so the payload stays manageable. It also makes testing much easier since you can see everything without firing off more requests.

### Question 2
Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

### Answer
Yes, it is, though the response codes differ across calls, which is worth explaining.

The first DELETE on a room with no sensors attached removes it and returns 204. Send the same request again and the room is already gone, so you get 404. The status code changed but the server's state did not because the room is still deleted either way. Idempotency is about what happens to the resource, not about getting back the same status code every time. Sending that request another ten times changes nothing further, which is exactly what idempotent means.

## Part 3

### Question 1
We explicitly use the `@Consumes(MediaType.APPLICATION_JSON)` annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAXRS handle this mismatch?

### Answer
`@Consumes(MediaType.APPLICATION_JSON)` tells the runtime this method only takes JSON. If a request arrives with a different `ContentType` such as `text/plain` or `application/xml`, JAXRS looks for a message body reader that can handle that format and convert it into the expected Java type. If it cannot find one, the request gets rejected.

That rejection comes back as `415 Unsupported Media Type`. It is a useful annotation to have because it makes the contract clear up front: send JSON or get a meaningful error, not something unpredictable.

### Question 2
You implemented this filtering using `@QueryParam`. Contrast this with an alternative design where the type is part of the URL path (for example, `/api/v1/sensors/type/CO2`). Why is the query parameter approach generally considered superior for filtering and searching collections?

### Answer
Query parameters work better for filtering because they describe a view of a collection rather than a different resource. `GET /sensors?type=temperature` is still the sensors collection, just filtered. A path like `/sensors/type/CO2` starts to look like "type" is its own nested resource, which does not really match what is happening and makes the structure harder to extend.

They also scale well. Need more filters later? Just add them: `?type=temperature&status=ACTIVE`. Building a separate path for every combination would get out of hand quickly.

In this project, `@QueryParam("type")` keeps things clean and follows the standard REST idea that paths identify resources and query parameters refine them.

## Part 4

### Question 1
Discuss the architectural benefits of the SubResource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path in one massive controller class?

### Answer
SubResource Locators keep things from getting out of hand. Instead of one class accumulating every nested route until nobody wants to touch it, you delegate so the parent handles its own logic and hands off to a dedicated class for the nested resource.

In this project, `SensorResource` deals with sensorlevel operations and `SensorReadingResource` handles reading history for a specific sensor. Each has a focused job, which makes both easier to read and easier to test in isolation.

In a large API, stuffing everything into one class is a real problem. A controller that is thousands of lines long is hard to navigate, hard to test, and tends to break in unexpected places when requirements change. Splitting by responsibility means changes in one area are less likely to reach into another.

## Part 5

### Question 1
Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

### Answer
404 means the URI the client requested does not exist. That is not what is going on here. The endpoint exists and the request is wellformed. The problem is that the data inside the body references a room that is not in the system.

`422 Unprocessable Entity` fits better. The server understood the request fine but cannot act on it because the content does not make sense. In this project, if a client sends valid JSON to create a sensor but the `roomId` does not match any room, the format is not wrong but the reference is. 422 says that clearly. 404 would just be confusing.

### Question 2
From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

### Answer
Stack traces hand attackers a lot they should not have: package names, class names, method names, line numbers, library versions. That is a map of how the application is put together. It makes reconnaissance easier because you can see what framework is in use, how the code is structured, and sometimes spot where validation or error handling might be thin.

The generic exception mapper in this project stops that from happening. Instead of sending Java internals to whoever made the request, it returns a plain JSON error. Even information that seems harmless can help an attacker build a clearer picture of how to approach the system, so cutting it off at the source is the right call.

### Question 3
Why is it advantageous to use JAXRS filters for crosscutting concerns like logging, rather than manually inserting `Logger.info()` statements inside every single resource method?

### Answer
Putting logging in every method manually works right up until you need to change it. Then you are hunting through the whole codebase and it is easy to miss things or add it inconsistently. Filters put that behaviour in one place and apply it automatically to every request.

In this project, the logging filter records the HTTP method and URI on every incoming request and the status code on every outgoing response. The resource methods do not know anything about it. The business logic stays clean, and if the logging behaviour ever needs updating, there is one file to change.