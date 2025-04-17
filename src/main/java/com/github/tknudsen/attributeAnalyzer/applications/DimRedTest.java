package com.github.tknudsen.attributeAnalyzer.applications;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.JB.colormaps.colorMaps1D.quantitative.AbstractColorMap1D;
import com.JB.colormaps.colorMaps1D.quantitative.impl.DarkGrayToOrangeColorMap;
import com.JB.colormaps.colorMaps2D.ColorMap2D;
import com.JB.colormaps.colorMaps2D.impl.TeulingFig3;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.featureVector.EuclideanDistanceMeasure;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.Parsers;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.DMandML.model.retrieval.kNN.KNN;
import com.github.TKnudsen.infoVis.view.frames.SVGFrame;
import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.TKnudsen.infoVis.view.panels.scatterplot.BivariateValueDistributionPanel;
import com.github.TKnudsen.infoVis.view.ui.NimbusUITools;
import com.github.TKnudsen.infoVis.view.viewTransformation.ViewTransformation.DimRed;

import de.javagl.common.ui.JSpinners;
import de.javagl.selection.SelectionEvent;
import de.javagl.selection.SelectionListener;
import de.javagl.selection.SelectionModel;
import de.javagl.selection.SelectionModels;

public class DimRedTest {

	private static AbstractColorMap1D colorMapBipolar = (AbstractColorMap1D) DarkGrayToOrangeColorMap.getInstance();
	private static int kNN = 25;

	private static ComplexDataContainer container = Test1.loadAuthorStatistics(0.8, 0.4, true, false, true);

	private static double colorScaler = 0.5;

