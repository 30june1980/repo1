package com.shutterfly.missioncontrolservices.fulfillmenthub.mcutils.archive.service;

import com.shutterfly.missioncontrolservices.fulfillmenthub.mcutils.config.SFTPService;
import com.shutterfly.missioncontrolservices.fulfillmenthub.mcutils.dto.FileDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
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
@Async
public void generateFilesForArchive(FileDto fileDto){
  String sourceFilePathAndName= fileDto.getSourceFilePathAndName();
  String[] listOfStrings=sourceFilePathAndName.split("/");
  String sourceFileName=listOfStrings[listOfStrings.length-1];
  String destinationFilePath= fileDto.getDestinationFilePath();
  int noOfFiles= fileDto.getNoOfCopies();
  String localFileName=sourceFileName;
  //download file
  sftpService.downloadFile(sourceFilePathAndName,localFileName);
  String remoteFileName;
  //upload multiple files to sftp
  for (int i = 1; i <= noOfFiles; i++) {
    remoteFileName = FilenameUtils.removeExtension(localFileName)+"_"+ i + "."+FilenameUtils.getExtension(localFileName);
    sftpService.uploadFile(localFileName,destinationFilePath+remoteFileName);
  }
  //delete local file
  sftpService.deleteLocalFile(localFileName);
}
}



