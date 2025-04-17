package com.github.tknudsen.attributeAnalyzer.applications;

import java.awt.Color;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.model.transformations.dimensionalityReduction.DimensionalityReductionPipeline;
import com.github.TKnudsen.infoVis.view.viewTransformation.ViewTransformation;

public class SDGViewTransformation extends ViewTransformation<Object> {

	ComplexDataContainer container;

	public SDGViewTransformation(ComplexDataContainer container, Function<Float, Color> colorMapping,
			BiFunction<Float, Float, Color> colorMapping2D) {
		super(colorMapping, colorMapping2D);

		this.container = container;
	}

	@Override
	public Color mapToColor(Object scoringObject) {
		if (scoringObject == null)
			return Color.BLACK;

		if (colorLookup.containsKey(scoringObject))
			return colorLookup.get(scoringObject);

		DimensionalityReductionPipeline<Object> dimRed = getDimensionalityReductionPipeline();

		if (dimRed == null)
			return Color.BLACK;

		Double mapToXPosition = mapToXPosition(scoringObject);
		Double mapToYPosition = mapToYPosition(scoringObject);
		if (mapToXPosition != null || mapToYPosition != null) {
			colorLookup.put(scoringObject, colorMapping2D(mapToXPosition.floatValue(), mapToYPosition.floatValue()));
		} else
			colorLookup.put(scoringObject, Color.CYAN);

		return colorLookup.get(scoringObject);
	}

	@Override
	protected void refreshDimensionalityReductionPipeline() {
		dimensionalityReductionPipeline = null;
		colorLookup.clear();

		dimensionalityReductionPipeline = new SDGDimensionalityReductionPipeline(container, DimRed.MDS);
	}

}
