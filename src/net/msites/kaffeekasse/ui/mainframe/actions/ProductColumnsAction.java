package net.msites.kaffeekasse.ui.mainframe.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import net.msites.guilibrary.toolbox.GUIImageManager;
import net.msites.kaffeekasse.ui.dialog.ProductColumnsDialog;
import net.msites.kaffeekasse.ui.mainframe.KaffeekasseFrame;

public class ProductColumnsAction extends AbstractAction {
	
	/**
	 * 
	 */
	private final KaffeekasseFrame kaffeekasseFrame;

	public ProductColumnsAction(KaffeekasseFrame kaffeekasseFrame) {
		super("Produktespalten anpassen", GUIImageManager.getInstance().getImageIcon("table_column.png"));
		this.kaffeekasseFrame = kaffeekasseFrame;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		new ProductColumnsDialog(this.kaffeekasseFrame).setVisible(true);
	}
}