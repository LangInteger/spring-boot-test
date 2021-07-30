package com.example.demo.validation;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class ValidationTestController {

  @PostMapping(value = "/validationTest")
  public void validationTest(@Valid @RequestBody Person person) {
    // do nothing
  }
}
