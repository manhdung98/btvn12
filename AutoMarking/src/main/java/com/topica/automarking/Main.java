package com.topica.automarking;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.Store;

import org.apache.log4j.Logger;

public class Main {
	static final Logger logger = Logger.getLogger(Handler.class);
	private static final String USERNAME = "daotv97@gmail.com";
	private static final String PASSWORD = "tranvandao";
	
	public static void main(String[] args) {
		Handler handler = new Handler(USERNAME, PASSWORD);
		
		try {
			Store store = handler.conn();
			handler.readAndDown(store);
			store.close();
		} catch (MessagingException | IOException e) {
			logger.error(e);
		}
		
	}
}
