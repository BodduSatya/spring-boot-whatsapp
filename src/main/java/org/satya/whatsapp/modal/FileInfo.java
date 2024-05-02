package org.satya.whatsapp.modal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileInfo {

    private String name;
    private long size;
    private String createdDate;
    private String filePath;

}
