package com.example.demo.validation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import javax.validation.constraints.Digits;
import java.io.IOException;

@Data
public class Person {

  @Digits(integer = 200, fraction = 0, message = "code should be number and no larger than 200")
  private int ageInt;

  @Digits(integer = 200, fraction = 0, message = "code should be number and no larger than 200")
  private String ageString;

  @JsonDeserialize(using = MyIntDeserializer.class)
  private int ageStringWithCustomizeErrorMessage;
}

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
