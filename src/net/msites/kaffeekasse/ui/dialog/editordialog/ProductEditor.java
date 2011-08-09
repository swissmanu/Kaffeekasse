package net.msites.kaffeekasse.ui.dialog.editordialog;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;

import net.msites.kaffeekasse.data.entities.DataEntity;
import net.msites.kaffeekasse.data.entities.Product;


public class ProductEditor extends EditorComponent {

	private Product product;
	
	private final JTextField txtProductName = new JTextField();
	private final JTextField txtProductNameEN = new JTextField();
	private final JTextField txtPrice = new JTextField();
	
	public ProductEditor() {
		this(null);
	}
	
	public ProductEditor(Product product) {
		super();
		
		/* Daten übernehmen: */
		if(product != null) {
			this.product = new Product(
					product.getName(),
					product.getName_en(),
					product.getPrice()
			);			
		} else {
			this.product = new Product();
		}
		
		/* GUI erstellen: */
		initGui();
		buildGui();
	}
	
	// GUI-Erstellung ----------------------------------------------------------
	private void initGui() {
		txtProductName.setText(product.getName());
		txtProductNameEN.setText(product.getName_en());
		txtPrice.setText(product.getPrice().toString());
	}
	
	private void buildGui() {
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(true);
		
		JLabel lblProductName = new JLabel("Produktname");
		JLabel lblProductNameEN = new JLabel("Produktname (engl.)");
		JLabel lblPrice = new JLabel("Basispreis");
		
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblProductName)
						.addComponent(txtProductName)
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblProductNameEN)
						.addComponent(txtProductNameEN)
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblPrice)
						.addComponent(txtPrice)
				)
		);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
						.addComponent(lblProductName)
						.addComponent(lblProductNameEN)
						.addComponent(lblPrice)
				)
				.addGroup(layout.createParallelGroup()
						.addComponent(txtProductName)
						.addComponent(txtProductNameEN)
						.addComponent(txtPrice)
				)				
		);
		
	}

	
	// EditorPanel-Implementierung ---------------------------------------------
	@Override
	public void writeData() {
		this.product.setName(txtProductName.getText());
		this.product.setName_en(txtProductNameEN.getText());
		this.product.setPrice(Double.parseDouble(txtPrice.getText()));
	}
	
	@Override
	public DataEntity getData() {
		return product;
	}
	
	@Override
	public boolean validateData() {
		boolean dataValid = true;
		
		if(txtProductName.getText().equals("")) dataValid = false;
		if(dataValid && txtProductNameEN.getText().equals("")) dataValid = false;
		if(dataValid && txtPrice.getText().equals("")) dataValid = false;
		
		return dataValid;
	}

}
