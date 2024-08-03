package org.satya.whatsapp.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import jakarta.mail.internet.MimeMessage;
import org.satya.whatsapp.modal.MailRequest;
import org.satya.whatsapp.modal.MailResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender sender;

    @Autowired
    private Configuration config;

    public MailResponse sendEmail(MailRequest mailReq, Map<String,Object> modal){
        MailResponse response = null;
        MimeMessage mimeMessage = sender.createMimeMessage();
        try{
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            helper.addAttachment("logo.png",new ClassPathResource("/static/images/logo.png"));

            Template t = config.getTemplate("email-template.ftl");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(t,modal);

            helper.setText(html,true);
            helper.setTo(mailReq.getTo());
            helper.setSubject(mailReq.getSubject());
            sender.send(mimeMessage);

            response = new MailResponse("mail send to :"+mailReq.getTo(),Boolean.TRUE);
        }catch (Exception e){
            System.out.println("e = " + e);
            response = new MailResponse("mail sending failed :"+e.getMessage(),Boolean.FALSE);
        }
        return response;

    }


}
