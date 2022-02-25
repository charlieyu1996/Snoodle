package cloudcode.guestbook.backend;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.IOException;
import java.io.InputStream;

public class CloudStorage {


    public void uploadObject(
        String projectId, String bucketName, String objectName, byte[] filePath) throws IOException {
      // The ID of your GCP project
      // String projectId = "your-project-id";
  
      // The ID of your GCS bucket
      // String bucketName = "your-unique-bucket-name";
  
      // The ID of your GCS object
      // String objectName = "your-object-name";
  
      // The path to your file to upload
      // String filePath = "path/to/your/file"
  
      Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
      BlobId blobId = BlobId.of(bucketName, objectName);
      BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
      storage.create(blobInfo, filePath);
  
      System.out.println(
          "File " + filePath + " uploaded to bucket " + bucketName + " as " + objectName);
    }



    public void uploadObjectFromMemory(
        String projectId, String bucketName, String objectName, InputStream audio) throws IOException {
      // The ID of your GCP project
      // String projectId = "your-project-id";
  
      // The ID of your GCS bucket
      // String bucketName = "your-unique-bucket-name";
  
      // The ID of your GCS object
      // String objectName = "your-object-name";
  
      // The string of contents you wish to upload
      // String contents = "Hello world!";
  
      Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
      BlobId blobId = BlobId.of(bucketName, objectName);
      BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
    //   storage.createFrom(blobInfo, new ByteArrayInputStream(audio));
      storage.createFrom(blobInfo, audio);
  
      System.out.println(
          "Object "
              + objectName
              + " uploaded to bucket "
              + bucketName
              + " with contents ");
    }

//     public static void main(String[] args) {
//     try {
//         uploadObjectFromMemory("charliyu-demo", "snoodle-voice", "testobject", "hello world");
//     } catch (IOException e) {
//         // TODO Auto-generated catch block
//         e.printStackTrace();
//     }
//   }
}