package net.msites.kaffeekasse.email;

import net.msites.kaffeekasse.data.entities.Consumer;

public class Email {

	private Consumer recepient;
	private String subject;
	private String text;
	
	public Email(Consumer recepient, String subject, String text) {
		this.recepient = recepient;
		this.subject = subject;
		this.text = text;
	}

	public Consumer getRecepient() {
		return recepient;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public String getText() {
		return text;
	}
	
	/**
	 * Erstellt aus den Informationen in diesem Objekt einen gültigen "mailto:"-
	 * Link und gibt diesen als String zurück.
	 * 
	 * @return
	 */
	public String getMailToLink() {
		StringBuffer link = new StringBuffer();
		link.append("mailto:");
		link.append(getRecepient().getEmail());
		link.append("?subject=");
		link.append(getSubject());
		link.append("&body=");
		link.append(encode(getText())); //.replace("\n", "%0A"));
		
		return link.toString();
	}
	
	private String encode(String text) {
		String result = text.replaceAll("%","%25");
		result = result.replaceAll("\"", "%22");
		result = result.replaceAll("\n", "%0A");
		result = result.replaceAll("&","%26");
		result = result.replaceAll("\\+","%2B");
		result = result.replaceAll(",","%2C");
		result = result.replaceAll("/","%2F");
		result = result.replaceAll(";","%3B");
		result = result.replaceAll("=","%3D");
		result = result.replaceAll("\\?","%3F");
		result = result.replaceAll("@","%40");
		result = result.replaceAll("<","%3C");
		result = result.replaceAll(">","%3E");

		
		return result;
	}
	
}
