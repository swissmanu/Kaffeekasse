package net.msites.kaffeekasse.ui.dialog.editordialog;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;

import net.msites.kaffeekasse.data.entities.Consumer;
import net.msites.kaffeekasse.data.entities.DataEntity;
import net.msites.kaffeekasse.data.entities.Language;


public class ConsumerEditor extends EditorComponent {

	private Consumer consumer;
	
	private final JTextField txtFirstname = new JTextField();
	private final JTextField txtLastname = new JTextField();
	private final JTextField txtEmail = new JTextField();
	private final JComboBox comLanguage = new JComboBox(new String[]{"Deutsch","Englisch"});
	private final JCheckBox chkAddToNewBillings = new JCheckBox("Automatisch zu neuen Abrechnungen hinzufügen");
	
	public ConsumerEditor() {
		this(null);
	}
	
	public ConsumerEditor(Consumer consumer) {
		super();
		
		/* Daten übernehmen: */
		if(consumer != null) {
			this.consumer = new Consumer(
					consumer.getFirstname(),
					consumer.getLastname(),
					consumer.getEmail(),
					consumer.getLanguage(),
					consumer.getAddToNewBillings()
			);			
		} else {
			this.consumer = new Consumer();
		}
		
		/* GUI erstellen: */
		initGui();
		buildGui();
	}
	
	// GUI-Erstellung ----------------------------------------------------------
	private void initGui() {
		/* Daten in Komponenten umfüllen: */
		txtLastname.setText(consumer.getLastname());
		txtFirstname.setText(consumer.getFirstname());
		txtEmail.setText(consumer.getEmail());
		chkAddToNewBillings.setSelected(consumer.getAddToNewBillings());
		
		switch(consumer.getLanguage()) {
		case GERMAN :
			comLanguage.setSelectedIndex(0);
			break;
		case ENGLISH :
			comLanguage.setSelectedIndex(1);
			break;
		}
	}
	
	private void buildGui() {
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(true);
		
		JLabel lblLastname = new JLabel("Nachname");
		JLabel lblFirstname = new JLabel("Vorname");
		JLabel lblEmail = new JLabel("E-Mail-Adresse");
		JLabel lblLanguage = new JLabel("Sprache");
		JLabel lblAddToNewBillings = new JLabel("Hinzufügen");
		
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblLastname)
						.addComponent(txtLastname)
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblFirstname)
						.addComponent(txtFirstname)
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblEmail)
						.addComponent(txtEmail)
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblLanguage)
						.addComponent(comLanguage)
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblAddToNewBillings)
						.addComponent(chkAddToNewBillings)
				)
		);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
						.addComponent(lblLastname)
						.addComponent(lblFirstname)
						.addComponent(lblEmail)
						.addComponent(lblLanguage)
						.addComponent(lblAddToNewBillings)
				)
				.addGroup(layout.createParallelGroup()
						.addComponent(txtLastname)
						.addComponent(txtFirstname)
						.addComponent(txtEmail)
						.addComponent(comLanguage)
						.addComponent(chkAddToNewBillings)
				)				
		);
		
	}

	
	// EditorPanel-Implementierung ---------------------------------------------
	@Override
	public void writeData() {
		this.consumer.setLastname(txtLastname.getText());
		this.consumer.setFirstname(txtFirstname.getText());
		this.consumer.setEmail(txtEmail.getText());
		this.consumer.setAddToNewBillings(chkAddToNewBillings.isSelected());
		
		if(comLanguage.getSelectedIndex() == 0) this.consumer.setLanguage(Language.GERMAN);
		else this.consumer.setLanguage(Language.ENGLISH);
	}
	
	@Override
	public DataEntity getData() {
		return consumer;
	}
	
	@Override
	public boolean validateData() {
		boolean dataValid = true;
		
		if(txtLastname.getText().equals("")) dataValid = false;
		if(dataValid && txtFirstname.getText().equals("")) dataValid = false;
		if(dataValid && txtEmail.getText().equals("")) dataValid = false;
		
		return dataValid;
	}

}
