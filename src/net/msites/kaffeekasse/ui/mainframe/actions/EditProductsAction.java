package net.msites.kaffeekasse.ui.mainframe.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import net.msites.guilibrary.toolbox.GUIImageManager;
import net.msites.kaffeekasse.ui.dialog.ProductDialog;
import net.msites.kaffeekasse.ui.mainframe.KaffeekasseFrame;

/** 
 * Öffnet den {@link ProductDialog}.
 */
public class EditProductsAction extends AbstractAction {
	
	/**
	 * 
	 */
	private final KaffeekasseFrame kaffeekasseFrame;

	public EditProductsAction(KaffeekasseFrame kaffeekasseFrame) {
		super("Produkte", GUIImageManager.getInstance().getImageIcon("package_green.png"));
		this.kaffeekasseFrame = kaffeekasseFrame;
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		new ProductDialog(this.kaffeekasseFrame).setVisible(true);
	}
}