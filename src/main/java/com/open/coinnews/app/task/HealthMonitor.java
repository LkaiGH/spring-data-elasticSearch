package com.open.coinnews.app.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class HealthMonitor {

    private static final String HEALTHPATH1="http://39.105.112.166:9200/_cluster/health";
    private static final String HEALTHPATH2="http://39.105.112.166:9400/_cluster/health";
    private static final String HEALTHPATH3="http://39.105.112.166:9500/_cluster/health";

    private static final String GREEN = "green";
    private static final String YELLOY = "yellow";
    private static final String RED = "red";


    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JavaMailSender javaMailSender;

    @Scheduled(fixedDelay = 5000)
    public void healthCheck(){

        HttpClient httpClient = HttpClients.createDefault();

        HttpGet get = new HttpGet(HEALTHPATH1);

        try {
            HttpResponse response = httpClient.execute(get);
            if(response.getStatusLine().getStatusCode()!= HttpServletResponse.SC_OK){
                System.out.println("节点异常");
            }else{
               String body = EntityUtils.toString(response.getEntity(),"Utf-8");
               JsonNode result = objectMapper.readTree(body);
               String status = result.get("status").asText();

               String message="";
               Boolean issend = false;
               switch (status){
                   case GREEN:
                       message = "节点正常-----";
                       break;
                   case YELLOY:
                       message ="检查节点-----";
                       break;
                   case RED:
                       message ="节点异常------";
                       break;
                   default:
                       message ="异常异常！------";
                       break;
               }
               if(!issend){
                   //可以获取到es节点信息 判断哪个节点出错

                   sendMessage(message);
               }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }



    private  void sendMessage(String message){

        SimpleMailMessage messages = new SimpleMailMessage();
        messages.setFrom("发送人@163.com");
        messages.setTo("收件人@163.com");
        messages.setSubject("测试--------");
        messages.setText("测试--------");
        javaMailSender.send(messages);
    }


}
