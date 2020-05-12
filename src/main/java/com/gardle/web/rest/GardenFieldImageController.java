package com.gardle.web.rest;

import com.gardle.security.AuthoritiesConstants;
import com.gardle.service.ImageStorageService;
import com.gardle.service.dto.UploadImageResponseDTO;
import io.github.jhipster.web.util.HeaderUtil;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Api(value = "Endpoint for gardenfield images")
@RestController
@RequestMapping("/api/v1")
public class GardenFieldImageController {

    private final Logger log = LoggerFactory.getLogger(GardenFieldImageController.class);
    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ImageStorageService imageStorageService;

    public GardenFieldImageController(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    @ApiOperation(value = "Upload a gardenfield image")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Successfully stored gardenfield image"),
        @ApiResponse(code = 400, message = "Request is not well formed, maybe missing field"),
        @ApiResponse(code = 401, message = "You are not authorized to store a gardenfield image"),
        @ApiResponse(code = 409, message = "Gardenfield does not exist")
    })
    @PostMapping("/gardenfields/{id}/uploadImage")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.USER + "\")")
    public ResponseEntity<UploadImageResponseDTO> uploadImage(
        @ApiParam(value = "Id of gardenfield", required = true) @PathVariable("id") Long id,
        @ApiParam(value = "Image for gardenfield", required = true) @RequestParam("image") MultipartFile image) {
        String fileName = imageStorageService.storeImage(id, image);

        UriComponentsBuilder gardenFieldPath = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/api/v1/gardenfields/")
            .path(id.toString());

        String fileDownloadUri = gardenFieldPath.cloneBuilder()
            .path("/downloadImage/")
            .path(fileName)
            .toUriString();

        String thumbnailDownloadUri = gardenFieldPath.cloneBuilder()
            .path("/downloadThumbnail/")
            .path(fileName)
            .toUriString();

        return ResponseEntity.created(URI.create(fileDownloadUri))
            .body(new UploadImageResponseDTO(fileName, fileDownloadUri,
                thumbnailDownloadUri, image.getContentType(), image.getSize())
            );
    }

    @ApiOperation(value = "Upload multiple gardenfield images")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Successfully stored gardenfield images"),
        @ApiResponse(code = 400, message = "Request is not well formed, maybe missing field"),
        @ApiResponse(code = 401, message = "You are not authorized to store a gardenfield images"),
        @ApiResponse(code = 409, message = "Gardenfield does not exist")
    })
    @PostMapping("/gardenfields/{id}/uploadMultipleImages")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.USER + "\")")
    public ResponseEntity<List<UploadImageResponseDTO>> uploadMultipleImages(
        @ApiParam(value = "Id of gardenfield", required = true) @PathVariable("id") Long id,
        @ApiParam(value = "Images for gardenfield", required = true) @RequestParam("files") MultipartFile[] images) {
        return new ResponseEntity<>(Arrays.stream(images)
            .map(image -> uploadImage(id, image))
            .collect(Collectors.toList())
            .stream()
            .map(HttpEntity::getBody)
            .collect(Collectors.toList()), HttpStatus.CREATED);
    }

    @ApiOperation(value = "Download a gardenfield image")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved gardenfield image"),
        @ApiResponse(code = 400, message = "Request is not well formed, maybe missing field"),
        @ApiResponse(code = 409, message = "Gardenfield does not exist")
    })
    @GetMapping(value = "/gardenfields/{id}/downloadImage/{imageName:.+}")
    @ResponseBody
    public ResponseEntity<Resource> downloadImage(
        @ApiParam(value = "Id of gardenfield", required = true) @PathVariable("id") Long id,
        @ApiParam(value = "Name of picture for gardenfield", required = true) @PathVariable("imageName") String imageName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = imageStorageService.loadImage(id, imageName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
    }

    @ApiOperation(value = "Download a gardenfield thumbnail")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved gardenfield thumbnail"),
        @ApiResponse(code = 400, message = "Request is not well formed, maybe missing fieldid"),
        @ApiResponse(code = 409, message = "Gardenfield does not exist")
    })
    @GetMapping(value = "/gardenfields/{id}/downloadThumbnail/{imageName:.+}")
    @ResponseBody
    public ResponseEntity<Resource> downloadThumbnail(
        @ApiParam(value = "Id of gardenfield", required = true) @PathVariable("id") Long id,
        @ApiParam(value = "Name of picture for gardenfield", required = true) @PathVariable("imageName") String imageName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = imageStorageService.loadThumbnail(id, imageName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
    }

    @ApiOperation(value = "returns all gardenfield imagefilenames")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved gardenfield image filenames"),
        @ApiResponse(code = 400, message = "Request is not well formed, maybe missing field"),
        @ApiResponse(code = 409, message = "Gardenfield does not exist")
    })
    @GetMapping("/gardenfields/{id}/downloadImages")
    public ResponseEntity<List<String>> downloadImages(
        @ApiParam(value = "Id of gardenfield", required = true) @PathVariable("id") Long id) {
        List<String> fileNames = imageStorageService.getImageFilenames(id);

        return ResponseEntity.ok()
            .body(fileNames);
    }

    @ApiOperation(value = "returns filename of cover image")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved gardenfield cover image filename"),
        @ApiResponse(code = 400, message = "Request is not well formed, maybe missing field"),
        @ApiResponse(code = 409, message = "Gardenfield does not exist")
    })
    @GetMapping("/gardenfields/{id}/coverImageName")
    public ResponseEntity<String> getCoverImage(
        @ApiParam(value = "Id of gardenfield", required = true) @PathVariable("id") Long id) {
        String fileName = imageStorageService.getCoverImage(id);
        return ResponseEntity.ok().body(fileName);
    }

    @ApiOperation(value = "Delete a gardenfield image")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Successfully deleted a gardenfield image"),
        @ApiResponse(code = 400, message = "Request is not well formed, maybe missing field"),
        @ApiResponse(code = 401, message = "You are not authorized to delete a gardenfield image"),
        @ApiResponse(code = 409, message = "Gardenfield does not exist")
    })
    @DeleteMapping("/gardenfields/{id}/{imageName:.+}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.USER + "\")")
    public ResponseEntity<Void> deleteImage(
        @ApiParam(value = "Id of gardenfield", required = true) @PathVariable("id") Long id,
        @ApiParam(value = "Name of picture for gardenfield", required = true) @PathVariable("imageName") String imageName) {
        log.debug("REST request to delete image {} for gardenfield: {}", imageName, id);
        imageStorageService.deleteImageAndThumbnail(id, imageName);
        return ResponseEntity.noContent().headers(
            HeaderUtil.createAlert(applicationName, "gardenFieldImageManagement.deleted", imageName)).build();
    }

}
