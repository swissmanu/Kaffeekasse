package net.msites.kaffeekasse.ui.dialog;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.GroupLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.GroupLayout.Alignment;

import net.msites.guilibrary.toolbox.GUIHelper;
import net.msites.guilibrary.toolbox.MultilineLabel;
import net.msites.kaffeekasse.data.DataContainer;
import net.msites.kaffeekasse.data.entities.Billing;
import net.msites.kaffeekasse.data.entities.Language;
import net.msites.kaffeekasse.data.entities.Property;
import net.msites.kaffeekasse.email.Email;
import net.msites.kaffeekasse.email.EmailFactory;

public class EmailDialog extends JDialog {

	private final Map<Language, TranslationTab> tabs = new HashMap<Language, TranslationTab>();
	private final JTabbedPane tbpTranslations = new JTabbedPane();
	private final Action actionCreate = new CreateAction();
	private final Action actionCancel = new CancelAction();
	
	private Billing billing;
	
	public EmailDialog(Window owner, Billing billing) {
		super(owner);
		this.billing = billing;
		
		setModal(true);
		setTitle("Persönliche Abrechnungs-E-Mails erstellen");
		setSize(500,400);
		GUIHelper.centerOnOwner(this, owner);
		setResizable(false);
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				actionCancel.actionPerformed(new ActionEvent(this,0,"windowClosed"));
			}
		});
		
		/* GUI erstellen: */
		setContentPane(buildGui());
	}
	
	/**
	 * Setzt die {@link Component}'s des GUI's zusammen und gibt diese zusammengefasst
	 * in einer {@link JComponent} zurück.
	 * 
	 * @return
	 */
	private JComponent buildGui() {
		JPanel gui = new JPanel();
		GroupLayout layout = new GroupLayout(gui);
		gui.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		initKeyBindings(gui);
		initGui();
		
		MultilineLabel lblIntro = new MultilineLabel(
				"E-Mails werden passend für die Sprache des entsprechenden " +
				"Konsumenten erstellt. Passen Sie die Übersetzungen, falls " +
				"gewünscht, an und klicken Sie auf \"E-Mails erstellen\"."
				);
		JButton btnCreate = new JButton(actionCreate);
		JButton btnClose = new JButton(actionCancel);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(lblIntro, 35,35,35)
				.addComponent(tbpTranslations)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnCreate)
						.addComponent(btnClose)
						)
				);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
						.addComponent(lblIntro)
						.addComponent(tbpTranslations)
						.addGroup(Alignment.LEADING, layout.createSequentialGroup()

								)
						.addGroup(Alignment.TRAILING, layout.createSequentialGroup()
								.addComponent(btnCreate)
								.addComponent(btnClose)
								)
						)
				);

		return gui;
	}
	
	/**
	 * Initialisiert das GUI.
	 */
	private void initGui() {
		tabs.put(Language.GERMAN, new TranslationTab(Language.GERMAN));
		tabs.put(Language.ENGLISH, new TranslationTab(Language.ENGLISH));
		
		tbpTranslations.setFocusable(false);
		tbpTranslations.addTab("Deutsch", tabs.get(Language.GERMAN));
		tbpTranslations.addTab("Englisch", tabs.get(Language.ENGLISH));
		
		loadTranslations();
	}
	
	private void initKeyBindings(JComponent component) {
		InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Create");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Close");
		
		ActionMap actionMap = component.getActionMap();
		actionMap.put("Create", actionCreate);
		actionMap.put("Close", actionCancel);
	}
	
	
	// Persistenz --------------------------------------------------------------
	private void saveTranslations() {
		DataContainer dataContainer = DataContainer.getInstance();
		
		/* Deutsch: */
		TranslationTab tab = tabs.get(Language.GERMAN);
		Property property = dataContainer.getProperty(DataContainer.PROPERTY_KEY_EMAIL_SUBJECT_GERMAN);
		if(property != null) property.setValue(tab.getSubject());
		else property = new Property(DataContainer.PROPERTY_KEY_EMAIL_SUBJECT_GERMAN, tab.getSubject());
		dataContainer.putProperty(property);
		
		property = dataContainer.getProperty(DataContainer.PROPERTY_KEY_EMAIL_TEXT_GERMAN);
		if(property != null) property.setValue(tab.getText());
		else property = new Property(DataContainer.PROPERTY_KEY_EMAIL_TEXT_GERMAN, tab.getText());
		dataContainer.putProperty(property);
		
		
		/* Englisch: */
		tab = tabs.get(Language.ENGLISH);
		property = dataContainer.getProperty(DataContainer.PROPERTY_KEY_EMAIL_SUBJECT_ENGLISH);
		if(property != null) property.setValue(tab.getSubject());
		else property = new Property(DataContainer.PROPERTY_KEY_EMAIL_SUBJECT_ENGLISH, tab.getSubject());
		dataContainer.putProperty(property);
		
		property = dataContainer.getProperty(DataContainer.PROPERTY_KEY_EMAIL_TEXT_ENGLISH);
		if(property != null) property.setValue(tab.getText());
		else property = new Property(DataContainer.PROPERTY_KEY_EMAIL_TEXT_ENGLISH, tab.getText());
		dataContainer.putProperty(property);
	}
	
	private void loadTranslations() {
		DataContainer dataContainer = DataContainer.getInstance();
		
		/* Deutsch: */
		Property property = dataContainer.getProperty(DataContainer.PROPERTY_KEY_EMAIL_SUBJECT_GERMAN);
		if(property != null) tabs.get(Language.GERMAN).setSubject(property.getValue());
		property = dataContainer.getProperty(DataContainer.PROPERTY_KEY_EMAIL_TEXT_GERMAN);
		if(property != null) tabs.get(Language.GERMAN).setText(property.getValue());
		
		/* Englisch: */
		property = dataContainer.getProperty(DataContainer.PROPERTY_KEY_EMAIL_SUBJECT_ENGLISH);
		if(property != null) tabs.get(Language.ENGLISH).setSubject(property.getValue());
		property = dataContainer.getProperty(DataContainer.PROPERTY_KEY_EMAIL_TEXT_ENGLISH);
		if(property != null) tabs.get(Language.ENGLISH).setText(property.getValue());		
	}
	
	
	// Hilfskomponenten --------------------------------------------------------
	private class TranslationTab extends JPanel {
		
		private final JTextField txtSubject = new JTextField();
		private final JTextArea txtText = new JTextArea();
		
		@SuppressWarnings("unused")
		private Language language;
		
		public TranslationTab(Language language) {
			this.language = language;
			
			JLabel lblSubject = new JLabel("Betreff");
			JLabel lblText = new JLabel("Text");
			JScrollPane scpText = new JScrollPane(txtText);
			
			GroupLayout layout = new GroupLayout(this);
			setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.BASELINE)
							.addComponent(lblSubject)
							.addComponent(txtSubject)
							)
					.addGroup(layout.createParallelGroup(Alignment.BASELINE)
							.addComponent(lblText)
							.addComponent(scpText)
							) 
				);
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup()
							.addComponent(lblSubject)
							.addComponent(lblText)
							)
					.addGroup(layout.createParallelGroup()
							.addComponent(txtSubject)
							.addComponent(scpText)
							) 
					);
			
		}
		
		public void setSubject(String subject) {
			txtSubject.setText(subject);
		}
		
		public void setText(String text) {
			txtText.setText(text);
		}
		
		public String getSubject() {
			return txtSubject.getText();
		}
		
		public String getText() {
			return txtText.getText();
		}
		
	}
	
	
	// Actions -----------------------------------------------------------------
	private class CreateAction extends AbstractAction {
		public CreateAction() {
			super("E-Mails erstellen");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Runnable task = new Runnable() {
				@Override
				public void run() {
					saveTranslations();
					
					Map<Language, String> subjects = new HashMap<Language, String>();
					Map<Language, String> text = new HashMap<Language, String>();
					subjects.put(Language.GERMAN, tabs.get(Language.GERMAN).getSubject());
					subjects.put(Language.ENGLISH, tabs.get(Language.ENGLISH).getSubject());
					text.put(Language.GERMAN, tabs.get(Language.GERMAN).getText());
					text.put(Language.ENGLISH, tabs.get(Language.ENGLISH).getText());
					
					List<Email> emails = EmailFactory.createEmails(billing, subjects, text);					
					for(Email email: emails) {
						try {
							Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + email.getMailToLink());
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
					
					JOptionPane.showMessageDialog(
							SwingUtilities.getWindowAncestor(EmailDialog.this),
							"Alle E-Mails wurden erfolgreich erstellt!",
							"E-Mails erstellt",
							JOptionPane.INFORMATION_MESSAGE);
				}
			};
			
			WaitDialog wait = new WaitDialog(
					EmailDialog.this,
					"E-Mails erstellen",
					"E-Mails werden erstellt... Bitte warten.",
					task);
			wait.setVisible(true);
			
			dispose();
		}
	}
	
	private class CancelAction extends AbstractAction {
		public CancelAction() {
			super("Abbrechen");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	}
	
}
