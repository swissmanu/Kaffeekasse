package net.msites.kaffeekasse.ui.dialog.editordialog;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;

import net.msites.kaffeekasse.data.entities.DataEntity;
import net.msites.kaffeekasse.data.entities.OtherCost;


public class OtherCostEditor extends EditorComponent {

	private OtherCost otherCost;
	
	private final JTextField txtText = new JTextField();
	private final JTextField txtAmount = new JTextField();
	
	public OtherCostEditor() {
		this(null);
	}
	
	public OtherCostEditor(OtherCost otherCost) {
		super();
		
		/* Daten übernehmen: */
		if(otherCost != null) {
			this.otherCost = new OtherCost(
					null,
					otherCost.getText(),
					otherCost.getAmount()
			);
			this.otherCost.setSurpressChangeFlag(true);
		} else {
			this.otherCost = new OtherCost();
		}
		
		/* GUI erstellen: */
		initGui();
		buildGui();
	}
	
	// GUI-Erstellung ----------------------------------------------------------
	private void initGui() {
		txtText.setText(otherCost.getText());
		txtAmount.setText(otherCost.getAmount().toString());
	}
	
	private void buildGui() {
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(true);
		
		JLabel lblText = new JLabel("Text");
		JLabel lblBetrag = new JLabel("Betrag");
		
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblText)
						.addComponent(txtText)
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblBetrag)
						.addComponent(txtAmount)
				)
		);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
						.addComponent(lblText)
						.addComponent(lblBetrag)						
				)
				.addGroup(layout.createParallelGroup()
						.addComponent(txtText)
						.addComponent(txtAmount)
				)				
		);
		
	}

	
	// EditorPanel-Implementierung ---------------------------------------------
	@Override
	public void writeData() {
		this.otherCost.setText(txtText.getText());
		this.otherCost.setAmount(Double.parseDouble(txtAmount.getText()));
	}
	
	@Override
	public DataEntity getData() {
		return otherCost;
	}
	
	@Override
	public boolean validateData() {
		boolean dataValid = true;
		
		if(txtText.getText().equals("")) dataValid = false;
		if(dataValid && txtAmount.getText().equals("")) dataValid = false;
		
		return dataValid;
	}

}
