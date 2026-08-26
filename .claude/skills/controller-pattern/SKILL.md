---
name: controller-pattern
description: Sam's shape for REST controllers — @RestController with no class-level prefix, full path per mapping, always ResponseEntity, no logic, exceptions left to ControllerExceptionHandler. Load before writing or editing any controller.
---

# Controller pattern

A controller logs the operation, calls one service method, and wraps the result. That is the whole job.

## The reference controller

```java
@RestController
public class Controller {

    private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

    private final Service service;

    public Controller(Service service) {
        this.service = service;
    }

    @GetMapping("/companies")
    public ResponseEntity<List<String>> getCompanies(@RequestParam("postcode") final String postcode,
                                                     @RequestParam("radius") final long radius) {
        LOGGER.info("Getting companies within [{}] mile radius of postcode [{}]", radius, postcode);

        return ResponseEntity.ok(service.getCompanies(postcode, radius));
    }
}
```

## No class-level prefix, ever

`@RestController` alone at the top. Never put `@RequestMapping` on the class. Every mapping annotation
carries the complete path, and repeating a prefix across methods is accepted and expected:

```java
@RestController
public class Controller {

    @GetMapping("/companies")
    ...

    @GetMapping("/companies/{companyNumber}")
    ...
}
```

Not this — the full path exists nowhere in the file, so searching for it finds nothing:

```java
@RestController
@RequestMapping("/companies")
public class Controller {

    @GetMapping("/{companyNumber}")
    ...
}
```

The duplication is the price of being able to grep a literal URL and land on its handler.

## Always `ResponseEntity<>`

Never return a bare body type. `ResponseEntity<Void>` where there is no body:

```java
@DeleteMapping("/companies/{companyNumber}")
public ResponseEntity<Void> deleteCompany(@PathVariable("companyNumber") final String companyNumber) {
    LOGGER.info("Deleting company [{}]", companyNumber);

    service.deleteCompany(companyNumber);

    return ResponseEntity.noContent().build();
}
```

## No logic

Log, call one service method, wrap the result. No branching, no mapping, no assembling a response from
two calls, no validation beyond what the annotations already do. A method that needs a decision is a
method whose decision belongs in the service.

Constructor injection, `private final` fields, no field `@Autowired`. Entities never appear in a
signature — map to DTOs at the boundary.

## Exceptions are somebody else's job

No try/catch. No status-code mapping. No error logging. Exceptions propagate out of the controller to
`ControllerExceptionHandler` in `common/exceptions/`, which owns both the status code and the error log.

That class is not written yet, so a controller today simply lets exceptions escape. Do not compensate for
its absence by catching in the controller.

## Log messages stay short

One INFO line naming the operation with enough to identify it. Do not mechanically grow the message as
the signature grows — a parameter does not earn a place in the log just by existing:

```java
LOGGER.info("Getting companies within [{}] mile radius of postcode [{}]", radius, postcode);
```

Not `"Getting companies radius [{}] postcode [{}] limit [{}] offset [{}] sort [{}]"`.

## Both kinds of test

Every controller gets a unit test and an integration test.

**`ControllerTest`** — plain Mockito, **no MockMvc**. Calls the controller method directly and asserts on
the returned `ResponseEntity`. The shape comes from the `unit-test-pattern` skill.

**`ControllerIT`** — `@SpringBootTest(webEnvironment = WebEnvironment.MOCK)` with `@AutoConfigureMockMvc`,
driving the real HTTP path through `MockMvc`. Endpoint paths and parameter values become constants:

```java
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class ControllerIT {

    private static final String GET_COMPANIES_ENDPOINT = "/companies";
    private static final String POSTCODE = "G128AU";
    private static final String RADIUS = "5";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldSendGetRequestToGetCompaniesEndpointAndReturnListOfCompanies() throws Exception {
        // given / when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(GET_COMPANIES_ENDPOINT)
                        .queryParam("postcode", POSTCODE)
                        .queryParam("radius", RADIUS))
                .andExpect(status().isOk());

        // then
        final String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        assertEquals("[]", responseBody);
    }
}
```
