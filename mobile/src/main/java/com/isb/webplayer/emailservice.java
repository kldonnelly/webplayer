package com.isb.webplayer;

import android.widget.Toast;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;



public class emailservice {
   String DomainName;
 //  String stringSenderEmail;
 private OnEventListener<String> mCallBack;

    public emailservice(String DomainName)
    {
        this.DomainName=DomainName;
    }

    public void send(String Sender,String subject,String body,OnEventListener<String> callback)
    {
        mCallBack=callback;
        String stringPasswordSenderEmail = "";

        String stringHost = "mail."+DomainName;
        Properties properties = System.getProperties();

        properties.put("mail.smtp.host", stringHost);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "false");
        properties.put("mail.smtp.localhost",DomainName);
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(Sender, stringPasswordSenderEmail);
            }
        });

        MimeMessage mimeMessage = new MimeMessage(session);


        try {
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(Sender));
            mimeMessage.setSubject(subject);
            mimeMessage.setText(body);


        } catch (MessagingException e) {
            e.printStackTrace();
        }

        Thread thread = new Thread(() -> {
            try {
                Transport.send(mimeMessage);
                mCallBack.onSuccess("Mail sent to "+Sender);
              //  Toast.makeText(getContext().getApplicationContext(), "Message sent", Toast.LENGTH_SHORT).show();
            } catch (MessagingException e) {
                e.printStackTrace();
                mCallBack.onFailure(e);
            }
        });
        thread.start();


    }
}
