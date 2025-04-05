package com.example.kursa;

import android.os.AsyncTask;
import android.util.Log;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class SendMailTask extends AsyncTask<Void, Void, Boolean> {
    private final String senderEmail;
    private final String senderPassword;
    private final String recipientEmail;
    private final String subject;
    private final String body;

    public SendMailTask(String senderEmail, String senderPassword, String recipientEmail, String subject, String body) {
        this.senderEmail = senderEmail;
        this.senderPassword = senderPassword;
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.body = body;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);

            // Устанавливаем тип содержимого как HTML
            message.setContent(body, "text/html; charset=utf-8");

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            Log.e("SendMailTask", "Ошибка при отправке письма: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            Log.d("SendMailTask", "Письмо успешно отправлено!");
        } else {
            Log.e("SendMailTask", "Не удалось отправить письмо.");
        }
    }
}