package net.msites.kaffeekasse.ui.mainframe.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import net.msites.guilibrary.toolbox.GUIImageManager;
import net.msites.kaffeekasse.ui.dialog.WaitDialog;
import net.msites.kaffeekasse.ui.mainframe.KaffeekasseFrame;

/**
 * Aktuelle Abrechnung speichern
 */
public class SaveAction extends AbstractAction {
	
	/**
	 * 
	 */
	private final KaffeekasseFrame kaffeekasseFrame;

	public SaveAction(KaffeekasseFrame kaffeekasseFrame) {
		super("Speichern", GUIImageManager.getInstance().getImageIcon("disk.png"));
		this.kaffeekasseFrame = kaffeekasseFrame;
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				SaveAction.this.kaffeekasseFrame.getCurrentTab().save();
			}
		};
		
		WaitDialog waitDialog = new WaitDialog(
				this.kaffeekasseFrame,
				"Speichern",
				"Daten werden gespeichert... Bitte warten.",
				task);
		waitDialog.setVisible(true);
	}
}