package ru.futurelink.mo.web.controller.iface;

import java.io.InputStream;
import java.util.HashMap;

public interface IMailSender {
	public void sendMail(String reciever, String subject, String body, HashMap<String, InputStream> attachments);
}
