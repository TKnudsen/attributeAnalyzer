package com.github.tknudsen.attributeAnalyzer.view.panels;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Collection;

import javax.swing.JPanel;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.panels.boxplot.BoxPlotHorizontalCartPanel;
import com.github.TKnudsen.infoVis.view.panels.distribution1D.Distribution1DHorizontalPanel;

public class NumericalContinuousAttributeCharacteristicsPanel extends AttributeCharacteristicsPanel<Double> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1617737257897086025L;

	public NumericalContinuousAttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<Double> parser,
			Double missingValueIndicator) {
		super(values, parser, missingValueIndicator);
	}

	@Override
	protected void addContentToValueDistributionPanel() {
		Collection<Double> parsedValues = getParsedValues();

		if (parsedValues.size() == 0)
			return;

		StatisticsSupport dataStatistics = new StatisticsSupport(parsedValues);

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new GridLayout(2, 0));

		BoxPlotHorizontalCartPanel infoVisBoxPlotHorizontalPanel = new BoxPlotHorizontalCartPanel(dataStatistics);
		infoVisBoxPlotHorizontalPanel.setBackgroundColor(null);
		contentPanel.add(infoVisBoxPlotHorizontalPanel);

		Distribution1DHorizontalPanel infoVisDistribution1DHorizontalPanel = new Distribution1DHorizontalPanel(
				getParsedValues());
		infoVisDistribution1DHorizontalPanel.setBackgroundColor(null);
		contentPanel.add(infoVisDistribution1DHorizontalPanel);

		valueDistributionPanel.add(contentPanel, BorderLayout.CENTER);
	}

	@Override
	protected Class<Double> getClassType() {
		return Double.class;
	}

	@Override
	public String getName() {
		return "Numerical (continuous) Properties";
	}

	@Override
	protected boolean testForMissingValue(Double missingValueIndicator, Double value) {
		if (Double.isNaN(missingValueIndicator))
			if (Double.isNaN(value))
				return true;
			else
				return false;
		else
			return missingValueIndicator == value;
	}

}
