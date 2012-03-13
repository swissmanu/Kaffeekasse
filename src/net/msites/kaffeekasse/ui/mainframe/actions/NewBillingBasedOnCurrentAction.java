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
import net.msites.kaffeekasse.data.entities.BillingPosition;
import net.msites.kaffeekasse.data.entities.BillingProductPrice;
import net.msites.kaffeekasse.data.entities.Consumer;
import net.msites.kaffeekasse.data.entities.DataEntity;
import net.msites.kaffeekasse.data.entities.OtherCost;
import net.msites.kaffeekasse.data.entities.Product;
import net.msites.kaffeekasse.ui.dialog.WaitDialog;
import net.msites.kaffeekasse.ui.mainframe.BillingTab;
import net.msites.kaffeekasse.ui.mainframe.KaffeekasseFrame;

/**
 * Erstellt aufgrund der aktuellen Abrechnung eine neue.<br/>
 * Wurde eine Person nicht als Bezahlt markiert, wird der entsprechende
 * Betrag auf die neue Abrechnung übernommen.
 */
public class NewBillingBasedOnCurrentAction extends AbstractAction {
	
	/**
	 * 
	 */
	private final KaffeekasseFrame kaffeekasseFrame;

	public NewBillingBasedOnCurrentAction(KaffeekasseFrame kaffeekasseFrame) {
		super("Neue Abrechnung (basierend auf aktueller)", GUIImageManager.getInstance().getImageIcon("table_lightning.png"));
		this.kaffeekasseFrame = kaffeekasseFrame;
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				DataEntity data = NewBillingBasedOnCurrentAction.this.kaffeekasseFrame.getCurrentTab().getData();
				if(data != null && data instanceof Billing) {
					Billing oldBilling = (Billing)data;
					Billing newBilling = new Billing("Neue Abrechnung", new Date());
					
					List<Consumer> consumers = new ArrayList<Consumer>();
					for(Consumer consumer: DataContainer.getInstance().getConsumers())
						if(consumer.getAddToNewBillings()) consumers.add(consumer);
					List<Product> products = NewBillingBasedOnCurrentAction.this.kaffeekasseFrame.getProductColumns();
					List<BillingProductPrice> prices = new ArrayList<BillingProductPrice>();
					for(Product product: DataContainer.getInstance().getProducts())
						prices.add(new BillingProductPrice(newBilling, product));
					
					newBilling.setBillingProductPrices(prices);
					
					for(BillingPosition oldBillingPosition: oldBilling.getBillingPositions()) {
						if(!oldBillingPosition.isPaid()) {
							BillingPosition newBillingPosition = new BillingPosition();
							newBillingPosition.setBilling(newBilling);
							newBillingPosition.setConsumer(oldBillingPosition.getConsumer());
							
							Double oldConsumptionsTotal = oldBillingPosition.calculateTotalConsumptions();
							
							for(OtherCost oldOtherCost: oldBillingPosition.getOtherCosts()) {
								OtherCost newOtherCost = new OtherCost(
										newBillingPosition,
										oldOtherCost.getText(),
										oldOtherCost.getAmount());
								newBillingPosition.addOtherCost(newOtherCost);
							}
							
							if(oldConsumptionsTotal > 0d) {
								OtherCost oldConsumptationsOtherCost = new OtherCost(
										newBillingPosition,
										"Austehende Konsumationen von \"" + oldBilling.getTitle() + "\"",
										oldConsumptionsTotal);
								newBillingPosition.addOtherCost(oldConsumptationsOtherCost);
							}
							
							if(newBillingPosition.getOtherCosts().size() > 0) {
								newBilling.addBillingPosition(newBillingPosition);
							}
						}
					}
					
					newBilling.setChanged(false);
					NewBillingBasedOnCurrentAction.this.kaffeekasseFrame.addTab("< neue Abrechnung >", new BillingTab(newBilling, consumers, products), true);
				}
			}
		};
		
		WaitDialog waitDialog = new WaitDialog(
				this.kaffeekasseFrame,
				"Neue Abrechnung",
				"Neue Abrechnung wird erstellt... Bitte warten.",
				task);
		waitDialog.setVisible(true);
	}
}