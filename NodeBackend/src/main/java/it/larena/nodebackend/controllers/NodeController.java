package it.larena.nodebackend.controllers;

import it.larena.nodebackend.controllers.requests.PutObjectRequest;
import it.larena.nodebackend.services.S3Service;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@Validated
@RequestMapping("/nodes")
public class NodeController {

    private final S3Service s3Service;

    public NodeController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @GetMapping(value = "/test")
    public ResponseEntity<String> test() {
        return new ResponseEntity<>("Online.", HttpStatus.OK);
    }

    @GetMapping(value = "/objects/all")
    public ResponseEntity<List<String>> listObjects(){
        List<String> l = s3Service.listObjects();
        if(l != null){
            return new ResponseEntity<>(l, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(l, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/objects/search")
    public ResponseEntity<List<String>> searchObjects(@RequestParam String prefix){
        List<String> l = s3Service.searchObject(prefix);
        if(l != null){
            return new ResponseEntity<>(l, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(l, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/objects")
    public ResponseEntity<String> putObject(@RequestBody @Validated PutObjectRequest putObjectRequest){
        File file = null;
        try {
            String key = putObjectRequest.getKey();
            byte[] fileBytes = putObjectRequest.getFileBytes();
            Path path = Files.createTempFile("file", ".temp");
            file = path.toFile();
            FileUtils.writeByteArrayToFile(file, fileBytes);
            boolean done = s3Service.putObject(key, file);
            if(done)
                return new ResponseEntity<>("File uploaded.", HttpStatus.OK);
            return new ResponseEntity<>("Error.", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>("Error.", HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            if (file != null && file.exists())
                file.delete();
        }
    }

    @GetMapping(value = "/objects/url")
    public ResponseEntity<String> getObjectURL(@RequestParam String key) {
        URL url = s3Service.getObjectURL(key);
        if (url != null)
            return new ResponseEntity<>(url.toString(), HttpStatus.OK);
        else
            return new ResponseEntity<>("Error.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping(value = "/objects")
    public ResponseEntity<byte[]> getObject(@RequestParam String key){
        byte[] object = s3Service.getObject(key);
        if(object != null)
            return new ResponseEntity<>(object, HttpStatus.OK);
        else
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DeleteMapping(value = "/objects")
    public ResponseEntity<String> deleteObject(@RequestParam String key){
        boolean done = s3Service.deleteObject(key);
        if(done)
            return new ResponseEntity<>("Deleted.", HttpStatus.OK);
        else
            return new ResponseEntity<>("Error.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
