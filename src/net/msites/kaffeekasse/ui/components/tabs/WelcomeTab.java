package net.msites.kaffeekasse.ui.components.tabs;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import net.msites.kaffeekasse.data.entities.DataEntity;

import com.itextpdf.text.Font;

public class WelcomeTab extends AbstractEditableTab {
	
	public WelcomeTab() {
		buildGui();
	}
	
	private void buildGui() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
		
		JLabel lblTitle = new JLabel("Willkommen in der Kaffeekasse!");
		lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 18f));
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

		add(lblTitle,BorderLayout.NORTH);
	}
	
	
	// AbstractEditableTab-Implementierung -------------------------------------
	@Override
	/**
	 * Dieser Tab kann nie geschlossen werden.
	 */
	public boolean beforeCloseTab() {
		return false;
	}
	
	@Override
	public boolean canSave() {
		return false;
	}

	@Override
	public DataEntity getData() {
		return null;
	}

	@Override
	public boolean hasChanged() {
		return false;
	}

	@Override
	public void save() {}

}
