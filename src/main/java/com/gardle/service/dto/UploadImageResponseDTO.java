package com.gardle.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@ApiModel(description = "All details about the UploadImageResponse")
public class UploadImageResponseDTO {

    @ApiModelProperty(value = "The name of the uploaded image", dataType = "String")
    private String imageName;

    @ApiModelProperty(value = "The URI to download the image", dataType = "String")
    private String imageDownloadUri;

    @ApiModelProperty(value = "The URI to download the thumbnail", dataType = "String")
    private String thumbnailDownloadUri;

    @ApiModelProperty(value = "The type of the image", dataType = "String")
    private String imageType;

    @ApiModelProperty(value = "The size of the image", dataType = "long")
    private long size;
}
