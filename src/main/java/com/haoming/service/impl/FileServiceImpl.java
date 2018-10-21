package com.haoming.service.impl;

import com.google.common.collect.Lists;
import com.haoming.service.IFileService;
import com.haoming.util.FTPUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service("iFileService")
@Slf4j
public class FileServiceImpl implements IFileService {

    public String upload(MultipartFile file, String path) {
        String fileName = file.getOriginalFilename();
        // Extension
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);
        String uploadFileName = UUID.randomUUID().toString()+"."+fileExtensionName;
        log.info("Uploading file:{}, path:{}, new filename:{}", fileName, path, uploadFileName);

        File fileDir = new File(path);
        if (!fileDir.exists()) {
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile = new File(path, uploadFileName);

        try {
            file.transferTo(targetFile);

            // Upload the file to the FTP server.
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));

            // Delete the file on the local folder.
            targetFile.delete();
        } catch (IOException e) {
            log.error("Failed to upload the file.", e);
            return null;
        }

        return targetFile.getName();
    }
}
