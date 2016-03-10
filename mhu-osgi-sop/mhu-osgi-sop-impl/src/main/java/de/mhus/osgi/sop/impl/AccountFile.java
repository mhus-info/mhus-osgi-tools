package de.mhus.osgi.sop.impl;

import java.io.File;
import java.util.HashSet;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MXml;
import de.mhus.osgi.sop.api.Sop;
import de.mhus.osgi.sop.api.SopApi;
import de.mhus.osgi.sop.api.aaa.Account;

public class AccountFile extends MLog implements Account {
	
	private Document doc;
	private String account;
	private boolean valide;
	private File file;
	private long modified;
	private String name;
	
	private String password = null;
	private long timeout;
	private Boolean isPasswordValidated = null;
	private HashSet<String> groups = new HashSet<>();
	
	public AccountFile(File f, String account) throws ParserConfigurationException, SAXException, IOException {
		FileInputStream is = new FileInputStream(f);
		doc = MXml.loadXml(is);
		is.close();
		this.account = account;
		valide = account != null;
		file = f;
		modified = f.lastModified();
		
		
		Element pwE = MXml.getElementByPath(doc.getDocumentElement(),"password");
		if (pwE != null)
				password = pwE.getAttribute("plain");
		name = MXml.getElementByPath(doc.getDocumentElement(),"information").getAttribute("name");
		
		timeout = MCast.tolong( doc.getDocumentElement().getAttribute("timeout"), 0);
		
		
		Element xmlAcl = MXml.getElementByPath(doc.getDocumentElement(), "groups");
		for (Element xmlAce : MXml.getLocalElementIterator(xmlAcl,"group")) {
			groups.add(xmlAce.getAttribute("name").trim().toLowerCase());
		}
	}

	private UUID toUUID(String value) {
		if (MString.isEmpty(value)) return null;
		try {
			return UUID.fromString(value);
		} catch (IllegalArgumentException e) {}
		return null;
	}

	@Override
	public String getAccount() {
		return account;
	}

	@Override
	public boolean isValide() {
		return valide;
	}

	@Override
	public synchronized boolean validatePassword(String password) {
		if (isPasswordValidated == null) {
			try {
				if (password != null) {
					boolean out = validatePasswordInternal(password);
					if (out) {
						isPasswordValidated = true;
						return true;
					}
				}
				isPasswordValidated = Sop.getApi(SopApi.class).validatePassword(this, password);
			} catch (Throwable t) {
				log().w("validatePassword",account,t);
			}
		}
		return isPasswordValidated == null ? false : isPasswordValidated;
	}

	public boolean isChanged() {
		return !file.exists() || modified != file.lastModified();
	}


	public String toString() {
		return account + " " + name;
	}
	
	@Override
	public boolean isSyntetic() {
		return false;
	}

	@Override
	public String getDisplayName() {
		return name;
	}
	
	public long getTimeout() {
		return timeout;
	}

	public boolean validatePasswordInternal(String password) {
		return this.password.equals(password);
	}

	@Override
	public boolean hasGroup(String group) {
		return groups.contains(group);
	}

}
