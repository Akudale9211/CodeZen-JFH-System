package com.marketyardbill.marketyardbill.service;

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow.Builder;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class GoogleDriveService {
    private static final String APPLICATION_NAME = "Spring Boot Drive Uploader";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList("https://www.googleapis.com/auth/drive.file");
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private Drive getDriveService() throws Exception {
        InputStream in = this.getClass().getResourceAsStream("/credentials.json");
        if (in == null) {
            throw new RuntimeException("credentials.json not found");
        } else {
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
            GoogleAuthorizationCodeFlow flow = (new Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientSecrets, SCOPES)).setDataStoreFactory(new FileDataStoreFactory(new File("tokens"))).setAccessType("offline").build();
            return (new com.google.api.services.drive.Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, (new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver())).authorize("user"))).setApplicationName("Spring Boot Drive Uploader").build();
        }
    }

    private String getOrCreateFolderId(String folderName, Drive driveService) throws Exception {
        FileList result = (FileList)driveService.files().list().setQ("mimeType='application/vnd.google-apps.folder' and name='" + folderName + "' and trashed=false").setSpaces("drive").setFields("files(id, name)").execute();
        if (result.getFiles().isEmpty()) {
            com.google.api.services.drive.model.File folderMetadata = new com.google.api.services.drive.model.File();
            folderMetadata.setName(folderName);
            folderMetadata.setMimeType("application/vnd.google-apps.folder");
            com.google.api.services.drive.model.File folder = (com.google.api.services.drive.model.File)driveService.files().create(folderMetadata).setFields("id").execute();
            return folder.getId();
        } else {
            return ((com.google.api.services.drive.model.File)result.getFiles().get(0)).getId();
        }
    }

    public String uploadInvoiceExcel() {
        try {
            String filePath = "C:/Users/Speed/JaradBackup/invoice_history.xlsx";
            Drive driveService = this.getDriveService();
            String folderId = this.getOrCreateFolderId("Invoice_H", driveService);
            String fileName = "Invoice_history_" + String.valueOf(LocalDateTime.now()) + ".xlsx";
            com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
            fileMetadata.setName(fileName);
            fileMetadata.setParents(Collections.singletonList(folderId));
            File file = new File(filePath);
            FileContent mediaContent = new FileContent("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", file);
            com.google.api.services.drive.model.File uploadedFile = (com.google.api.services.drive.model.File)driveService.files().create(fileMetadata, mediaContent).setFields("id").execute();
            return uploadedFile.getId();
        } catch (Exception var9) {
            var9.printStackTrace();
            return null;
        }
    }

    @Scheduled(
            cron = "0 0 8 */7 * *"
    )
    public void scheduledUpload() {
        System.out.println("Starting scheduled upload to Google Drive...");
        String fileId = this.uploadInvoiceExcel();
        if (fileId != null) {
            System.out.println("Scheduled upload successful. File ID: " + fileId);
        } else {
            System.err.println("Scheduled upload failed.");
        }

    }
}
