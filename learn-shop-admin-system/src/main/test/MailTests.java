package test;

import com.billow.system.AdminSystemApp;
import com.billow.system.service.MailService;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {AdminSystemApp.class}) // 指定启动类
public class MailTests {

    @Autowired
    private JavaMailSender mailSender; // 自动注入的Bean

    @Value("${spring.mail.username}")
    private String Sender; // 读取配置文件中的参数

    @Autowired
    private MailService mailService;

    @Test
    public void sendSimpleMail() throws Exception {
        SimpleMailMessage message = new SimpleMailMessage();
        System.out.println("test hello Sender " + Sender);
        message.setFrom(Sender);
        message.setTo(Sender); // 自己给自己发送邮件
        message.setSubject("主题：简单邮件");
        message.setText("测试邮件内容");
        mailSender.send(message);
    }
}