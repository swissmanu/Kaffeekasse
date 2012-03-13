package net.msites.kaffeekasse.ui.mainframe.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import net.msites.guilibrary.toolbox.GUIImageManager;
import net.msites.kaffeekasse.ui.dialog.ConsumerDialog;
import net.msites.kaffeekasse.ui.mainframe.KaffeekasseFrame;

/**
 * Öffnet den {@link ConsumerDialog}.
 */
public class EditConsumersAction extends AbstractAction {
	
	/**
	 * 
	 */
	private final KaffeekasseFrame kaffeekasseFrame;

	public EditConsumersAction(KaffeekasseFrame kaffeekasseFrame) {
		super("Konsumenten", GUIImageManager.getInstance().getImageIcon("user.png"));
		this.kaffeekasseFrame = kaffeekasseFrame;
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		new ConsumerDialog(this.kaffeekasseFrame).setVisible(true);
	}
}