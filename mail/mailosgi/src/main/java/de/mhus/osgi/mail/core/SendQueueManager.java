package de.mhus.osgi.mail.core;

import javax.mail.Address;
import javax.mail.Message;

public interface SendQueueManager {

	public static String QUEUE_DEFAULT = "default";
	
	/**
	 * Send a mail.
	 * 
	 * @param queue TODO
	 * @param message TODO
	 * @param addresse TODO
	 * @throws Exception TODO
	 */
	void sendMail(String queue, Message message, Address addresse) throws Exception;

	/**
	 * Send a mail.
	 * 
	 * @param queue TODO
	 * @param message TODO
	 * @param addresses TODO
	 * @throws Exception  TODO
	 */
	void sendMail(String queue, Message message, Address[] addresses) throws Exception;
	
	/**
	 * Get a queue.
	 * 
	 * @param name TODO
	 * @return the queue
	 */
	SendQueue getQueue(String name);
	
	/**
	 * Register a new queue or overwrite a queue.
	 * 
	 * @param queue TODO
	 */
	void registerQueue(SendQueue queue);
	
	/**
	 * Unregister a existing queue.
	 * 
	 * @param name TODO
	 */
	void unregisterQueue(String name);
	
	/**
	 * Return a list of queue names.
	 * 
	 * @return the list
	 */
	String[] getQueueNames();

	void sendMail(Message message, Address[] addresses) throws Exception;

	void sendMail(Message message, Address addresse) throws Exception;
	
}