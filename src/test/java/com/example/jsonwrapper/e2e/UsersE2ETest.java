package com.example.jsonwrapper.e2e;

import com.example.jsonwrapper.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class UsersE2ETest {

//    @RegisterExtension
//    static WireMockExtension wm = WireMockExtension.newInstance().options(wireMockConfiq().dynamicPort()).build();


    @Autowired
    private TestRestTemplate rest;

//    @Autowired
//    private MockMvc mockMvc;

    @Test
    void getAllUsers() {

        // arrange
//        wm.stubFor(get("/users").willReturn(okJson("""
//                [
//                    {"id":1,"name":"testName","username":"testUName", "email": "test@email", "address": {}
//                ]
//                """)));

        ResponseEntity<String> res = rest.getForEntity("/api/users", String.class);
        System.out.println(res.getStatusCode());
        System.out.println(res.getBody());

//        ResponseEntity<User[]> res = rest.getForEntity("/api/users", User[].class);
//
//        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(res.getBody()).hasSize(10);


    }
}
