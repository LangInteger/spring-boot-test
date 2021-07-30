package com.example.demo.swagger;

import com.example.demo.swagger.Message;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

  @RequestMapping(value = "/message",
      produces = {"application/json;charset=utf-8"},
      consumes = {"application/json;charset=utf-8"},
      method = RequestMethod.POST)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "request", required = true,
          dataType = "MessageDto", paramType = "body")
  })
  ResponseEntity<?> createMessage(Message message) {
    return null;
  }
}
