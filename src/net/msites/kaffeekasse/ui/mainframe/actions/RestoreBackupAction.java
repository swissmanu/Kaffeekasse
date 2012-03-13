package net.msites.kaffeekasse.ui.mainframe.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import net.msites.kaffeekasse.backup.DatabaseBackup;
import net.msites.kaffeekasse.ui.dialog.WaitDialog;
import net.msites.kaffeekasse.ui.mainframe.KaffeekasseFrame;

/**
 * Stellt ein Datenbankbackup wieder her.
 */
public class RestoreBackupAction extends AbstractAction {
	
	/**
	 * 
	 */
	private final KaffeekasseFrame kaffeekasseFrame;

	public RestoreBackupAction(KaffeekasseFrame kaffeekasseFrame) {
		super("Datenbank-Backup wiederherstellen");
		this.kaffeekasseFrame = kaffeekasseFrame;
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK ));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		/* Keine Abrechnungen oder anderes offen?: */
		if(this.kaffeekasseFrame.hasTabs()) {
			JOptionPane.showMessageDialog(
					this.kaffeekasseFrame,
					"Bitte schliessen Sie vor dem Wiederherstellen eines Datenbank-Backups vorgängig\n" +
					"alle offenen Abrechnungen.\n",
					"Datenbank-Backup wiederherstellen",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		/* Vorbereiten: */
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
		fileChooser.setDialogTitle("Datenbank-Backup wiederherstellen");
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setCurrentDirectory(backupFolder);
		int result = fileChooser.showOpenDialog(this.kaffeekasseFrame);
		
		if(result == JFileChooser.APPROVE_OPTION) {
			result = JOptionPane.showConfirmDialog(
					this.kaffeekasseFrame,
					"Sind sie sicher, dass Sie das Backup mit dem Namen \"" +
					fileChooser.getSelectedFile().getName() + "\" wiederherstellen möchten?\n" +
					"Alle momentan vorhandenen Daten (Abrechnungen, Konsumenten usw.) gehen damit " +
					"unwiederruflich verloren!",
					"Datenbank-Backup wiederherstellen",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE
					);
			
			if(result == JOptionPane.YES_OPTION) {
				Runnable task = new Runnable() {
					@Override
					public void run() {
						try {
							DatabaseBackup.restoreBackup(fileChooser.getSelectedFile());
							JOptionPane.showMessageDialog(
									RestoreBackupAction.this.kaffeekasseFrame,
									"Die Datenbank wurde erfolgreich wiederhergestellt.",
									"Datenbank-Backup wiederherstellen",
									JOptionPane.INFORMATION_MESSAGE);
						} catch (Exception e1) {
							e1.printStackTrace();
							JOptionPane.showMessageDialog(
									RestoreBackupAction.this.kaffeekasseFrame,
									"Ein Fehler ist aufgetreten!\n" +
									"Die Datenbank konnte nicht wiederhergestellt werden.",
									"Datenbank-Backup wiederherstellen",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				};
				
				WaitDialog waitDialog = new WaitDialog(
						this.kaffeekasseFrame,
						"Datenbank-Backup wiederherstellen",
						"Backup wird wiederhergestellt... Bitte warten.",
						task);
				waitDialog.setVisible(true);
			}
		}
	}
}