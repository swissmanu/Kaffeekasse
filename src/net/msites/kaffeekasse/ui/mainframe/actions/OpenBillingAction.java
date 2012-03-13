package net.msites.kaffeekasse.ui.mainframe.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import net.msites.guilibrary.toolbox.GUIImageManager;
import net.msites.kaffeekasse.data.entities.Billing;
import net.msites.kaffeekasse.ui.dialog.BillingDialog;
import net.msites.kaffeekasse.ui.dialog.DialogResult;
import net.msites.kaffeekasse.ui.mainframe.BillingTab;
import net.msites.kaffeekasse.ui.mainframe.KaffeekasseFrame;

/**
 * Bestehende Abrechnung öffnen
 */
public class OpenBillingAction extends AbstractAction {
	
	/**
	 * 
	 */
	private final KaffeekasseFrame kaffeekasseFrame;

	public OpenBillingAction(KaffeekasseFrame kaffeekasseFrame) {
		super("Abrechnung öffnen", GUIImageManager.getInstance().getImageIcon("folder_table.png"));
		this.kaffeekasseFrame = kaffeekasseFrame;
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		BillingDialog billingDialog = new BillingDialog(this.kaffeekasseFrame);
		billingDialog.setVisible(true);
		
		if(billingDialog.getDialogResult() == DialogResult.OK) {
			Billing selectedBilling = billingDialog.getSelectedBilling();
			
			this.kaffeekasseFrame.addTab(
					selectedBilling.getTitle(),
					new BillingTab(
							selectedBilling,
							selectedBilling.getUsedConsumers(),
							this.kaffeekasseFrame.getProductColumns()),
					true
					);
		}
	}
}