package com.github.tknudsen.attributeAnalyzer.applications;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import com.github.TKnudsen.ComplexDataObject.data.attributes.AttributeTypeAndParserDetector;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.tknudsen.attributeAnalyzer.view.views.AttributeTypeSelectionFrame;

public class AttributeCharacterization {

	public static Entry<Class<Object>, IObjectParser<Object>> characterizeAttribute(String attribute,
			ComplexDataContainer complexDataContainer, AttributeTypeAndParserDetector detector) {

		Objects.requireNonNull(attribute);
		Objects.requireNonNull(complexDataContainer);
		Objects.requireNonNull(detector);

		Collection<Object> values = complexDataContainer.getAttributeValues(attribute).values();

		return detector.getAttributeTypeAndParserType(values);
	}

	/**
	 * 
	 * @param tokens        outer list contains rows, inner list contains attributes
	 * @param headlineCount number of headlines containing attribute names and to be
	 *                      ignored for the interpretation task
	 * @return list
	 * @throws IOException e
	 */
	public static List<Entry<Class<Object>, IObjectParser<Object>>> interpreteData(List<List<String>> tokens,
			int headlineCount) throws IOException {

		AttributeTypeSelectionFrame attributeTypeSelecor = new AttributeTypeSelectionFrame(
				"Attribute Characterization");

		List<Entry<Class<Object>, IObjectParser<Object>>> attributes = new ArrayList<>();

		int attributeCount = 0;
		for (List<String> row : tokens)
			attributeCount = Math.max(attributeCount, row.size());

		for (int a = 0; a < attributeCount; a++) {
			Collection<Object> values = new ArrayList<>();

			for (int r = headlineCount; r < tokens.size(); r++)
				if (tokens.get(r).size() > a)
					values.add(tokens.get(r).get(a));

			if (headlineCount > 0)
				attributeTypeSelecor.setTitle("Attribute Characterization: " + tokens.get(0).get(a));

			attributes.add(attributeTypeSelecor.getAttributeTypeAndParserType(values));
		}

		attributeTypeSelecor.dispose();

		return attributes;
	}

}
