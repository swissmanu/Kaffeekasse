package net.msites.kaffeekasse.ui.dialog.chartdialog;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JList;
import javax.swing.JScrollPane;

import net.msites.kaffeekasse.data.DataContainer;
import net.msites.kaffeekasse.data.entities.Billing;
import net.msites.kaffeekasse.data.entities.BillingPosition;
import net.msites.kaffeekasse.data.entities.Consumer;
import net.msites.kaffeekasse.ui.components.checkablejlist.AbstractCheckableListModel;
import net.msites.kaffeekasse.ui.components.checkablejlist.CheckableJList;
import net.msites.kaffeekasse.ui.components.checkablejlist.CheckableListCellRenderer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class TopConsumersChartTab extends ChartTab {
	
	private final CheckableJList lstBillings = new CheckableJList();
	private final List<Billing> billings = DataContainer.getInstance().getBillings();
	
	public TopConsumersChartTab() {
		super(new Chart("Konsumenten Top 10", null));
		
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
		DefaultCategoryDataset dataset = null;
		boolean dataAvailable = false;
		
		/* Dataset erstellen: */
		dataset = new DefaultCategoryDataset();
			
		// Charts erstellen:
		List<ChartsPosition> charts = new ArrayList<ChartsPosition>(30);
		for(Object object: lstBillings.getCheckedElements()) {
			Billing billing = (Billing)object;
			
			for(Consumer consumer: billing.getUsedConsumers()) {
				ChartsPosition chartsPosition = searchChartPosition(consumer, charts);
				if(chartsPosition == null) {
					chartsPosition = new ChartsPosition(consumer, 0);
					charts.add(chartsPosition);
				}
				
				BillingPosition billingPosition = billing.getBillingPosition(consumer);
				chartsPosition.total += billingPosition.calculateTotalConsumptions()
										+ billingPosition.calculateTotalOtherCosts();
				
				dataAvailable = true;
			}
		}
		
		// Dataset zusammenstellen:
		Collections.sort(charts);
		int i = 0;
		for(ChartsPosition pos: charts) {
			if(i >= 10) break;
			dataset.addValue(pos.total, "Umsatz", pos.consumer.getFullname());
			i++;
		}
		
		/* Chart erstellen: */
		if(dataAvailable) {
			chart = ChartFactory.createStackedBarChart(
					"",
					"Top 10",
					"Umsatz",
					dataset,
					PlotOrientation.HORIZONTAL,
					false,
					false, true);
			chart.setAntiAlias(true);
			chart.setTextAntiAlias(true);
			chart.setBackgroundPaint(null);
		}
		
		getChart().setChartModel(chart);
		getChartCanvas().repaint();
	}

	// Hilfsmethoden & -klassen-------------------------------------------------
	private ChartsPosition searchChartPosition(Consumer consumer, List<ChartsPosition> inList) {
		for(ChartsPosition pos: inList) {
			if(pos.consumer.equals(consumer)) return pos;
		}
		
		return null;
	}
	
	private class ChartsPosition implements Comparable<ChartsPosition> {
		public Consumer consumer;
		public double total;
		
		public ChartsPosition(Consumer consumer, double total) {
			this.consumer = consumer;
			this.total = total;
		}
		
		public int compareTo(ChartsPosition o) {
			return Double.compare(o.total, total);
		};
	}
	
	
	
	
}
