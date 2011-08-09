package net.msites.kaffeekasse.ui.dialog.chartdialog;

import org.jfree.chart.JFreeChart;

public class Chart {

	private String name = "";
	private JFreeChart chart = null;
	
	public Chart() {}
	
	public Chart(String name, JFreeChart chart) {
		this.name = name;
		this.chart = chart;
	}
	
	public String getName() {
		return name;
	}
	
	public JFreeChart getChart() {
		return chart;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setChartModel(JFreeChart chart) {
		this.chart = chart;
	}
	
}
