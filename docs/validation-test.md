# Spring Validation Mechanism Test

[This Stackoverflow Question](https://stackoverflow.com/questions/68590470/add-custom-validation-on-age-field-exception-in-java-spring/68590906#68590906) 
comes up with 

- Q1: how to distinguish exceptions between 
  - data binding
  - customized validation regulation check

There is also another requirement of:

- Q2: how to display valuable information when data binding fails

## 1 Distinguish Exceptions

In my test, I found it is possible to achieve this by `ControllerAdvice`:

```java
package com.example.demo.validation;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ControllerAdvice
public class CustomExceptionHandlerResolver {

  private static final int COMMON_PARAMETER_ERROR_CODE = 42;

  /**
   * For errors from data binding, for example, try to attach "hello" to a int field. Treat this as http-level error,
   * so response with http code 400.
   * <p>
   * - Error messages are attached to message field of response body
   * - code field of response body is not important and also assigned with 400
   * <p>
   * Another option is totally use http concept and attach error message to an http head named error, thus no need to
   * initiate a DataContainer object.
   * <p>
   * Example text from exception.getMessage():
   * <p>
   * - JSON parse error: Cannot deserialize value of type `int` from String "1a": not a valid `int` value....
   */
  @ExceptionHandler
  @ResponseBody
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public DataContainer<?> handleBindException(
      HttpServletRequest request, HttpServletResponse response, HttpMessageNotReadableException exception) {
    System.out.println("In handleBindException");
    System.out.println(exception);
    return new DataContainer(400, exception.getMessage());
  }

  /**
   * For errors from regulation violation defined in annotations such as @NotBlank with @Valid aside the @RequestBody
   * object. Treat this as business error, so response with http code 200.
   * <p>
   * - Error messages defined in validation annotations are attached to message field of response body
   * - code field of response body is important and should be defined in the whole API system
   */
  @ExceptionHandler
  @ResponseBody
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected DataContainer handleMethodArgumentNotValidException(
      HttpServletRequest request, HttpServletResponse response, MethodArgumentNotValidException ex)
      throws IOException {
    System.out.println("In handleMethodArgumentNotValidException");
    List<FieldError> errors = ex.getBindingResult().getFieldErrors();
    String errorMessages = errors.stream()
        .map(FieldError::getDefaultMessage)
        .collect(Collectors.joining(";"));
    return new DataContainer(COMMON_PARAMETER_ERROR_CODE, errorMessages);
  }

}
```

The most important thing is that we must find the concise exception class thrown, and in our case, it is
`org.springframework.http.converter.HttpMessageNotReadableException`.

## 2 Display Valuable Information for data binding exception

The text got from `HttpMessageNotReadableException` is created by the spring framework and is kinda robotic.

What if we want to use information from validation annotation we defined? I cannot find a way to achieve this by now.

## 3 Test Step

All test code related to this topic is under package `com.example.demo.validation`.

Download this project and run with 

- `gradlew clean bootRun`

### 3.1 Test `MethodArgumentNotValidException`

Request:

- curl -X POST "http://localhost:8080/validationTest" -H "accept: */*" -H "Content-Type: application/json" -d "{ \"ageInt\": \"0\", \"ageString\": \"1a\"}"

Response:

```json
{
  "code": 42,
  "message": "code should be number and not larger than 200",
  "data": null
}
```

### 3.2 Test `HttpMessageNotReadableException`

Request:

- curl -X POST "http://localhost:8080/validationTest" -H "accept: */*" -H "Content-Type: application/json" -d "{ \"ageInt\": \"1a\", \"ageString\": \"0\"}"

Response:

```json
{
  "code": 400,
  "message": "JSON parse error: Cannot deserialize value of type `int` from String \"1a\": not a valid `int` value; nested exception is com.fasterxml.jackson.databind.exc.InvalidFormatException: Cannot deserialize value of type `int` from String \"1a\": not a valid `int` value\n at [Source: (PushbackInputStream); line: 2, column: 13] (through reference chain: com.example.demo.validation.Person[\"ageInt\"])",
  "data": null
}
```

### 3.3 Tips

You can also pen `http://localhost:8080/swagger-ui/#/validation-test-controller/validationTestUsingPOST` 
in your browser and test in web page more easily.


