package net.msites.kaffeekasse.ui.dialog.chartdialog;

import java.awt.Component;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.msites.guilibrary.toolbox.GUIImageManager;
import net.msites.kaffeekasse.data.DataContainer;
import net.msites.kaffeekasse.data.entities.Billing;
import net.msites.kaffeekasse.data.entities.BillingPosition;
import net.msites.kaffeekasse.data.entities.Consumption;
import net.msites.kaffeekasse.data.entities.Product;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

public class ProductRevenueChartTab extends ChartTab {
	
	private final JList lstBillings = new JList();
	private final List<Billing> billings = DataContainer.getInstance().getBillings();
	
	public ProductRevenueChartTab() {
		super(new Chart("Umsatzanteile pro Produkt", null));
		
		buildGui();
		
		if(lstBillings.getModel().getSize() > 0) lstBillings.setSelectedIndex(0);
		else buildChart();
	}
	
	private void buildGui() {
		lstBillings.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);
				
				if(value != null) {
					Billing billing = (Billing)value;
					setText(billing.getTitle());
					setIcon(GUIImageManager.getInstance().getImageIcon("table.png"));
				} else {
					setText("");
					setIcon(null);
				}
				return this;
			}
		});
		
		lstBillings.setModel(new AbstractListModel() {
			@Override
			public int getSize() {
				return billings.size();
			}
			
			@Override
			public Object getElementAt(int index) {
				return billings.get(index);
			}
		});
		
		lstBillings.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				buildChart();
			}
		});
		
		lstBillings.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scpBillings = new JScrollPane(lstBillings);
		
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
						.addComponent(scpBillings)
						.addComponent(getChartCanvas())
						)
				);
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(scpBillings, 160,160,160)
				.addComponent(getChartCanvas())
				);
	}
	
	
	@Override
	public void buildChart() {
		JFreeChart chart = null;
		Billing selectedBilling = null;
		
		if(lstBillings.getSelectedValue() != null) {
			selectedBilling = (Billing)lstBillings.getSelectedValue();
			DefaultPieDataset dataset = new DefaultPieDataset();
			
			/* Dataset erstellen: */
			Map<Product, Double> totals = new HashMap<Product, Double>(10);
			double totalOtherCosts = 0;
			
			for(BillingPosition billingPosition: selectedBilling.getBillingPositions()) {
				for(Consumption consumption: billingPosition.getConsumptions()) {
					Product product = consumption.getProduct();
					
					Double currentTotal = totals.get(product);
					if(currentTotal != null) {
						currentTotal += selectedBilling.getBillingProductPrice(product).getPrice()
							* consumption.getAmount();
					} else {
						currentTotal = selectedBilling.getBillingProductPrice(product).getPrice()
							* consumption.getAmount();
					}
					totals.put(product, currentTotal);
				}
				
				totalOtherCosts += billingPosition.calculateTotalOtherCosts();
			}
			
			Set<Product> products = totals.keySet();
			Iterator<Product> iter = products.iterator();
			while(iter.hasNext()) {
				Product product = iter.next();
				double total = totals.get(product);
				String text = product.getName() + " (" + NumberFormat.getCurrencyInstance().format(total) + ")";
				
				dataset.setValue(text, total);
			}
			
			dataset.setValue(
					"Andere Kosten (" + NumberFormat.getCurrencyInstance().format(totalOtherCosts) + ")",
					totalOtherCosts);
			
			chart = ChartFactory.createPieChart(
					selectedBilling.getTitle(),
					dataset,
					false,
					false,
					false);
			chart.setAntiAlias(true);
			chart.setTextAntiAlias(true);
			chart.setBackgroundPaint(null);
		}
		
		getChart().setChartModel(chart);
		getChartCanvas().repaint();
	}

}
