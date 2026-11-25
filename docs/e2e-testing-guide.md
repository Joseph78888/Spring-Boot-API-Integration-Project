# End-to-end API testing guide

This guide teaches you how to create a reliable end-to-end (E2E) suite for JSON Wrapper. This repository is a Java 17, Spring Boot 4.1 REST API that exposes /api/users and /api/posts, then calls JSONPlaceholder with RestClient.

## What E2E means here

There is no browser UI, so browser automation is not useful here. An E2E test sends a real HTTP request to a real, running Spring Boot server and checks the HTTP response an API consumer receives.

```text
test HTTP client
       | GET /api/posts/7
       v
Spring Boot server (real, random port)
 controller -> service -> JsonPlaceHolderClient -> RestClient
                                                    |
                                                    | GET /posts/7
                                                    v
                              WireMock (local JSONPlaceholder substitute)
```

Only the external JSONPlaceholder boundary is replaced. This makes the suite deterministic: it will not fail because the internet is down, the public service changes, or you hit a rate limit. Routing, serialization, controllers, services, HTTP clients, and exception handling still run for real.

| Test level | Typical tools | Use it here for |
| --- | --- | --- |
| Unit | JUnit, Mockito | Small service branching |
| MVC slice | WebMvcTest, MockMvc | Controller validation |
| E2E API | SpringBootTest random port, WireMock | Full public API request path |
| Production smoke | curl/Newman against deployed URL | Deployment and provider availability |

Keep real JSONPlaceholder checks separate, manual, and labelled as production smoke tests. Do not call a third-party public service during normal CI E2E tests.

## Current contract to cover

| Application request | Expected downstream request | Important check |
| --- | --- | --- |
| GET /api/users | GET /users | 200; users are mapped |
| GET /api/posts | GET /posts | 200; posts are mapped |
| GET /api/posts?userId=1 | GET /posts?userId=1 | Query is forwarded |
| GET /api/posts/7 | GET /posts/7 | Path and fields are mapped |
| GET /api/posts/999 | downstream 404 | API returns ProblemDetail with 404 |
| POST /api/posts | POST /posts | Body is forwarded; app returns 201 |
| PUT /api/posts/7 | PUT /posts/7 | ID and body are forwarded |
| DELETE /api/posts/7 | DELETE /posts/7 | Request occurs; controller currently returns 200 |

The Java Post model currently stores JSON userId in field post_id. That does not map the provider's userId field or expose the intended public JSON contract. Fix it before adding the post tests.

```java
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Post {
    private Long id;
    private String title;
    private String body;

    @JsonProperty("userId")
    private Long userId;
}
```

E2E tests must assert the public JSON name userId, never a private Java field name. The first post test will demonstrate this bug if you deliberately run it before this small model correction.

## Tool choices

- SpringBootTest with RANDOM_PORT starts the real embedded server on a free port. It avoids this app's normal port 8081.
- TestRestTemplate calls the running API over HTTP. The existing Spring Boot test starter supplies it.
- WireMock starts a local HTTP server, returns controlled JSON, and records outgoing requests.
- DynamicPropertySource changes only jsonPlaceHolder.base-url in tests, leaving production configuration unchanged.