	public static void main(String[] args) {

		NimbusUITools.switchToNimbus();

		ColorMap2D colorMap2D = new TeulingFig3(); // a bit slow

		SDGViewTransformation viewTransformation = new SDGViewTransformation(container, x -> colorMapping1D(x),
				(xx, y) -> colorMap2D.getColor(xx, y));

		SelectionModel<Object> selectionModel = SelectionModels.create();

		viewTransformation.setDimRedMethod(DimRed.tSNE);

		LinkedHashSet<String> smallMultiples = new LinkedHashSet<String>();

		for (int i = 1; i < 18; i++)
			smallMultiples.add(Test1.ChatGPT_cont_ + i);

		List<JPanel> panels = new ArrayList<>();
		for (String attribute : smallMultiples)
			panels.add(scatterplot(container, viewTransformation, attribute, selectionModel, pk -> getName(pk)));
		JPanel jPanel = new JPanel();
		SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(colorScaler, 0.01, 10.0, 0.01);
		JSpinner spinner = new JSpinner(spinnerNumberModel);
		JSpinners.setSpinnerDraggingEnabled(spinner, true);
		jPanel.add(spinner, BorderLayout.NORTH);
		jPanel.add(new JPanel(), BorderLayout.CENTER);
		panels.add(jPanel);
		SVGFrame frame = SVGFrameTools.dropSVGFramePanelMatrix(panels, "");
		spinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				colorScaler = Parsers.parseDouble(spinnerNumberModel.getValue());

				frame.revalidate();
				frame.repaint();
			}
		});
		
		Collection<NumericalFeatureVector> highD = new ArrayList<>();
		for (Object pk : container.primaryKeySet())
			highD.add(viewTransformation.getDimensionalityReductionPipeline().getFeatureVectorsMap().get(pk));
		KNN<NumericalFeatureVector> knnRetrievalHighD = new KNN<NumericalFeatureVector>(kNN,
				new EuclideanDistanceMeasure(), highD);
		Collection<NumericalFeatureVector> lowD = new ArrayList<>();
		for (Object pk : container.primaryKeySet())
			lowD.add(viewTransformation.getDimensionalityReductionPipeline().getLowDimFeatureVector(pk));
		KNN<NumericalFeatureVector> knnRetrievalLowD = new KNN<NumericalFeatureVector>(kNN,
				new EuclideanDistanceMeasure(), lowD);

		String selected = "130203";// "US8552441094";//"US67066G1040"

		// debugging
		ComplexDataObject author = container.get(selected);
		if (author != null) {
			List<Entry<NumericalFeatureVector, Double>> neighbors = knnRetrievalHighD.retrieveNeighbors(
					viewTransformation.getDimensionalityReductionPipeline().getFeatureVectorsMap().get(selected));
			System.out.println(authorString(author, container::isNumeric, true));
			printkNNResult(container, neighbors);
			neighbors = knnRetrievalLowD.retrieveNeighbors(
					viewTransformation.getDimensionalityReductionPipeline().getLowDimFeatureVector(selected));
			System.out.println(authorString(author, container::isNumeric, true));
			printkNNResult(container, neighbors);
		}

		selectionModel.setSelection(Arrays.asList(selected));

		selectionModel.addSelectionListener(new SelectionListener<Object>() {

			@Override
			public void selectionChanged(SelectionEvent<Object> selectionEvent) {
				for (Object pk : selectionEvent.getSelectionModel().getSelection()) {
					ComplexDataObject author = container.get(pk);

					String s = authorString(author, container::isNumeric, true);
					System.err.println(s);
					System.out.println("---");
				}

				for (Object pk : selectionEvent.getSelectionModel().getSelection()) {
					System.err.println("kNN HD Retrieval for author "
							+ authorString(container.get(pk), container::isNumeric, true));
					List<Entry<NumericalFeatureVector, Double>> neighbors = knnRetrievalHighD.retrieveNeighbors(
							viewTransformation.getDimensionalityReductionPipeline().getFeatureVectorsMap().get(pk));

					printkNNResult(container, neighbors);
					break;
				}

				for (Object pk : selectionEvent.getSelectionModel().getSelection()) {
					System.err.println("kNN 2D Retrieval for author "
							+ authorString(container.get(pk), container::isNumeric, true));
					List<Entry<NumericalFeatureVector, Double>> neighbors = knnRetrievalLowD.retrieveNeighbors(
							viewTransformation.getDimensionalityReductionPipeline().getLowDimFeatureVector(pk));

					printkNNResult(container, neighbors);
					break;
				}
			}
		});

		System.out.println();
	}

	private static String authorString(ComplexDataObject author, Function<String, Boolean> isNumeric,
			boolean printSectorVector) {
		String s = getName(author.getAttribute(Test1.authorID));

		s += "\t";
		if (printSectorVector) {
			for (int i = 1; i < 18; i++)
				s += ("[" + i + "]:" + author.getAttribute(Test1.ChatGPT_cont_ + i) + "\t");
		}

		return s;
	}

	private static void printkNNResult(ComplexDataContainer container,
			List<Entry<NumericalFeatureVector, Double>> neighbors) {

		for (Entry<NumericalFeatureVector, Double> element : neighbors) {
			String ID = element.getKey().getAttribute(Test1.authorID).toString();
			System.out.println(MathFunctions.round(element.getValue(), 3) + ": "
					+ authorString(container.get(ID), container::isNumeric, true));
		}
	}

	private static String getName(Object pk) {
		return pk == null ? null : String.valueOf(container.get(pk).getAttribute(Test1.authorName));
	}

	private static BivariateValueDistributionPanel<Object> scatterplot(ComplexDataContainer container,
			SDGViewTransformation viewTransformation, String attribute, SelectionModel<Object> selectionModel,
			Function<Object, String> toNameFunction) {

		BivariateValueDistributionPanel<Object> panel = new BivariateValueDistributionPanel<Object>(
				new ArrayList<Object>(container.primaryKeySet()), x -> viewTransformation.mapToXPosition(x),
				y -> viewTransformation.mapToYPosition(y), selectionModel, attribute);

		panel.getScatterplot().setToolTipMapping(toNameFunction);
		Function<Object, Paint> f = new Function<Object, Paint>() {

			@Override
			public Paint apply(Object pk) {
				ComplexDataObject cdo = container.get(pk);
				Double d = Parsers.parseDouble(cdo.getAttribute(attribute));
				if (d == null || Double.isNaN(d))
					return Color.DARK_GRAY;
				return colorMapBipolar.apply((float) Math.min(1.0, d.floatValue() * colorScaler));
			}
		};
		panel.getScatterplot().setColorEncodingFunction(f);

		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		return panel;
	}

	public static Color colorMapping1D(float value) {
		return colorMapBipolar.getColor(value);
	}

}
