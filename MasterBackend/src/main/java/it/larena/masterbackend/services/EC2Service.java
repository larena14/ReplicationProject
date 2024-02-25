package it.larena.masterbackend.services;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.util.EC2MetadataUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class EC2Service {

    @Value("${vpc.id}")
    private String vpcId;

    private final AmazonEC2 ec2client;

    private final int RUNNING = 16;

    public Map<String, String[]> getNodes(){
        Map<String, String[]> ips = new HashMap<>();
        String localPrivateIP;
        DescribeInstancesResult response;
        try {
            localPrivateIP = EC2MetadataUtils.getPrivateIpAddress();
            response = ec2client.describeInstances();
        }
        catch (Exception e){
            return ips;
        }
        for (Reservation reservation : response.getReservations())
            for (Instance instance : reservation.getInstances()) {
                try{
                    if (instance.getState().getCode() == RUNNING && instance.getVpcId().equals(vpcId) && !instance.getPrivateIpAddress().equals(localPrivateIP)) {
                        ips.put(instance.getTags().get(0).getValue(), new String[]{instance.getInstanceId(), instance.getPlacement().getAvailabilityZone()});
                    }
                }
                catch (Exception ignored){}
            }
        return ips;
    }

    public String getIP(String id){
        try {
            DescribeInstancesRequest request = new DescribeInstancesRequest().withInstanceIds(id);
            DescribeInstancesResult response = ec2client.describeInstances(request);
            return response.getReservations().get(0).getInstances().get(0).getPrivateIpAddress();
        }
        catch(Exception e) {
            return null;
        }
    }

}

