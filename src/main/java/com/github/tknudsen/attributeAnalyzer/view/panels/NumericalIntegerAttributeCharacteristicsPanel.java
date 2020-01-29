package com.github.tknudsen.attributeAnalyzer.view.panels;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JPanel;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.panels.boxplot.BoxPlotHorizontalCartPanel;
import com.github.TKnudsen.infoVis.view.panels.distribution1D.Distribution1DHorizontalPanel;

public class NumericalIntegerAttributeCharacteristicsPanel extends AttributeCharacteristicsPanel<Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1617737257897086025L;

	public NumericalIntegerAttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<Integer> parser,
			Integer missingValueIndicator) {
		super(values, parser, missingValueIndicator);
	}

	@Override
	protected void addContentToValueDistributionPanel() {
		Collection<Integer> parsedValues = getParsedValues();

		if (parsedValues.size() == 0)
			return;

		StatisticsSupport dataStatistics = new StatisticsSupport(parsedValues);

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new GridLayout(2, 0));

		BoxPlotHorizontalCartPanel infoVisBoxPlotHorizontalPanel = new BoxPlotHorizontalCartPanel(dataStatistics);
		infoVisBoxPlotHorizontalPanel.setBackgroundColor(null);
		contentPanel.add(infoVisBoxPlotHorizontalPanel);

		Collection<Double> values = new ArrayList<>();
		for (Integer i : getParsedValues())
			values.add(i.doubleValue());

		Distribution1DHorizontalPanel infoVisDistribution1DHorizontalPanel = new Distribution1DHorizontalPanel(values);
		infoVisDistribution1DHorizontalPanel.setBackgroundColor(null);
		contentPanel.add(infoVisDistribution1DHorizontalPanel);

		valueDistributionPanel.add(contentPanel, BorderLayout.CENTER);
	}

	@Override
	protected Class<Integer> getClassType() {
		return Integer.class;
	}

	@Override
	public String getName() {
		return "Numerical (discrete, integer) Properties";
	}

}
