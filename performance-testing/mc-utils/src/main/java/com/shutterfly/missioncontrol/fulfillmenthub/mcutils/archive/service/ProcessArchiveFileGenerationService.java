package com.shutterfly.missioncontrol.fulfillmenthub.mcutils.archive.service;

import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.archive.dto.ArchiveFileDto;
import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.config.FtpConfiguration;
import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.config.SFTPService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
* Created by Shweta on 03-10-2017.
*/
@Slf4j
@Service
public class ProcessArchiveFileGenerationService {

private SFTPService sftpService;

@Autowired
public ProcessArchiveFileGenerationService(SFTPService sftpService) {
  this.sftpService=sftpService;
}

public void generateFilesForArchive(ArchiveFileDto archiveFileDto){
  String sourceFilePathAndName=archiveFileDto.getSourceFilePathAndName();
  String[] listOfStrings=sourceFilePathAndName.split("/");
  String sourceFileName=listOfStrings[listOfStrings.length-1];
  String destinationFilePath=archiveFileDto.getDestinationFilePath();
  int noOfFiles=archiveFileDto.getNoOfFiles();
  String localFileName=sourceFileName;
  //download file
  sftpService.downloadFile(sourceFilePathAndName,localFileName);
  String remoteFileName;
  //upload multiple files to sftp
  for (int i = 1; i <= noOfFiles; i++) {
    remoteFileName = FilenameUtils.removeExtension(localFileName)+"_"+ i + ".xml";
    sftpService.uploadFile(localFileName,destinationFilePath+remoteFileName);
  }

}
}



