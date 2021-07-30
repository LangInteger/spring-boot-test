package com.example.demo.validation;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DataContainer<T>  {

  // return 0 when success and others when failing
  private int code;

  // return ok when success and useful information when fail
  private String message;

  // return things like your student when success, and null when fail
  private T data;

  public DataContainer(int code, String message) {
    this.code = code;
    this.message = message;
  }

}