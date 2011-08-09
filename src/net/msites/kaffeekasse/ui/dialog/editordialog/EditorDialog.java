package net.msites.kaffeekasse.ui.dialog.editordialog;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.GroupLayout.Alignment;

import net.msites.guilibrary.toolbox.GUIHelper;
import net.msites.kaffeekasse.data.entities.DataEntity;
import net.msites.kaffeekasse.ui.dialog.DialogResult;

public class EditorDialog extends JDialog {

	private final OKAction actionOK = new OKAction();
	private final CancelAction actionCancel = new CancelAction();
	private EditorComponent editor;
	private DialogResult dialogResult;
	
	
	public EditorDialog(Dialog owner, EditorComponent editor, String title) {
		super(owner, title, true);
		initEditorDialog(editor);
	}
	
	public EditorDialog(Frame owner, EditorComponent editor, String title) {
		super(owner, title, true);
		initEditorDialog(editor);
	}
	
	private void initEditorDialog(EditorComponent editor) {
		this.editor = editor;
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				actionCancel.actionPerformed(new ActionEvent(this,0,"windowClosed"));
			}
		});
		
		setContentPane(buildGui(editor));
		pack();
		setResizable(false);
		GUIHelper.centerOnOwner(this, getOwner());
	}
	
	
	private JComponent buildGui(EditorComponent editor) {
		JPanel gui = new JPanel();
		GroupLayout layout = new GroupLayout(gui);
		gui.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		initKeyBindings(gui);
		
		editor.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		
		JButton btnOK = new JButton(actionOK);
		JButton btnCancel = new JButton(actionCancel);
		getRootPane().setDefaultButton(btnOK);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(editor)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnOK)
						.addComponent(btnCancel)
				)
		);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
						.addComponent(editor)
						
						.addGroup(Alignment.TRAILING, layout.createSequentialGroup()
								.addComponent(btnOK)
								.addComponent(btnCancel)
						)
				)
		);
		
		return gui;
	}
	
	private void initKeyBindings(JComponent component) {
		InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "OK");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Cancel");
		
		ActionMap actionMap = component.getActionMap();
		actionMap.put("OK", actionOK);
		actionMap.put("Cancel", actionCancel);
	}
	
	// Zugriff -----------------------------------------------------------------
	/**
	 * Gibt die Daten aus dem Editor in Form eines {@link DataEntity}'s zurück.
	 * 
	 * @return {@link DataEntity}
	 * @see EditorComponent#getData()
	 */
	public DataEntity getData() {
		return editor.getData();
	}
	
	public DialogResult getDialogResult() {
		return dialogResult;
	}
	
	// Actions -----------------------------------------------------------------
	private class OKAction extends AbstractAction {
		public OKAction() {
			super("Speichern");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if(!editor.validateData()) {
				JOptionPane.showMessageDialog(
						EditorDialog.this,
						"Bitte stellen Sie sicher, dass alle Felder korrekt ausgefüllt sind!",
						"Fehler",
						JOptionPane.ERROR_MESSAGE);
			} else {
				editor.writeData();
				dialogResult = DialogResult.OK;
				dispose();
			}
		}
	}
	
	private class CancelAction extends AbstractAction {
		public CancelAction() {
			super("Abbrechen");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			dialogResult = DialogResult.CANCEL;
			dispose();
		}
	}
	
}
