package com.github.tknudsen.attributeAnalyzer.applications;

import java.util.ArrayList;
import java.util.Collection;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.DoubleParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.tknudsen.attributeAnalyzer.view.panels.NumericalContinuousAttributeCharacteristicsPanel;

public class DoubleAttributeCharacteristicsTester {

	public static void main(String[] args) {

		Collection<Object> values = new ArrayList<Object>();

		values.add("");
		values.add(true);
		values.add("true");
		values.add(1);
		values.add(0);
		values.add(1.3);
		values.add(5.5);
		values.add(4.4);
		values.add(1.8);
		values.add(2.5);
		values.add(3);
		values.add(3);

		for (int i = 0; i < 100; i++)
			values.add(Math.random() * 3);

		values.add(Double.NaN);

		IObjectParser<Double> parser = new DoubleParser();

		NumericalContinuousAttributeCharacteristicsPanel panel = new NumericalContinuousAttributeCharacteristicsPanel(
				values, parser, Double.NaN);

		SVGFrameTools.dropSVGFrame(panel, "Test", 600, 600);

	}

}
