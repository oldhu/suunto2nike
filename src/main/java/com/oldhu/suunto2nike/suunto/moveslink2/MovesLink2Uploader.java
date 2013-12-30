package com.oldhu.suunto2nike.suunto.moveslink2;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.oldhu.suunto2nike.Util;
import com.oldhu.suunto2nike.nike.NikePlus;
import com.oldhu.suunto2nike.nike.NikePlusProperties;
import com.oldhu.suunto2nike.nike.NikePlusXmlGenerator;
import com.oldhu.suunto2nike.suunto.SuuntoMove;

public class MovesLink2Uploader
{
	private static MovesLink2Uploader _instance = new MovesLink2Uploader();
	private static Log log = LogFactory.getLog(MovesLink2Uploader.class);
	private NikePlusProperties nikePlusProperties;

	public static MovesLink2Uploader getInstance()
	{
		return _instance;
	}

	private MovesLink2Uploader()
	{

	}

	private File getDataFolder()
	{
		String userHome = System.getProperty("user.home");
		if (Util.isWindows()) {
			return new File(new File(userHome), "AppData/Roaming/Suunto/Moveslink2");
		}
		if (Util.isMac()) {
			return new File(new File(userHome), "Library/Application Support/Suunto/Moveslink2");			
		}
		return null;
	}
	
	public boolean checkIfEnvOkay() throws IOException
	{
		File folder = getDataFolder();
		if (!folder.exists()) {
			log.error("Cannot find moveslink2 data folder");
			return false;
		}
		if (!folder.canWrite()) {
			log.error("Cannot write to moveslink2 data folder");
		}

		nikePlusProperties = new NikePlusProperties(getDataFolder());

		return true;
	}
	
	public void uploadXMLFiles() throws Exception
	{
		File folder = getDataFolder();

		File notRunFolder = new File(folder, "NotRun");
		File uploadedMovesFolder = new File(folder, "Uploaded");
		
		notRunFolder.mkdir();
		uploadedMovesFolder.mkdir();
		
		File[] files = folder.listFiles();
		for (File file : files) {
			String fileName = file.getName();
			if (fileName.startsWith("log-")) {
				log.info("Analyzing " + fileName);
				XMLParser parser = new XMLParser(file);
				if (parser.isParseCompleted()) {
					uploadMoveToNike(parser.getSuuntoMove());
					file.renameTo(new File(uploadedMovesFolder, file.getName()));
				} else {
					file.renameTo(new File(notRunFolder, file.getName()));
				}
			}
		}
	}

	private void uploadMoveToNike(SuuntoMove suuntoMove) throws Exception
	{
		NikePlusXmlGenerator nikeXml = new NikePlusXmlGenerator(suuntoMove);
		Document doc = nikeXml.getXML();
		String nikeEmail = nikePlusProperties.getEmail();
		char[] nikePassword = nikePlusProperties.getPassword().toCharArray();
		NikePlus u = new NikePlus();
		u.fullSync(nikeEmail, nikePassword, new Document[] { doc } , null);
	}
}