package org.satya.whatsapp.service;

import org.apache.poi.ss.usermodel.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.satya.whatsapp.config.FileStorageProperties;
import org.satya.whatsapp.exception.FileStorageException;
import org.satya.whatsapp.exception.MyFileNotFoundException;
import org.satya.whatsapp.modal.FileInfo;
import org.satya.whatsapp.modal.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }
            System.out.println("file.getSize() = " + file.getSize());
//            if( file.getSize() >  ) {
//                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
//            }

//            fileName = new UUID(System.currentTimeMillis(), UUID.randomUUID().getLeastSignificantBits()) + fileName.substring(fileName.indexOf("."));

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        } catch (Exception e) {
            e.printStackTrace();
            throw new FileStorageException("something went wrong. Please try again!", e);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileName, ex);
        }
    }

    public List<FileInfo> loadUploadFiles() {
        List<FileInfo> filesList = new ArrayList<>();
        try {
            String folderPath = this.fileStorageLocation.toAbsolutePath().toString();

            File folder = new File(folderPath);

            if (!folder.isDirectory()) {
                throw new IllegalArgumentException("Provided path is not a directory");
            }

            File[] files = folder.listFiles();

            if (files == null || files.length == 0) {
                System.out.println("No files found in the folder");
                return filesList;
            }

            for (File file : files) {
                BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                long size = attr.size();
                Date createdDate = new Date(attr.creationTime().toMillis());
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate = dateFormat.format(createdDate);

                String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/uploads/")
                        .path(file.getName())
                        .toUriString();

                FileInfo fileInfo = new FileInfo(file.getName(), size, formattedDate, fileDownloadUri);
                filesList.add(fileInfo);
            }
        } catch (Exception e) {
            System.out.println("e = " + e);
        }
        return filesList;
    }

    public List<MessageDTO> readExcel(String srcFileName) throws IOException {
        List<MessageDTO> list = new ArrayList<>();
        try {
            String filePath = this.fileStorageLocation + "/" + srcFileName;
            System.out.println("fileName = " + srcFileName);
            System.out.println("filePath = " + filePath);
            FileInputStream fileInputStream = new FileInputStream(filePath);

            // Create a Workbook object
            Workbook workbook = WorkbookFactory.create(fileInputStream);

            // Get the first sheet
            Sheet sheet = workbook.getSheetAt(0);

            // Get the header row
            Row headerRow = sheet.getRow(0);
            int columnCount = headerRow.getLastCellNum();

            var toMobileNumber = "";
            var typeOfMsg = "";
            var message = "";
            var mediaUrl = "";
            var caption = "";
            var fileName = "";

            // Iterate through each row starting from the second row
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);

                toMobileNumber = "";
                typeOfMsg = "";
                message = "";
                mediaUrl = "";
                caption = "";
                fileName = "";

                if (row != null) {
                    JSONObject jsonObject = new JSONObject();
                    // Iterate through each cell in the row
                    for (int cellIndex = 0; cellIndex < columnCount; cellIndex++) {
                        Cell headerCell = headerRow.getCell(cellIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        // Extract header column name
                        String columnName = headerCell.getStringCellValue();
                        // Extract cell value and put it in the JSON object
//                        jsonObject.put(columnName, cell.toString());

                        if ( "tomobilenumber".equalsIgnoreCase(columnName.toLowerCase().trim()) ) {
                            toMobileNumber = cell!=null && !cell.toString().isEmpty()? cell.toString().trim():"";
                        }
                        else if ( "typeofmsg".equalsIgnoreCase(columnName.toLowerCase().trim()) ) {
                            typeOfMsg = cell!=null && !cell.toString().isEmpty()? cell.toString().trim():"";
                        }
                        else if ( "message".equalsIgnoreCase(columnName.toLowerCase().trim()) ) {
                            message = cell!=null && !cell.toString().isEmpty()? cell.toString().trim():"";
                        }
                        else if ( "mediaurl".equalsIgnoreCase(columnName.toLowerCase().trim()) ) {
                            mediaUrl = cell!=null && !cell.toString().isEmpty()? cell.toString().trim():"";
                        }
                        else if ( "caption".equalsIgnoreCase(columnName.toLowerCase().trim()) ) {
                            caption = cell!=null && !cell.toString().isEmpty()? cell.toString().trim():"";
                        }
                        else if ( "fileName".equalsIgnoreCase(columnName.toLowerCase().trim()) ) {
                            fileName = cell!=null && !cell.toString().isEmpty()? cell.toString().trim():"";
                        }
                    }
                    // Add the JSON object to the array
                    list.add(new MessageDTO(toMobileNumber,typeOfMsg,message,mediaUrl,caption,fileName));
                }
            }

            // Close the workbook
            workbook.close();
        }catch (Exception e){
            System.out.println("e = " + e);
        }
        return list;
    }
}
