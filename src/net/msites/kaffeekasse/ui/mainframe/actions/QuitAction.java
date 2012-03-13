package net.msites.kaffeekasse.ui.mainframe.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import net.msites.kaffeekasse.data.DataContainer;
import net.msites.kaffeekasse.ui.mainframe.KaffeekasseFrame;

/** 
 * Beendet die Applikation.
 */
public class QuitAction extends AbstractAction {
	
	/**
	 * 
	 */
	private final KaffeekasseFrame kaffeekasseFrame;

	public QuitAction(KaffeekasseFrame kaffeekasseFrame) {
		super("Beenden");
		this.kaffeekasseFrame = kaffeekasseFrame;
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK ));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		DataContainer.getInstance().cleanUpDataContainer();
		this.kaffeekasseFrame.dispose();
		
		System.exit(0);
	}
}