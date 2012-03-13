package net.msites.kaffeekasse.ui.mainframe.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import net.msites.guilibrary.toolbox.GUIImageManager;
import net.msites.kaffeekasse.ui.dialog.ConsumptionSheetDialog;
import net.msites.kaffeekasse.ui.mainframe.KaffeekasseFrame;

/**
 * Erstellt eine Strichliste für den Pausenraum als PDF.
 */
public class PDFConsumptationSheetAction extends AbstractAction {
	
	/**
	 * 
	 */
	private final KaffeekasseFrame kaffeekasseFrame;

	public PDFConsumptationSheetAction(KaffeekasseFrame kaffeekasseFrame) {
		super("Strichliste", GUIImageManager.getInstance().getImageIcon("table_edit.png"));
		this.kaffeekasseFrame = kaffeekasseFrame;
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_MASK ));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		new ConsumptionSheetDialog(this.kaffeekasseFrame).setVisible(true);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_MASK));
	}
}