package it.larena.masterbackend.controllers.rest;

import it.larena.masterbackend.controllers.requests.PutObjectRequest;
import it.larena.masterbackend.services.EC2Service;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.javatuples.Pair;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;


@RestController
@Validated
@RequestMapping("/nodes")
public class NodesController {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final Gson gson = new Gson();

    private final EC2Service ec2Service;

    public NodesController(EC2Service ec2Service) {
        this.ec2Service = ec2Service;
    }

    @GetMapping(value = "/test/all")
    public ResponseEntity<List<Map<String, String>>> test() {
        Map<String, String[]> ips = ec2Service.getNodes();
        List<Map<String, String>> responses = new LinkedList<>();
        for(String tag : ips.keySet()){
            Map<String, String> map = new HashMap<>();
            map.put("tag", tag);
            String[] info = ips.get(tag);
            map.put("id", info[0]);
            map.put("zone", info[1]);
            responses.add(map);
        }
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping(value = "/test")
    public ResponseEntity<String> testNode(@RequestParam String instanceId){
        String ip = ec2Service.getIP(instanceId);
        if(ip != null){
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://"+ip+":8090/nodes/test"))
                    .GET()
                    .build();
            HttpResponse<String> response;
            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if(response.statusCode() != 200)
                    return new ResponseEntity<>("Error.", HttpStatus.INTERNAL_SERVER_ERROR);
                return new ResponseEntity<>("Node is online.", HttpStatus.OK);
            } catch (IOException | InterruptedException e) {
                return new ResponseEntity<>("Error.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        else{
            return new ResponseEntity<>("Error.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/objects/all")
    public ResponseEntity<List<String>> listObjects(@RequestParam String instanceId){
        String ip = ec2Service.getIP(instanceId);
        if(ip != null){
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://"+ip+":8090/nodes/objects/all"))
                    .GET()
                    .build();
            HttpResponse<String> response;
            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if(response.statusCode() != 200)
                    return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
                List<String> l = gson.fromJson(response.body(), List.class);
                return new ResponseEntity<>(l, HttpStatus.OK);
            } catch (IOException | InterruptedException e) {
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        else{
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping(value = "/objects/search")
    public ResponseEntity<List<String>> searchObjects(@RequestParam String instanceId, @RequestParam String prefix){
        String ip = ec2Service.getIP(instanceId);
        if(ip != null){
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://"+ip+":8090/nodes/objects/search?prefix="+prefix))
                    .GET()
                    .build();
            HttpResponse<String> response;
            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if(response.statusCode() != 200)
                    return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
                List<String> l = gson.fromJson(response.body(), List.class);
                return new ResponseEntity<>(l, HttpStatus.OK);
            } catch (IOException | InterruptedException e) {
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        else {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping(value = "/objects")
    public ResponseEntity<String> putObject(@RequestBody @Validated PutObjectRequest putObjectRequest){
        String ip = ec2Service.getIP(putObjectRequest.getInstanceId());
        if(ip != null){
            String key = putObjectRequest.getKey();
            putObjectRequest.setKey(key.replaceAll("\\s", "-"));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://"+ip+":8090/nodes/objects"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(putObjectRequest)))
                    .build();
            HttpResponse<String> response;
            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if(response.statusCode() != 200)
                    return new ResponseEntity<>("Error.", HttpStatus.INTERNAL_SERVER_ERROR);
                return new ResponseEntity<>(response.body(), HttpStatus.OK);
            } catch (IOException | InterruptedException e) {
                return new ResponseEntity<>("Error.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        else{
            return new ResponseEntity<>("Error.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping(value = "/objects/url")
    public ResponseEntity<String> getObjectURL(@RequestParam String instanceId, @RequestParam String key) {
        String ip = ec2Service.getIP(instanceId);
        System.out.println(key);
        if(ip != null){
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://"+ip+":8090/nodes/objects/url?key="+key))
                    .GET()
                    .build();
            HttpResponse<String> response;
            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if(response.statusCode() != 200)
                    return new ResponseEntity<>("Error.", HttpStatus.INTERNAL_SERVER_ERROR);
                return new ResponseEntity<>(response.body(), HttpStatus.OK);
            } catch (IOException | InterruptedException e) {
                return new ResponseEntity<>("Error.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        else{
            return new ResponseEntity<>("Error.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/objects")
    public ResponseEntity<byte[]> getObject(@RequestParam String instanceId, @RequestParam String key){
        String ip = ec2Service.getIP(instanceId);
        if(ip != null){
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://"+ip+":8090/nodes/objects?key="+key))
                    .GET()
                    .build();
            HttpResponse<byte[]> response;
            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
                if(response.statusCode() != 200)
                    return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
                final HttpHeaders httpHeaders= new HttpHeaders();
                httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                return new ResponseEntity<>(response.body(), httpHeaders, HttpStatus.OK);
            } catch (IOException | InterruptedException e) {
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        else{
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @DeleteMapping(value = "/objects")
    public ResponseEntity<String> deleteObject(@RequestParam String instanceId, @RequestParam String key){
        String ip = ec2Service.getIP(instanceId);
        if(ip != null){
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://"+ip+":8090/nodes/objects?key="+key))
                    .DELETE()
                    .build();
            HttpResponse<String> response;
            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if(response.statusCode() != 200)
                    return new ResponseEntity<>("Error.", HttpStatus.INTERNAL_SERVER_ERROR);
                return new ResponseEntity<>(response.body(), HttpStatus.OK);
            } catch (IOException | InterruptedException e) {
                return new ResponseEntity<>("Error.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        else {
            return new ResponseEntity<>("Error.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}
