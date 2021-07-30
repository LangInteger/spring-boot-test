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
  public DataContainer handleMethodArgumentNotValidException(
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