package net.msites.kaffeekasse.ui.mainframe.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import net.msites.kaffeekasse.ui.components.tabbeduserinterface.TabbedUserInterface;

/**
 * Schliesst den aktuellen Tab, ausser, der "Willkommen"-Tab ist ausgewählt.
 */
public class CloseTabAction extends AbstractAction {
	
	/**
	 * 
	 */
	private final TabbedUserInterface kaffeekasseFrame;

	public CloseTabAction(TabbedUserInterface kaffeekasseFrame) {
		super("Schliessen");
		this.kaffeekasseFrame = kaffeekasseFrame;
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK ));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		kaffeekasseFrame.closeCurrentTab();
	}

}