package net.msites.kaffeekasse.ui.dialog.chartdialog;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JScrollPane;

import net.msites.kaffeekasse.data.DataContainer;
import net.msites.kaffeekasse.data.entities.Billing;
import net.msites.kaffeekasse.data.entities.BillingPosition;
import net.msites.kaffeekasse.data.entities.Consumption;
import net.msites.kaffeekasse.data.entities.Product;
import net.msites.kaffeekasse.ui.components.checkablejlist.AbstractCheckableListModel;
import net.msites.kaffeekasse.ui.components.checkablejlist.CheckableJList;
import net.msites.kaffeekasse.ui.components.checkablejlist.CheckableListCellRenderer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class TotalPerBillingChartTab extends ChartTab {
	
	private final CheckableJList lstBillings = new CheckableJList();
	private final List<Billing> billings = DataContainer.getInstance().getBillings();
	private final JCheckBox chkProducts = new JCheckBox("Produktanteile");
	
	public TotalPerBillingChartTab() {
		super(new Chart("Total pro Abrechnung", null));
		
		buildGui();
		buildChart();
	}
	
	private void buildGui() {
		lstBillings.setCellRenderer(new CheckableListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);
				
				Billing billing = (Billing)value;
				setText(billing.getTitle());
				
				return this;
			}
		});
		
		lstBillings.setModel(new AbstractCheckableListModel() {
			@Override
			public int getSize() {
				return billings.size();
			}
			
			@Override
			public Object getElementAt(int index) {
				return billings.get(index);
			}
			
			@Override
			public void setChecked(int index, boolean checked) {
				super.setChecked(index, checked);
				buildChart();
			}
		}, true);
		
		chkProducts.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				buildChart();
			}
		});
		
		JScrollPane scpBillings = new JScrollPane(lstBillings);
		
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
						.addGroup(layout.createSequentialGroup()
								.addComponent(scpBillings)
								.addComponent(chkProducts)
								)
						.addComponent(getChartCanvas())
						)
				);
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
						.addComponent(scpBillings, 160,160,160)
						.addComponent(chkProducts)
						)
				.addComponent(getChartCanvas())
				);
	}
	
	
	@Override
	public void buildChart() {
		JFreeChart chart = null;
		DefaultCategoryDataset dataset = null;
		boolean dataAvailable = false;
		
		/* Dataset erstellen: */
		dataset = new DefaultCategoryDataset();
		boolean legend = true;
		
		if(chkProducts.isSelected()) {
			for(Object object: lstBillings.getCheckedElements()) {
				Billing billing = (Billing)object;
				
				Map<Product, Double> totals = new HashMap<Product, Double>(10);
				double totalOtherCosts = 0;
				
				for(BillingPosition billingPosition: billing.getBillingPositions()) {
					for(Consumption consumption: billingPosition.getConsumptions()) {
						Product product = consumption.getProduct();
						
						Double currentTotal = totals.get(product);
						if(currentTotal != null) {
							currentTotal += billing.getBillingProductPrice(product).getPrice()
								* consumption.getAmount();
						} else {
							currentTotal = billing.getBillingProductPrice(product).getPrice()
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
					dataset.addValue(total, product.getName(), billing.getTitle());
				}
				
				dataset.addValue(totalOtherCosts, "Andere Kosten", billing.getTitle());
				
				dataAvailable = true;
			}
		} else {
			legend = false;
			
			for(Object object: lstBillings.getCheckedElements()) {
				Billing billing = (Billing)object;
				double total = 0;
				
				for(BillingPosition position: billing.getBillingPositions()) {
					total += position.calculateTotalConsumptions();
					total += position.calculateTotalOtherCosts();
				}
		
				dataset.addValue(total, "Total", billing.getTitle());
				dataAvailable = true;
			}
		}
		
		/* Chart erstellen: */
		if(dataAvailable) {
			chart = ChartFactory.createStackedBarChart(
					"",
					"Abrechnungen",
					"Einnahmen",
					dataset,
					PlotOrientation.VERTICAL,
					legend,
					false, true);
			chart.setAntiAlias(true);
			chart.setTextAntiAlias(true);
			chart.setBackgroundPaint(null);
		}
		
		getChart().setChartModel(chart);
		getChartCanvas().repaint();
	}

}
