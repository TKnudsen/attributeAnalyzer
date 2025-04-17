package com.github.tknudsen.attributeAnalyzer.applications;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVectors;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.Parsers;
import com.github.TKnudsen.ComplexDataObject.model.transformations.descriptors.IDescriptor;
import com.github.TKnudsen.ComplexDataObject.model.transformations.descriptors.numericalFeatures.INumericFeatureVectorDescriptor;

public class SDGDescriptor implements INumericFeatureVectorDescriptor<Object> {

	ComplexDataContainer container;

	SortedSet<String> attributes = new TreeSet<>();

	private Map<Object, NumericalFeatureVector> fvsMap = new HashMap<>();

	public SDGDescriptor(ComplexDataContainer container) {
		this.container = container;
	}

	@Override
	public List<NumericalFeatureVector> transform(Object primaryKey) {
		if (!fvsMap.containsKey(primaryKey)) {

			ComplexDataObject author = container.get(primaryKey);

			double[] featureVector = new double[17];
			for (int i = 1; i < 18; i++) {
				Double d = Parsers.parseDouble(author.getAttribute(Test1.ChatGPT_cont_ + i));
				if (d == null || Double.isNaN(d))
					featureVector[(i++) - 1] = 0.0;
				else if (d < 0.3)
					featureVector[(i++) - 1] = 0.0;
				else
					featureVector[(i++) - 1] = d.doubleValue();
			}

//			LinearNormalizationFunction normalization = new LinearNormalizationFunction(
//					new StatisticsSupport(featureVector));
//			for (int i = 0; i < 17; i++)
//				featureVector[i] = (double) normalization.apply(featureVector[i]);

			for (int i = 0; i < 17; i++)
				featureVector[i] = Math.min(5.0, featureVector[i]);

			NumericalFeatureVector fv = NumericalFeatureVectors.createNumericalFeatureVector(featureVector,
					"sdg contribution", "description");
			fv.add(Test1.authorID, primaryKey);

			fvsMap.put(primaryKey, fv);
		}

		return Arrays.asList(fvsMap.get(primaryKey));
	}

	@Override
	public List<NumericalFeatureVector> transform(List<Object> primaryKeys) {
		List<NumericalFeatureVector> fvs = new ArrayList<>();
		for (Object pk : primaryKeys)
			fvs.addAll(transform(pk));

		return fvs;
	}

	@Override
	public List<IDescriptor<Object, NumericalFeatureVector>> getAlternativeParameterizations(int count) {
		return new ArrayList<>();
	}

	@Override
	public String getName() {
		return "SDGDescriptor";
	}

	@Override
	public String getDescription() {
		return "SDGDescriptor";
	}

}
