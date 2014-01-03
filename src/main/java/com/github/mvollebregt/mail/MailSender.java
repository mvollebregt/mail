package com.github.mvollebregt.mail;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Demo for sending an email with a PDF attachment.
 */
public class MailSender {

    public static void main(String [] args) throws IOException, MessagingException {
        MailSender mailSender = new MailSender();
        InputStream someInputStream = mailSender.getResourceAsStream("some_attachment.pdf");
        mailSender.sendMailWithAttachment("receiver@email.address", "Test message from Michel Vollebregt", "attachment.pdf", someInputStream);
        someInputStream.close();
    }

    /**
     * Sends a default mail with a specified subject and a specified attachment to a specified user.
     * The sender, message text and mail server settings are read from mail.properties.
     *
     * @param to            the email address the mail should be sent to
     * @param subject       the subject
     * @param fileName      the file name for the attachment that should be used in the email
     * @param attachment    an input stream that contains the attachment
     * @throws MessagingException  if something went wrong with sending the message
     * @throws IOException         if something went wrong with handling the attachment
     */
    public void sendMailWithAttachment(String to, String subject, String fileName, InputStream attachment) throws
            MessagingException, IOException {
        Properties props = readProperties();
        Session session = createSession(props);
        String from = props.getProperty("mail.message.from");
        String text = props.getProperty("mail.message.text");
        Message message = createMessage(session, from, subject, to);
        setMessageContent(message, text, fileName, attachment);
        Transport.send(message);
    }

    private Properties readProperties() throws IOException {
        Properties props = new Properties();
        props.load(getResourceAsStream("mail.properties"));
        return props;
    }

    private Session createSession(final Properties props) {
        Authenticator authenticator = null;
        if (Boolean.parseBoolean(props.getProperty("mail.smtp.auth"))) {
            authenticator = new Authenticator() {
                private PasswordAuthentication pa = new PasswordAuthentication(
                        props.getProperty("mail.auth.username"), props.getProperty("mail.auth.password"));
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    return pa;
                }
            };
        }

        return Session.getDefaultInstance(props, authenticator);
    }

    private Message createMessage(Session session, String from, String subject, String to) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject);
        return message;
    }

    private void setMessageContent(Message message, String bodyText, String fileName, InputStream inputStream) throws
            MessagingException, IOException {
        Multipart content = new MimeMultipart();
        BodyPart textPart = new MimeBodyPart();
        textPart.setText(bodyText);
        content.addBodyPart(textPart);
        BodyPart attachment = new MimeBodyPart();
        ByteArrayDataSource dataSource = new ByteArrayDataSource(inputStream, "application/pdf");
        attachment.setDataHandler(new DataHandler(dataSource));
        attachment.setFileName(fileName);
        content.addBodyPart(attachment);
        message.setContent(content);
    }

    private InputStream getResourceAsStream(String fileName) {
        return MailSender.class.getClassLoader().getResourceAsStream(fileName);
    }


}

