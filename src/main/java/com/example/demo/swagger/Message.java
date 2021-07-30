package com.example.demo.swagger;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class Message {

  @ApiModelProperty(hidden = true)
  private Integer code = null;

  @ApiModelProperty(hidden = true)
  private String message = null;
}