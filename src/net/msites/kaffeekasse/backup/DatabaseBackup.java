package net.msites.kaffeekasse.backup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import net.msites.kaffeekasse.data.DataContainer;

public class DatabaseBackup {

	public final static String DB_PATH = "db\\";
	public final static String TMP_PATH = "backups\\tmp\\";
	public final static String BACKUP_FILE_EXTENSION = ".kkbackup.zip";
	
	
	// Backup erstellen --------------------------------------------------------
	/**
	 * Erstellt ein Backup der Datenbank und speichert dieses als ZIP in die
	 * Datei <code>backupFile</code>.<br/>
	 * Die Datenbank selbst wird automatisch gestoppt und wieder gestartet.
	 * 
	 * @param backupFile
	 * @throws Exception
	 */
	public static void createBackup(File backupFile) throws Exception{
		createBackup(backupFile, true);
	}
	
	/**
	 * Erstellt ein Backup der Datenbank und speichert dieses als ZIP in die
	 * Datei <code>backupFile</code>.<br/>
	 * Über den Parameter <code>stopAndInitDatabase</code> kann angegeben werden,
	 * ob die Datenbank vor und nach dem Backup gestoppt resp. wieder gestartet
	 * werden soll. Empfohlen: <code>true</code>
	 * 
	 * @param backupFile
	 * @param stopAndInitDatabase
	 * @throws Exception
	 */
	public static void createBackup(File backupFile, boolean stopAndInitDatabase) throws Exception {
		/* Vorbereiten: */
		FileOutputStream fos = new FileOutputStream(backupFile);
		ZipOutputStream zos = new ZipOutputStream(fos);
		zos.setLevel(Deflater.DEFAULT_COMPRESSION);
		File databaseFolder = new File(DB_PATH);
		
		/* Datenbank stoppen: */
		if(stopAndInitDatabase) DataContainer.getInstance().cleanUpDataContainer();
		
		/* Datenbankfiles holen & ins ZIP schreiben: */
		backupFolder(databaseFolder, zos);
		zos.close();
		
		/* Datenbank wieder starten: */
		if(stopAndInitDatabase) DataContainer.getInstance().initDataContainer();
	}
	
	/**
	 * Ermöglicht das rekursive Speichern einer Ordnerstruktur mit allen 
	 * beinhalteten Dateien in ein ZIP-File.
	 * 
	 * @param folder Zu speichernder Ordner
	 * @param zip ZipOutputStream
	 * @throws Exception
	 */
	private static void backupFolder(File folder, ZipOutputStream zip) throws Exception {
		/* Vorbereiten: */
		byte[] buffer = new byte[1024];
		int len = 0;
		
		/* Ordner & Dateien verarbeiten: */
		for(File file: folder.listFiles()) {
			String path = file.getAbsolutePath();
			path = path.substring(path.indexOf(DB_PATH) + DB_PATH.length());
			
			if(file.isFile()) {
				// Datei verarbeiten:
				zip.putNextEntry(new ZipEntry(path));
				
				InputStream in = new FileInputStream(file);
				while((len = in.read(buffer)) > 0 ) {
					zip.write(buffer,0,len);
				}
				in.close();
				
				zip.closeEntry();
			} else {
				// Unterordner verarbeiten:
				backupFolder(file, zip);
			}
		}
	}
	
	
	// Backup wiederherstellen -------------------------------------------------
	/**
	 * Entpackt ein Backup in den temporären Ordner ({@link #TMP_PATH}).
	 * Nach erfolgreichem entpacken wird die aktuelle Datenbank gelöscht und
	 * die entpackte vom temporären Ordner in den Datenbankordner kopiert.<br/>
	 * Somit wird sichergestellt, dass falls ein Fehler beim entpacken auftreten
	 * sollte, eine funktionierende, aktuelle Datenbank nicht schon gelöscht wurde.
	 * 
	 * @param backupFile
	 */
	public static void restoreBackup(File backupFile) throws Exception {
		/* Vorbereiten: */
		ZipInputStream in = new ZipInputStream(new FileInputStream(backupFile));
		ZipFile zf = new ZipFile(backupFile);
		OutputStream out;
		
		// Temp-Ordner:
		File tmpFolder = new File(TMP_PATH);
		if(!tmpFolder.exists()) tmpFolder.mkdirs();
		
		/* Dateien entpacken: */
		for (Enumeration<?> em = zf.entries(); em.hasMoreElements();) {
			String filePath = em.nextElement().toString();
			in.getNextEntry();
			
			/* Ordner erstellen: */
			// Falls nötig, werden Unterordner erstellt.
			if(filePath.lastIndexOf(File.separatorChar) > -1) {
				String folderPath = filePath.substring(0, filePath.lastIndexOf(File.separatorChar)+1);
				File folder = new File(TMP_PATH + folderPath);
				if(!folder.exists()) {
					folder.mkdirs();
				}
			}
			
			/* Datei schreiben: */
			out = new FileOutputStream(TMP_PATH + filePath);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
		}
		in.close();
		
		/* Aktuelle Datenbank stoppen & löschen: */
		DataContainer.getInstance().cleanUpDataContainer();
		eraseFolder(new File(DB_PATH));
		
		/* Entpackte Datenbank verschieben & anschliessend starten: */
		copyFolder(new File(TMP_PATH), TMP_PATH.substring(0, TMP_PATH.length()-1), new File(DB_PATH));
		eraseFolder(new File(TMP_PATH));
		DataContainer.getInstance().initDataContainer();
	}
	
	/**
	 * Löscht den Inhalt des Ordners <code>folder</code> rekursiv.
	 * 
	 * @param folder
	 */
	private static void eraseFolder(File folder) {
		for(File file: folder.listFiles()) {
			if(file.isFile()) {
				System.out.println(file.delete());
			} else {
				eraseFolder(file);
				file.delete();
			}
		}
	}
	
	/**
	 * Kopiert einen Ordner mit all seinen Dateien und Unterordnern rekursiv
	 * an einen anderen Ort.
	 * 
	 * @param sourceFolder File/Folder
	 * @param sourceRoot String
	 * @param targetRoot File/Folder
	 * 
	 */
	private static void copyFolder(File sourceFolder, String sourceRoot, File targetRoot) throws Exception {
		InputStream in;
		OutputStream out;
		byte[] buf = new byte[1024];
		int len;
		
		String targetPath = sourceFolder.getAbsolutePath().replace(sourceRoot, targetRoot.getPath());
		File targetFolder = new File(targetPath);
		if(!targetFolder.exists()) targetFolder.mkdirs();
		
		for(File file: sourceFolder.listFiles()) {
			if(file.isFile()) {
				in = new FileInputStream(file);
				out = new FileOutputStream(new File(targetPath + "\\" + file.getName()));
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				out.close();
				in.close();
			} else {
				copyFolder(file, sourceRoot, targetRoot);
			}
		}
		

	}
	
	// Testing-Code ------------------------------------------------------------
//	public static void main(String[] args) {
//		try {
////			createBackup(new File("backups\\test.kkbackup.zip"), false);
////			restoreBackup(new File("backups\\05-03-2010 10-10.kkbackup.zip"));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
}
