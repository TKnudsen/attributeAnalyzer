package com.github.tknudsen.attributeAnalyzer.model.attributeCharacterization;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Objects;

import com.github.TKnudsen.ComplexDataObject.data.attributes.AttributeTypeAndParserDetector;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;

public class AttributeCharacterization {

	public static Entry<Class<Object>, IObjectParser<Object>> characterizeAttribute(String attribute,
			ComplexDataContainer complexDataContainer, AttributeTypeAndParserDetector detector) {

		Objects.requireNonNull(attribute);
		Objects.requireNonNull(complexDataContainer);
		Objects.requireNonNull(detector);

		Collection<Object> values = complexDataContainer.getAttributeValues(attribute).values();

		return detector.getAttributeTypeAndParserType(values);
	}

}
