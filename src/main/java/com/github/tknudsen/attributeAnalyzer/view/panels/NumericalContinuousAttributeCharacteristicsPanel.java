package com.github.tknudsen.attributeAnalyzer.view.panels;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.DoubleParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.panels.boxplot.BoxPlotHorizontalCartPanel;
import com.github.TKnudsen.infoVis.view.panels.distribution1D.Distribution1DHorizontalPanel;
import com.github.TKnudsen.infoVis.view.panels.distribution1D.Distribution1DHorizontalPanels;

public class NumericalContinuousAttributeCharacteristicsPanel extends AttributeCharacteristicsPanel<Double> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1617737257897086025L;

	JPanel contentPanel = new JPanel();

	public NumericalContinuousAttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<Double> parser,
			Double missingValueIndicator) {
		super(values, parser, missingValueIndicator);
	}

	@Override
	protected void addContentToValueDistributionPanel() {
		JCheckBox dotMeansThousandsCheckBox = new JCheckBox("Dot means Thousand", false);
		if (getParser() instanceof DoubleParser)
			dotMeansThousandsCheckBox.setSelected(((DoubleParser) getParser()).isDotMeansThousands());
		dotMeansThousandsCheckBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (getParser() instanceof DoubleParser) {
					((DoubleParser) getParser()).setDotMeansThousands(dotMeansThousandsCheckBox.isSelected());

					resetParsedValues();
					
					refreshContentPanel();
					
					repaint();
					revalidate();
				}
			}
		});
		valueDistributionPanel.add(dotMeansThousandsCheckBox, BorderLayout.NORTH);

		contentPanel = new JPanel();
		contentPanel.setLayout(new GridLayout(2, 0));

		refreshContentPanel();

		valueDistributionPanel.add(contentPanel, BorderLayout.CENTER);

		repaint();
		revalidate();
	}

	private void refreshContentPanel() {
		contentPanel.removeAll();

		Collection<Double> parsedValues = getParsedValues();

		if (parsedValues.size() == 0)
			return;

		StatisticsSupport dataStatistics = new StatisticsSupport(parsedValues);

		BoxPlotHorizontalCartPanel infoVisBoxPlotHorizontalPanel = new BoxPlotHorizontalCartPanel(dataStatistics);
		infoVisBoxPlotHorizontalPanel.setBackgroundColor(null);
		contentPanel.add(infoVisBoxPlotHorizontalPanel);

		Distribution1DHorizontalPanel<Double> infoVisDistribution1DHorizontalPanel = Distribution1DHorizontalPanels
				.createForDoubles(getParsedValues());
		infoVisDistribution1DHorizontalPanel.setBackgroundColor(null);
		contentPanel.add(infoVisDistribution1DHorizontalPanel);
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
