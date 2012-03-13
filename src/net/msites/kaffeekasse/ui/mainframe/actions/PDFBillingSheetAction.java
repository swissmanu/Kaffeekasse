package net.msites.kaffeekasse.ui.mainframe.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import net.msites.guilibrary.toolbox.GUIImageManager;
import net.msites.kaffeekasse.data.entities.Billing;
import net.msites.kaffeekasse.data.entities.DataEntity;
import net.msites.kaffeekasse.pdf.BillingPDFFactory;
import net.msites.kaffeekasse.ui.components.tabbeduserinterface.AbstractEditableTab;
import net.msites.kaffeekasse.ui.mainframe.KaffeekasseFrame;

/**
 * Erstellt ein PDF aus der aktuellen Abrechnung.
 */
public class PDFBillingSheetAction extends AbstractAction {
	
	/**
	 * 
	 */
	private final KaffeekasseFrame kaffeekasseFrame;

	public PDFBillingSheetAction(KaffeekasseFrame kaffeekasseFrame) {
		super("Abrechnung", GUIImageManager.getInstance().getImageIcon("table_gear.png"));
		this.kaffeekasseFrame = kaffeekasseFrame;
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		AbstractEditableTab editableTag = this.kaffeekasseFrame.getCurrentTab();
		DataEntity data = editableTag.getData();
		
		if(data instanceof Billing) {
			BillingPDFFactory factory = new BillingPDFFactory((Billing)data);
			factory.createPdf(this.kaffeekasseFrame, "Abrechnung.pdf");
		}
	}
}