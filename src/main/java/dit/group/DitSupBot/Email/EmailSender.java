package dit.group.DitSupBot.Email;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.angus.mail.smtp.SMTPSaslAuthenticator;
import org.jvnet.hk2.annotations.Service;
import org.springframework.beans.Mergeable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.w3c.dom.Text;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

@Slf4j
@Service
public class EmailSender {
    private static Session session;

    public boolean Send(String Text, String Objective, String To, String phone) throws MessagingException{
        Properties properties = new Properties();
        properties.put("mail.host", "smtp.yandex.ru");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.auth", "true");

        properties.put("mail.debug", "true");


        String Account = "noreply@adm-nk.ru";
        String Pass = "ndqpjmyilpgsyybw";

        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(Account, Pass);
            }
        };

        session = Session.getDefaultInstance(properties, auth);
        try{
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("noreply@adm-nk.ru"));
            message.setRecipients(
                    Message.RecipientType.TO, InternetAddress.parse(To));
            message.setSubject(Objective);
            String msg;
            if(phone.equals("0"))
            {
                msg = Text;
            }else {
                msg = Text + "\n" + phone;
            }

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(msg, "text/html; charset=utf-8");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);

            message.setContent(multipart);

            Transport.send(message);
            log.info("Email sent to adress " + To);
            return true;
        }catch (MailException e)
        {
            log.error("MailSendError: "+ e.getMessage());
            return false;
        }
    }

}
