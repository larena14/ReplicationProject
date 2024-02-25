package it.larena.nodebackend.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Service {

    @Value("${bucket.name}")
    private String bucketName;

    private final AmazonS3 s3client;

    public boolean putObject(String key, File file) {
        try {
            s3client.putObject(bucketName, key, file);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    public URL getObjectURL(String key) {
        try {
            return s3client.getUrl(bucketName, key);
        }
        catch (Exception e){
            return null;
        }
    }

    public List<String> listObjects() {
        try {
            ObjectListing objectListing = s3client.listObjects(bucketName);
            List<String> objectsKeys = new LinkedList<>();
            for(S3ObjectSummary os : objectListing.getObjectSummaries()) {
                objectsKeys.add(os.getKey());
            }
            return objectsKeys;
        }
        catch (Exception e){
            return null;
        }
    }

    public byte[] getObject(String objectKey) {
        try {
            S3Object s3object = s3client.getObject(bucketName, objectKey);
            S3ObjectInputStream inputStream = s3object.getObjectContent();
            return inputStream.readAllBytes();
        }
        catch (Exception e){
            return null;
        }
    }

    public boolean deleteObject(String objectKey) {
        try {
            s3client.deleteObject(bucketName, objectKey);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    public List<String> searchObject(String prefix) {
        try {
            ObjectListing objectListing = s3client.listObjects(bucketName);
            List<String> objectsKeys = new LinkedList<>();
            for(S3ObjectSummary os : objectListing.getObjectSummaries()) {
                if(os.getKey().contains(prefix))
                    objectsKeys.add(os.getKey());
            }
            return objectsKeys;
        }
        catch (Exception e){
            return null;
        }
    }

}

