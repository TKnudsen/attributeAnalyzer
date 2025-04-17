package com.github.tknudsen.attributeAnalyzer.applications;

import java.util.ArrayList;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.model.transformations.dimensionalityReduction.DimensionalityReductionPipeline;
import com.github.TKnudsen.ComplexDataObject.model.transformations.dimensionalityReduction.IDimensionalityReduction;
import com.github.TKnudsen.DMandML.model.transformations.dimensionalityReduction.FastMDS;
import com.github.TKnudsen.DMandML.model.transformations.dimensionalityReduction.PCA;
import com.github.TKnudsen.DMandML.model.transformations.dimensionalityReduction.TSNE;
import com.github.TKnudsen.infoVis.view.viewTransformation.ViewTransformation.DimRed;

import de.javagl.nd.distance.tuples.d.DoubleTupleDistanceFunctions;

public class SDGDimensionalityReductionPipeline extends DimensionalityReductionPipeline<Object> {

	public SDGDimensionalityReductionPipeline(ComplexDataContainer container, DimRed dimRed) {
		super(container.primaryKeySet(), new SDGDescriptor(container));

		setQuantileNormalizationRatio(0.0);

		calculateDimRed(dimRed);
	}

	private void calculateDimRed(DimRed dimRed) {
		IDimensionalityReduction<NumericalFeatureVector, NumericalFeatureVector> dimensionalityReduction = null;

		switch (dimRed) {
		case PCA:
			dimensionalityReduction = new PCA(new ArrayList<>(getFeatureVectorsMap().values()), 2);
			break;
		case MDS:
			dimensionalityReduction = new FastMDS<>(new ArrayList<>(getFeatureVectorsMap().values()),
					DoubleTupleDistanceFunctions.euclidean(), 2, 100);
			break;
		case tSNE:
			dimensionalityReduction = new TSNE(new ArrayList<>(getFeatureVectorsMap().values()), 2, 15.0, 100);
			break;
		}

		dimensionalityReduction.calculateDimensionalityReduction();

		setDimensionalityReduction(dimensionalityReduction);
	}

}
