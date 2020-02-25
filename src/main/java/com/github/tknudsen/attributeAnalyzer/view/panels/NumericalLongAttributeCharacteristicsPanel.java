package com.github.tknudsen.attributeAnalyzer.view.panels;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JPanel;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.panels.boxplot.BoxPlotHorizontalCartPanel;
import com.github.TKnudsen.infoVis.view.panels.distribution1D.Distribution1DHorizontalPanel;
import com.github.TKnudsen.infoVis.view.panels.distribution1D.Distribution1DHorizontalPanels;

public class NumericalLongAttributeCharacteristicsPanel extends AttributeCharacteristicsPanel<Long> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NumericalLongAttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<Long> parser,
			Long missingValueIndicator) {
		super(values, parser, missingValueIndicator);
	}

	@Override
	protected void addContentToValueDistributionPanel() {
		Collection<Long> parsedValues = getParsedValues();

		if (parsedValues.size() == 0)
			return;

		StatisticsSupport dataStatistics = new StatisticsSupport(parsedValues);

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new GridLayout(2, 0));

		BoxPlotHorizontalCartPanel infoVisBoxPlotHorizontalPanel = new BoxPlotHorizontalCartPanel(dataStatistics);
		infoVisBoxPlotHorizontalPanel.setBackgroundColor(null);
		contentPanel.add(infoVisBoxPlotHorizontalPanel);

		List<Double> values = new ArrayList<>();
		for (Long i : getParsedValues())
			values.add(i.doubleValue());

		Distribution1DHorizontalPanel<Double> infoVisDistribution1DHorizontalPanel = Distribution1DHorizontalPanels
				.createForDoubles(values);
		infoVisDistribution1DHorizontalPanel.setBackgroundColor(null);
		contentPanel.add(infoVisDistribution1DHorizontalPanel);

		valueDistributionPanel.add(contentPanel, BorderLayout.CENTER);
	}

	@Override
	protected Class<Long> getClassType() {
		return Long.class;
	}

	@Override
	public String getName() {
		return "Numerical (discrete) Long properties";
	}

}
