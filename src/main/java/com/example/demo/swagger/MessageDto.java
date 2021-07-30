package com.example.demo.swagger;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description = "")
@Data
public class MessageDto {

  @ApiModelProperty(required = true, value = "")
  private Integer otherCode = null;

  @ApiModelProperty(required = true, value = "")
  private String otherMessage = null;
}