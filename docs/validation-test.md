# Spring Validation Mechanism Test

[This Stackoverflow Topic](https://stackoverflow.com/questions/68590470/add-custom-validation-on-age-field-exception-in-java-spring/68590906#68590906) 
comes up with two questions.

- Q1: how to distinguish exceptions between 
  - data binding when conversion http request body to object
  - customized validation defined as annotation on that object
- Q2: how to display valuable information when data binding fails

All the code can be found in [this repo](https://github.com/LangInteger/spring-boot-test). All test code related to 
this topic is under package `com.example.demo.validation`.

The main data structure to be used:

```java
@Data
public class Person {

  @Digits(integer = 200, fraction = 0, message = "code should be number and no larger than 200")
  private int ageInt;

  @Digits(integer = 200, fraction = 0, message = "code should be number and no larger than 200")
  private String ageString;
}
```

The project can be run with command:
                                                                                           
- `gradlew clean bootRun`

## 1 Distinguish Exceptions

It is possible to achieve this by `ControllerAdvice`. The most important thing is to find the concise exception class 
thrown, and in our case, it is `org.springframework.http.converter.HttpMessageNotReadableException`.

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
  @ResponseStatus(HttpStatus.OK)
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

## 2 Display Valuable Information for Data Binding Exception

The text got from `HttpMessageNotReadableException` is created by the spring framework and is kinda robotic. We can use 
customize json deserializer to make the message more readable. Jackson itself doesn't support 
[customized information](https://github.com/FasterXML/jackson-annotations/issues/130) to be thrown in data binding fail yet.

Add a field to `Person`:

```java
  @JsonDeserialize(using = MyIntDeserializer.class)
  private int ageStringWithCustomizeErrorMessage;
```

```java
class MyIntDeserializer extends JsonDeserializer<Integer> {

  @Override
  public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    String text = p.getText();
    if (text == null || text.equals("")) {
      return 0;
    }

    int result;
    try {
      result = Integer.parseInt(text);
    } catch (Exception ex) {
      throw new RuntimeException("ageStringWithCustomizeErrorMessage must be number");
    }

    if (result < 0 || result >= 200) {
      throw new RuntimeException("ageStringWithCustomizeErrorMessage must in (0, 200)");
    }

    return result;
  }
}
```

## 3 Test Step

### 3.1 Test Validation Fail

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

### 3.2 Test Data Binding Fail

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

### 3.3 Test Data Binding in Customized Deserializer

Request:

- curl -X POST "http://localhost:8080/validationTest" -H "accept: */*" -H "Content-Type: application/json" -d "{ \"ageInt\": 0, \"ageString\": \"0\", \"ageStringWithCustomizeErrorMessage\": \"aa\"}"

Response:

```json
{
  "code": 400,
  "message": "JSON parse error: ageStringWithCustomizeErrorMessage must be number; nested exception is com.fasterxml.jackson.databind.JsonMappingException: ageStringWithCustomizeErrorMessage must be number (through reference chain: com.example.demo.validation.Person[\"ageStringWithCustomizeErrorMessage\"])",
  "data": null
}
```

Request:

- curl -X POST "http://localhost:8080/validationTest" -H "accept: */*" -H "Content-Type: application/json" -d "{ \"ageInt\": 0, \"ageString\": \"0\", \"ageStringWithCustomizeErrorMessage\": \"-1\"}"

Response:

```json
{
  "code": 400,
  "message": "JSON parse error: ageStringWithCustomizeErrorMessage must in (0, 200); nested exception is com.fasterxml.jackson.databind.JsonMappingException: ageStringWithCustomizeErrorMessage must in (0, 200) (through reference chain: com.example.demo.validation.Person[\"ageStringWithCustomizeErrorMessage\"])",
  "data": null
}
```

It's still a little robotic, but with more concise information offered by code.

### 3.4 Tips

Open `http://localhost:8080/swagger-ui/#/validation-test-controller/validationTestUsingPOST` 
in browser and all requests can be made in web page easily.