For the underlying details, see [Spring Boot testing](https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html) and [WireMock JUnit 5](https://wiremock.org/docs/junit-jupiter/).

## One-time setup

### Add WireMock

Add this dependency inside the existing dependencies element in pom.xml. The version is the current stable WireMock 3.x version in the [official installation guide](https://wiremock.org/docs/download-and-installation/).

```xml
<dependency>
    <groupId>org.wiremock</groupId>
    <artifactId>wiremock</artifactId>
    <version>3.13.2</version>
    <scope>test</scope>
</dependency>
```

Do not add another JUnit dependency: spring-boot-starter-webmvc-test already provides JUnit Jupiter and Spring test facilities.

### Organize tests

Create src/test/java/com/example/jsonwrapper/e2e/.

```text
src/test/java/com/example/jsonwrapper/
  JsonWrapperApplicationTests.java     # existing context smoke test
  e2e/
    PostsApiE2ETest.java               # posts HTTP contract
    UsersApiE2ETest.java               # users HTTP contract
```

## First complete test: posts

Create src/test/java/com/example/jsonwrapper/e2e/PostsApiE2ETest.java.

```java
package com.example.jsonwrapper.e2e;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class PostsApiE2ETest {

    @RegisterExtension
    static WireMockExtension jsonPlaceholder = WireMockExtension.newInstance()
            .options(com.github.tomakehurst.wiremock.core.WireMockConfiguration
                    .wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void externalServiceUrl(DynamicPropertyRegistry registry) {
        registry.add("jsonPlaceHolder.base-url", jsonPlaceholder::baseUrl);
    }

    @Autowired
    private TestRestTemplate http;

    @Test
    void getsOnePostAndMapsItsJson() {
        jsonPlaceholder.stubFor(get(urlEqualTo("/posts/7"))
                .willReturn(json("""
                        {"userId": 3, "id": 7,
                         "title": "A test title", "body": "A test body"}
                        """)));

        ResponseEntity<Map> response = http.getForEntity("/api/posts/7", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .containsEntry("id", 7)
                .containsEntry("userId", 3)
                .containsEntry("title", "A test title")
                .containsEntry("body", "A test body");
        jsonPlaceholder.verify(getRequestedFor(urlEqualTo("/posts/7")));
    }

    @Test
    void forwardsTheUserIdQueryParameter() {
        jsonPlaceholder.stubFor(get(urlEqualTo("/posts?userId=1"))
                .willReturn(json("""
                        [{"userId": 1, "id": 1, "title": "Only mine", "body": "..."}]
                        """)));

        ResponseEntity<Map[]> response =
                http.getForEntity("/api/posts?userId=1", Map[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0]).containsEntry("userId", 1);
        jsonPlaceholder.verify(getRequestedFor(urlEqualTo("/posts?userId=1")));
    }

    @Test
    void createsAPostAndForwardsTheBody() {
        String payload = """
                {"userId": 1, "title": "New post", "body": "Created in test"}
                """;

        jsonPlaceholder.stubFor(post(urlEqualTo("/posts"))
                .withRequestBody(equalToJson(payload))
                .willReturn(aResponse().withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"userId": 1, "id": 101,
                                 "title": "New post", "body": "Created in test"}
                                """)));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> response = http.postForEntity(
                "/api/posts", new HttpEntity<>(payload, headers), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsEntry("id", 101);
        jsonPlaceholder.verify(postRequestedFor(urlEqualTo("/posts"))
                .withRequestBody(equalToJson(payload)));
    }

    @Test
    void returnsProblemDetailWhenThePostDoesNotExist() {
        jsonPlaceholder.stubFor(get(urlEqualTo("/posts/999"))
                .willReturn(aResponse().withStatus(404)));

        ResponseEntity<Map> response = http.getForEntity("/api/posts/999", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .containsEntry("status", 404)
                .containsEntry("detail", "Post 999 was not found.");
    }

    private static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
    json(String body) {
        return aResponse().withHeader("Content-Type", "application/json").withBody(body);
    }
}
```

The test works in this order:

1. RegisterExtension manages a dynamic-port WireMock server and resets it for every test.
2. DynamicPropertySource gives its URL to RestClientConfig before Spring creates the RestClient bean.
3. stubFor defines the fake JSONPlaceholder behavior.
4. TestRestTemplate calls the application's public API, never a controller method directly.
5. Assertions validate the status and response JSON an API client sees.
6. verify proves the wrapper emitted the expected external request, catching wrong paths, query strings, and bodies.

## Complete CRUD coverage

Append these methods inside PostsApiE2ETest.

```java
@Test
void replacesAPost() {
    String payload = """
            {"userId": 1, "id": 7, "title": "Replaced", "body": "New body"}
            """;

    jsonPlaceholder.stubFor(put(urlEqualTo("/posts/7"))
            .withRequestBody(equalToJson(payload))
            .willReturn(json(payload)));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    ResponseEntity<Map> response = http.exchange(
            "/api/posts/7",
            org.springframework.http.HttpMethod.PUT,
            new HttpEntity<>(payload, headers),
            Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).containsEntry("title", "Replaced");
    jsonPlaceholder.verify(putRequestedFor(urlEqualTo("/posts/7"))
            .withRequestBody(equalToJson(payload)));
}

@Test
void deletesAPost() {
    jsonPlaceholder.stubFor(delete(urlEqualTo("/posts/7"))
            .willReturn(aResponse().withStatus(200)));

    ResponseEntity<Void> response = http.exchange(
            "/api/posts/7", org.springframework.http.HttpMethod.DELETE, null, Void.class);

    // This matches the controller as it exists today.
    // If you intentionally return 204 later, use HttpStatus.NO_CONTENT here.
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    jsonPlaceholder.verify(deleteRequestedFor(urlEqualTo("/posts/7")));
}
```

## Users endpoint

Create src/test/java/com/example/jsonwrapper/e2e/UsersApiE2ETest.java. It has the same harness because the external URL must be configured before that class's Spring context starts.

```java
package com.example.jsonwrapper.e2e;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class UsersApiE2ETest {

    @RegisterExtension
    static WireMockExtension jsonPlaceholder = WireMockExtension.newInstance()
            .options(com.github.tomakehurst.wiremock.core.WireMockConfiguration
                    .wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void externalServiceUrl(DynamicPropertyRegistry registry) {
        registry.add("jsonPlaceHolder.base-url", jsonPlaceholder::baseUrl);
    }

    @Autowired
    private TestRestTemplate http;

    @Test
    void getsUsersFromTheWrapperApi() {
        jsonPlaceholder.stubFor(get(urlEqualTo("/users"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [{"id": 1, "name": "Leanne Graham", "username": "Bret",
                                  "email": "leanne@example.com"}]
                                """)));

        ResponseEntity<Map[]> response = http.getForEntity("/api/users", Map[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0])
                .containsEntry("id", 1)
                .containsEntry("username", "Bret");
        jsonPlaceholder.verify(getRequestedFor(urlEqualTo("/users")));
    }
}
```

## Run tests

From the repository root in PowerShell:

```powershell
.\mvnw.cmd test
```

Run one class while developing:

```powershell
.\mvnw.cmd test "-Dtest=PostsApiE2ETest"
```

Run one method:

```powershell
.\mvnw.cmd test "-Dtest=PostsApiE2ETest#createsAPostAndForwardsTheBody"
```

Maven writes reports to target/surefire-reports. In IntelliJ, reload Maven after adding WireMock, then run the class with JUnit.

## Failure guide

| Symptom | Cause | Fix |
| --- | --- | --- |
| The app calls the real internet | Wrong or late property override | Use exact key jsonPlaceHolder.base-url in DynamicPropertySource |
| Connection refused | WireMock is not running when context reads the URL | Keep static WireMock extension and dynamic property in the test class |
| WireMock returns 404, app returns 502 | Stub does not match method/path/query/body | Read WireMock unmatched-request diagnostic; use verify |
| Response lacks userId | Post model has post_id instead of a mapped userId property | Apply the Post model correction above, then retain public-contract assertion |
| Problem response differs | Exception message or handler changed | Update test only for deliberate API-contract change |
| Fails only in parallel | Fixed port or shared state | Use dynamic ports and local fixtures |

## Rules for future endpoints

Every new endpoint needs E2E coverage for:

- happy-path status and public response JSON;
- forwarding of every path variable and query parameter;
- request JSON, headers, and external request body for writes;
- known provider errors, especially 404;
- unexpected provider failure; this application's current policy maps it to 502;
- WireMock verification of the outgoing provider request.

Use small JSON fixtures and names such as returnsNotFoundProblemDetailWhenDownstreamPostIsMissing. Assert HTTP contracts, never private methods or calls to a service. Keep this deterministic suite in CI, and reserve real JSONPlaceholder checks for a small, separately named deployment smoke test.

