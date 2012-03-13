package net.msites.kaffeekasse.ui.mainframe.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import net.msites.guilibrary.toolbox.GUIImageManager;
import net.msites.kaffeekasse.backup.DatabaseBackup;
import net.msites.kaffeekasse.ui.dialog.WaitDialog;
import net.msites.kaffeekasse.ui.mainframe.KaffeekasseFrame;

/**
 * Erstellt ein Backup der Datenbank.
 */
public class BackupAction extends AbstractAction {
	
	/**
	 * 
	 */
	public final KaffeekasseFrame kaffeekasseFrame;

	public BackupAction(KaffeekasseFrame kaffeekasseFrame) {
		super("Datenbank-Backup erstellen", GUIImageManager.getInstance().getImageIcon("pill.png"));
		this.kaffeekasseFrame = kaffeekasseFrame;
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_MASK ));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		/* Keine Abrechnungen oder anderes offen?: */
		if(kaffeekasseFrame.hasTabs()) {
			JOptionPane.showMessageDialog(
					this.kaffeekasseFrame,
					"Bitte schliessen und/oder speichern Sie vor dem Erstellen eines Datenbank-Backups vorgängig\n" +
					"alle offenen Abrechnungen.\n" +
					"Somit kann eine vollständige Sicherung der Daten gewährleistet werden.",
					"Datenbank-Backup erstellen",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		/* Vorbereiten: */
		// Dateinamen:
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH-mm");
		String filename = dateFormat.format(new Date()) + DatabaseBackup.BACKUP_FILE_EXTENSION;
		
		// Backup-Ordner:
		File backupFolder = new File("backups\\");
		if(!backupFolder.exists()) backupFolder.mkdir();
		
		/* FileChooser: */
		final JFileChooser fileChooser = new JFileChooser();
		fileChooser.addChoosableFileFilter(
				new FileFilter() {
					@Override
					public String getDescription() {
						return "Kaffeekasse Datenbank-Backup";
					}
					@Override
					public boolean accept(File f) {
						return f.getName().toLowerCase().endsWith(DatabaseBackup.BACKUP_FILE_EXTENSION)
							|| f.isDirectory();
					}
				});
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setDialogTitle("Datenbank-Backup erstellen");
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setCurrentDirectory(backupFolder);
		fileChooser.setSelectedFile(new File(filename));
		int result = fileChooser.showSaveDialog(this.kaffeekasseFrame);
		
		/* Backup erstellen, falls "Save" geklickt: */
		if(result == JFileChooser.APPROVE_OPTION) {
			Runnable task = new Runnable() {
				@Override
				public void run() {
					try {
						DatabaseBackup.createBackup(fileChooser.getSelectedFile(), true);
						JOptionPane.showMessageDialog(
								kaffeekasseFrame,
								"Die Datenbank wurde erfolgreich gesichert.",
								"Datenbank-Backup erstellen",
								JOptionPane.INFORMATION_MESSAGE);
					} catch (Exception e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(
								kaffeekasseFrame,
								"Ein Fehler ist aufgetreten!\n" +
								"Die Datenbank konnte nicht gesichert werden.",
								"Datenbank-Backup erstellen",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			};
			
			WaitDialog waitDialog = new WaitDialog(
					this.kaffeekasseFrame,
					"Datenbank-Backup erstellen",
					"Backup wird erstellt... Bitte warten.",
					task);
			waitDialog.setVisible(true);
		}
	}
}