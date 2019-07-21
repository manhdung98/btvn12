package com.topica.automarking;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.log4j.Logger;

public class Handler {
	private String username;
	private String password;

	public Handler(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}
	static final Logger logger = Logger.getLogger(Handler.class);
	public static final String HOST = "imap.gmail.com";
	public static final String PORT = "995";
	public static final String ENABLE = "true";
	public static final String PROTOCOL = "imaps";
	public static final String TYPE_FOLDER = "INBOX";
	public static final String SUBJECT_REGEX = "ITLAB-HOMEWORK";
	public static final String FILE_EXTENSION = ".zip";
	public static final String LOCAL_FOLDER = "tmp//raw//";
	public static final String FOLDER_EXTRACT = "tmp//extracted//";
	public static final String SMTP_HOST = "smtp.gmail.com";
	public static final String SMTP_PORT = "587";
	public static final String APP_NAME = "com.topica.automarking.CheckingEmail";
	public static final String SUBJECT = "Final Exam Grade";
	public static final String YOUR_GRADE = "Your Grade is: ";
	public static final String MARKING = "Marking ";
	public static final String SEND_GRADE = "Sending Grade to: ";
	public static final String FINISH_SEND = "Fnished sending grade to ";

	public Store conn() throws MessagingException {
		Properties properties = new Properties();

		properties.put("mail.imap.host", HOST);
		properties.put("mail.imap.port", PORT);
		properties.put("mail.imap.starttls.enable", ENABLE);
		Session emailSession = Session.getDefaultInstance(properties);
		Store store = emailSession.getStore(PROTOCOL);

		store.connect(HOST, this.username, this.password);
		return store;
	}

	public void readAndDown(Store store) throws MessagingException, IOException {
		Folder emailFolder = store.getFolder(TYPE_FOLDER);
		emailFolder.open(Folder.READ_ONLY);

		Message[] messages = emailFolder.getMessages();
		for (int i = 0, n = messages.length; i < n; i++) {
			Message message = messages[i];
			if (SUBJECT_REGEX.equals(message.getSubject())) {
				Multipart mp = (Multipart) message.getContent();
				int count = mp.getCount();
				for (int j = 0; j < count; j++) {
					BodyPart bodyPart = mp.getBodyPart(j);
					if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
						continue;
					}
					String fileName = bodyPart.getFileName();
					if (!FILE_EXTENSION.equals(fileName.substring(fileName.lastIndexOf('.')))) {
						continue;
					}
					File f = new File(LOCAL_FOLDER + bodyPart.getFileName());
					InputStream is = bodyPart.getInputStream();
					FileOutputStream fos = new FileOutputStream(f);
					byte[] buf = new byte[4096];
					int bytesRead;
					while ((bytesRead = is.read(buf)) != -1) {
						fos.write(buf, 0, bytesRead);
					}
					fos.close();

					String SOURCE_ZIPDIR = LOCAL_FOLDER + bodyPart.getFileName();
					String DESTINATION_DIR = FOLDER_EXTRACT;
					UnzipFile.extract(SOURCE_ZIPDIR, DESTINATION_DIR);
				}
			}
		}
		logger.info("OK");
	}

	public void grade(String studentStorageDirName) {
		File baseDir = new File(studentStorageDirName);
		String[] studentDirNames = baseDir.list((file, fileName) -> file.isDirectory());

		for (String studentAddr : studentDirNames) {
			String classpath = new StringBuilder().append(studentStorageDirName).append(File.separator)
					.append(studentAddr).toString();

			String answerFilePath = new StringBuilder().append(classpath).append(File.separator).append("test_pkg")
					.append(File.separator).append("Main.java").toString();

			logger.info(MARKING + studentAddr);
			float grade = CheckDate.getGrade(answerFilePath, classpath, APP_NAME);
			logger.info(SEND_GRADE + studentAddr);
			sendResult(studentAddr, grade);
			logger.info(FINISH_SEND + studentAddr);
		}
	}

	private void sendResult(String studentAddr, float grade) {
		Properties props = System.getProperties();
		props.put("mail.smtp.host", SMTP_HOST);
		props.put("mail.smtp.port", SMTP_PORT);
		props.put("mail.smtp.auth", ENABLE);
		props.put("mail.smtp.starttls.enable", ENABLE);
		Session session = Session.getDefaultInstance(props);  

		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(studentAddr));
			message.setSubject(SUBJECT);
			message.setText(new StringBuilder().append(YOUR_GRADE).append(grade).toString());

			Transport.send(message);

		} catch (MessagingException e) {
			logger.error(e);
		}
	}

}
