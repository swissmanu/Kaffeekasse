package net.msites.kaffeekasse.ui.mainframe.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import net.msites.guilibrary.toolbox.GUIImageManager;
import net.msites.kaffeekasse.data.DataContainer;
import net.msites.kaffeekasse.data.entities.Billing;
import net.msites.kaffeekasse.data.entities.BillingProductPrice;
import net.msites.kaffeekasse.data.entities.Consumer;
import net.msites.kaffeekasse.data.entities.Product;
import net.msites.kaffeekasse.ui.dialog.WaitDialog;
import net.msites.kaffeekasse.ui.mainframe.BillingTab;
import net.msites.kaffeekasse.ui.mainframe.KaffeekasseFrame;

// Actions -----------------------------------------------------------------
/**
 * Erstellt eine neue Abrechnung
 */
public class NewBillingAction extends AbstractAction {
	
	/**
	 * 
	 */
	private final KaffeekasseFrame kaffeekasseFrame;

	public NewBillingAction(KaffeekasseFrame kaffeekasseFrame) {
		super("Neue Abrechnung", GUIImageManager.getInstance().getImageIcon("table.png"));
		this.kaffeekasseFrame = kaffeekasseFrame;
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				Billing newBilling = new Billing("Neue Abrechnung", new Date());
				
				List<Consumer> consumers = new ArrayList<Consumer>();
				for(Consumer consumer: DataContainer.getInstance().getConsumers())
					if(consumer.getAddToNewBillings()) consumers.add(consumer);
				List<Product> products = NewBillingAction.this.kaffeekasseFrame.getProductColumns();
				List<BillingProductPrice> prices = new ArrayList<BillingProductPrice>();
				for(Product product: DataContainer.getInstance().getProducts())
					prices.add(new BillingProductPrice(newBilling, product));
				
				newBilling.setBillingProductPrices(prices);
				newBilling.setChanged(false);
				
				BillingTab editor = new BillingTab(newBilling, consumers, products);
				NewBillingAction.this.kaffeekasseFrame.addTab("< neue Abrechnung >", editor, true);
			}
		};
		
		WaitDialog wait = new WaitDialog(this.kaffeekasseFrame, "Neue Abrechnung", "Daten werden geladen... Bitte warten.", task);
		wait.setVisible(true);
	}
}