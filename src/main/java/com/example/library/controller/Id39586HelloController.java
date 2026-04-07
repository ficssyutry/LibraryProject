package com.example.library.controller;

import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@RestController
public class Id39586HelloController {

    @GetMapping("/hello")
    public String helloWorld() {
        return "hello world";
    }

    @PutMapping("/hello-put")
    public String helloPut(@RequestBody String name) {
        return "hello " + name;
    }


    @PostMapping("/hello-with-name")
    public String helloWithName(@RequestParam("name") String name) {
        return "hello " + name;
    }

    @PostMapping("/hello-body-json")
    public String helloWithJsonBody(@RequestBody String requestBody) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(requestBody);
        return jsonNode.get("name").asString();
    }


}