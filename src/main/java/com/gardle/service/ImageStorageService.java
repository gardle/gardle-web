package com.gardle.service;

import com.gardle.config.ImageStorageProperties;
import com.gardle.domain.GardenField;
import com.gardle.repository.GardenFieldRepository;
import com.gardle.service.exception.*;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ImageStorageService {

    private static final Logger log = LoggerFactory.getLogger(ImageStorageService.class);

    private final Path fileStorageLocation;

    private final double thumbnailQuality = 0.65;
    private final int thumbnailMaxHeight = 384;
    private final int thumbnailMaxWidth = 384;
    private final SecurityHelperService securityHelperService;
    private final GardenFieldRepository gardenFieldRepository;
    private final String thumbnailFolderName;

    public ImageStorageService(ImageStorageProperties imageStorageProperties, SecurityHelperService securityHelperService,
                               GardenFieldRepository gardenFieldRepository) {
        this.securityHelperService = securityHelperService;
        this.gardenFieldRepository = gardenFieldRepository;
        this.fileStorageLocation = Paths.get(imageStorageProperties.getImageDir())
            .toAbsolutePath().normalize();
        this.thumbnailFolderName = ImageStorageProperties.THUMBNAIL_FOLDER_NAME;
    }

    public String storeImage(Long gardenFieldId, MultipartFile file) {
        securityHelperService.checkAuthorityByGardenFieldId(gardenFieldId);
        checkImageValidity(file.getContentType());

        if (file.getSize() > 2000000) {
            throw new ImageStorageServiceException("Image is too large. Only images with less than 2MB are allowed");
        }
        //create folders
        Path gardenFieldPath = fileStorageLocation.resolve(gardenFieldId.toString());
        Path thumbnailPath = gardenFieldPath.resolve(this.thumbnailFolderName);
        try {
            Files.createDirectories(gardenFieldPath);
            Files.createDirectories(thumbnailPath);
        } catch (IOException ex) {
            throw new ImageStorageServiceException("Could not create the directory where the uploaded files will be stored.", ex);
        }

        // create random uuid, which is used as the new image filename
        UUID uuid = UUID.randomUUID();
        String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (fileExtension == null || fileExtension.isEmpty()) {
            fileExtension = "jpg";
        }
        String fileName = String.format("%s.%s", uuid, fileExtension);

        try {
            //create thumbnail
            File thumbnail = thumbnailPath.resolve(fileName).toFile();
            Thumbnails.of(file.getInputStream())
                .outputQuality(thumbnailQuality)
                .size(thumbnailMaxWidth, thumbnailMaxHeight)
                .toFile(thumbnail);

            // Copy file to the target location (Replacing existing file with the same name)
            Path imageTargetPath = gardenFieldPath.resolve(fileName);
            Files.copy(file.getInputStream(), imageTargetPath, StandardCopyOption.REPLACE_EXISTING);

            updateCoverImage(gardenFieldId);

            return fileName;
        } catch (IOException ex) {
            throw new ImageStorageServiceException("Could not store image " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadImage(Long gardenFieldId, String imageName) {
        return getResource(getImagePath(gardenFieldId, imageName));
    }

    public Resource loadThumbnail(Long gardenFieldId, String imageName) {
        return getResource(getThumbnailPath(gardenFieldId, imageName));
    }

    public List<String> getImageFilenames(Long gardenFieldId) {
        gardenFieldRepository.findById(gardenFieldId).orElseThrow(GardenFieldNotFoundServiceException::new);
        try (Stream<Path> walk = Files.walk(fileStorageLocation.resolve(gardenFieldId.toString()), 1)) {
            return walk.filter(Files::isRegularFile)
                .map(path -> path.getFileName().toString()).collect(Collectors.toList());
        } catch (NoSuchFileException ex) {
            return new ArrayList<>();
        } catch (IOException e) {
            throw new ImageStorageServiceException("Could not get image filenames", e);
        }
    }

    /**
     * Deletes the all images and thumbnails of one gardenfield
     *
     * @param gardenFieldId id of the gardenfield
     * @throws ImageStorageServiceException when at least one deletion failed
     */
    public void deleteImages(Long gardenFieldId) {
        AtomicInteger exceptionCounter = new AtomicInteger();
        getImageFilenames(gardenFieldId).forEach(image -> {
            try {
                this.deleteImageAndThumbnail(gardenFieldId, image);
            } catch (ImageStorageServiceException ex) {
                exceptionCounter.incrementAndGet();
            }
        });
        if (exceptionCounter.get() > 0) {
            throw new ImageStorageServiceDeletionException(String.format("Could not delete all files. %d failed", exceptionCounter.get()));
        }
    }

    /**
     * Deletes the image and its thumbnail
     *
     * @param gardenFieldId id of the gardenfield
     * @param imageName     name of the image
     * @throws ImageStorageServiceException when deleting failed
     */
    public void deleteImageAndThumbnail(Long gardenFieldId, String imageName) {
        this.securityHelperService.checkAuthorityByGardenFieldId(gardenFieldId);
        try {
            Files.deleteIfExists(getImagePath(gardenFieldId, imageName));
            Files.deleteIfExists(getThumbnailPath(gardenFieldId, imageName));
        } catch (IOException ex) {
            throw new ImageStorageServiceDeletionException(String.format("Could not delete image with name %s", imageName), ex);
        }

        updateCoverImage(gardenFieldId);
    }

    private Resource getResource(Path resourcePath) {
        try {
            Resource resource = new UrlResource(resourcePath.toUri());
            if (resource.exists()) {
                return resource;
            }
        } catch (MalformedURLException ex) {
            throw new ImageNotFoundServiceException(String.format("Invalid resource path %s", resourcePath), ex);
        }
        throw new ImageNotFoundServiceException(String.format("Could not find resource at %s", resourcePath));
    }

    private void checkImageValidity(String contentType) {
        if (contentType == null || !(contentType.equals("image/pjpeg") || contentType.equals("image/jpeg") || contentType.equals("image/jpg")
            || contentType.equals("image/png") || contentType.equals("image/gif") || contentType.equals("image/bmp")
            || contentType.equals("image/x-png") || contentType.equals("image/x-icon"))) {
            throw new NotAnImageServiceException("The file-format must one of the following: pjpeg, jpeg, jpg, " +
                "png, gif, bmp, x-png, x-icon");
        }
    }

    private Path getImagePath(Long gardenFieldId, String imageName) {
        return fileStorageLocation.resolve(gardenFieldId.toString())
            .resolve(imageName)
            .normalize();
    }

    private Path getThumbnailPath(Long gardenFieldId, String imageName) {
        return fileStorageLocation.resolve(gardenFieldId.toString())
            .resolve(this.thumbnailFolderName)
            .resolve(imageName)
            .normalize();
    }

    private String chooseCoverImage(Long gardenFieldId) {
        try (Stream<Path> walk = Files.walk(fileStorageLocation.resolve(gardenFieldId.toString()), 1)) {
            return walk.filter(Files::isRegularFile).findAny().map(path -> path.getFileName().toString()).get();
        } catch (NoSuchElementException ex) {
            throw new CoverNotFoundServiceException();
        } catch (IOException e) {
            throw new ImageStorageServiceException("Could not get image filenames", e);
        }
    }

    private void updateCoverImage(Long gardenFieldId) {
        GardenField gardenField = gardenFieldRepository.findById(gardenFieldId)
            .orElseThrow(GardenFieldNotFoundServiceException::new);

        String coverImage;
        try {
            coverImage = chooseCoverImage(gardenFieldId);
        }catch (CoverNotFoundServiceException ex){
            coverImage = null;
        }
        gardenField.setCoverImage(coverImage);
        gardenFieldRepository.save(gardenField);
    }

    public String getCoverImage(Long gardenFieldId) {
        GardenField gardenField = gardenFieldRepository.findById(gardenFieldId)
            .orElseThrow(GardenFieldNotFoundServiceException::new);

        String coverImage = gardenField.getCoverImage();
        if (coverImage == null) {
            coverImage = chooseCoverImage(gardenFieldId);
            gardenField.setCoverImage(coverImage);
            gardenFieldRepository.save(gardenField);
        }

        return coverImage;
    }
}
