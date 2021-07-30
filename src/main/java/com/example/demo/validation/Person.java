package com.example.demo.validation;

import lombok.Data;

import javax.validation.constraints.Digits;

@Data
public class Person {

  @Digits(integer = 200, fraction = 0, message = "code should be number and not larger than 200")
  private int ageInt;

  @Digits(integer = 200, fraction = 0, message = "code should be number and not larger than 200")
  private String ageString;
}
