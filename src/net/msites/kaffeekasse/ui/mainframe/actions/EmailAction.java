package net.msites.kaffeekasse.ui.mainframe.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import net.msites.guilibrary.toolbox.GUIImageManager;
import net.msites.kaffeekasse.data.entities.Billing;
import net.msites.kaffeekasse.data.entities.DataEntity;
import net.msites.kaffeekasse.ui.components.tabbeduserinterface.AbstractEditableTab;
import net.msites.kaffeekasse.ui.dialog.EmailDialog;
import net.msites.kaffeekasse.ui.mainframe.KaffeekasseFrame;

/**
 * Generiert persönliche E-Mails für alle Konsumenten in der aktuellen
 * Abrechnung.
 */
public 
class EmailAction extends AbstractAction {
	
	/**
	 * 
	 */
	private final KaffeekasseFrame kaffeekasseFrame;

	public EmailAction(KaffeekasseFrame kaffeekasseFrame) {
		super("Abrechnungs-E-Mails erstellen", GUIImageManager.getInstance().getImageIcon("email_start.png"));
		this.kaffeekasseFrame = kaffeekasseFrame;
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_MASK));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		AbstractEditableTab editableTag = this.kaffeekasseFrame.getCurrentTab();
		DataEntity data = editableTag.getData();
		
		if(data instanceof Billing) {
			EmailDialog emailDialog = new EmailDialog(this.kaffeekasseFrame, (Billing)data);
			emailDialog.setVisible(true);
		}
	}
}