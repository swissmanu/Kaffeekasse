package net.msites.kaffeekasse.ui.mainframe.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import net.msites.guilibrary.toolbox.GUIImageManager;
import net.msites.kaffeekasse.ui.dialog.chartdialog.ChartsDialog;
import net.msites.kaffeekasse.ui.mainframe.KaffeekasseFrame;

/**
 * Zeigt den Dialog mit den verschiedenen Diagrammen an.
 */
public class ChartsAction extends AbstractAction {
	
	/**
	 * 
	 */
	private final KaffeekasseFrame kaffeekasseFrame;

	public ChartsAction(KaffeekasseFrame kaffeekasseFrame) {
		super("Auswertungen", GUIImageManager.getInstance().getImageIcon("chart_bar.png"));
		this.kaffeekasseFrame = kaffeekasseFrame;
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_MASK ));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		new ChartsDialog(this.kaffeekasseFrame).setVisible(true);
	}
}