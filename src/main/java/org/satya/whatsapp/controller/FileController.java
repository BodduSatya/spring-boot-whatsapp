package org.satya.whatsapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.satya.whatsapp.modal.FileInfo;
import org.satya.whatsapp.modal.UploadFileResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.satya.whatsapp.service.FileStorageService;

@RestController
@ControllerAdvice
@CrossOrigin
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/uploadFile")
    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) {
        return getUploadFileResponse(file);
    }

    @PostMapping("/uploadMultipleFiles")
    public List<UploadFileResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        return Arrays.stream(files)
                .map(this::getUploadFileResponse)
                .collect(Collectors.toList());
    }

    private UploadFileResponse getUploadFileResponse(MultipartFile file) {
        try {
            String fileName = fileStorageService.storeFile(file);

            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/downloadFile/")
                    .path(fileName)
                    .toUriString();

            return new UploadFileResponse(fileName, fileDownloadUri,
                    file.getContentType(), file.getSize(), "File Uploaded Successfully.");
        }catch (Exception e){
            return new UploadFileResponse("", "",
                    "", 0, e.getMessage());
        }
    }


    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @ExceptionHandler({SizeLimitExceededException.class, MaxUploadSizeExceededException.class})
    public UploadFileResponse handleException(RedirectAttributes ra){
        System.out.println("max file size "+this.maxFileSize+" exceeded!" );
        return new UploadFileResponse("", "",
                "", 0, "max file size "+this.maxFileSize+" exceeded!");
    }

    @GetMapping("/loadUploadFiles")
    public List<FileInfo> loadUploadFiles() {
        return fileStorageService.loadUploadFiles();
    }

}
