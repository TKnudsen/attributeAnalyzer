package com.github.tknudsen.attributeAnalyzer.view.views;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import com.github.TKnudsen.ComplexDataObject.data.attributes.AttributeTypeAndParserDetector;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.BooleanParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.DoubleParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IntegerParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.LongParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.StringParser;
import com.github.tknudsen.attributeAnalyzer.data.events.AttributeTypeDecisionActionEvent;
import com.github.tknudsen.attributeAnalyzer.view.panels.AttributeCharacteristicsPanel;
import com.github.tknudsen.attributeAnalyzer.view.panels.BooleanAttributeCharacteristicsPanel;
import com.github.tknudsen.attributeAnalyzer.view.panels.CategoricalAttributeCharacteristicsPanel;
import com.github.tknudsen.attributeAnalyzer.view.panels.NumericalContinuousAttributeCharacteristicsPanel;
import com.github.tknudsen.attributeAnalyzer.view.panels.NumericalIntegerAttributeCharacteristicsPanel;
import com.github.tknudsen.attributeAnalyzer.view.panels.NumericalLongAttributeCharacteristicsPanel;

public class AttributeTypeSelectionView extends JPanel implements AttributeTypeAndParserDetector, ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8364345034820702346L;

	private final String title;
	protected Collection<Object> values;
	private final boolean showRawValues;

	protected List<AttributeCharacteristicsPanel<?>> attributeCharacteristicsPanels;

	// return types
	private boolean decisionMade = false;
	private Class<?> classType;
	private IObjectParser<?> parser;

	public AttributeTypeSelectionView(String title) {
		this.title = title;
		this.showRawValues = true;
	}

	public AttributeTypeSelectionView(String title, Collection<Object> values) {
		this.title = title;
		this.values = Objects.requireNonNull(values);

		this.showRawValues = true;

		initialize();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e instanceof AttributeTypeDecisionActionEvent) {
			AttributeTypeDecisionActionEvent<?> event = (AttributeTypeDecisionActionEvent<?>) e;
			classType = event.getAttributeType();
			parser = event.getParser();

			decisionMade = true;

			System.out.println(
					"AttributeTypeSelectionView: attribute type decision detected: " + classType.getSimpleName());
		}
	}

	@Override
	public Class<?> getAttributeType(Collection<Object> values) {
		this.values = Objects.requireNonNull(values);

		initialize();

		repaint();
		revalidate();

		while (!decisionMade) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("AttributeTypeSelectionView.getAttributeType: leaving while loop: ");

		return classType;
	}

	@Override
	public <T> Entry<Class<T>, IObjectParser<T>> getAttributeTypeAndParserType(Collection<Object> values) {
		this.values = Objects.requireNonNull(values);

		initialize();

		repaint();
		revalidate();

		while (!decisionMade) {
			try {
				repaint();
				revalidate();

				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("AttributeTypeSelectionView.getAttributeParserType: leaving while loop: ");

		@SuppressWarnings("unchecked")
		Class<T> c = (Class<T>) classType;
		@SuppressWarnings("unchecked")
		IObjectParser<T> p = (IObjectParser<T>) parser;
		return new AbstractMap.SimpleEntry<Class<T>, IObjectParser<T>>(c, p);
	}

	/**
	 * can be triggered from an external source. Used, e.g., when a Frame is about
	 * to be closed, or if no decision can be made.
	 */
	public void forceNullReturn() {
		classType = null;
		parser = null;

		decisionMade = true;

		System.out.println("AttributeTypeSelectionView: null return forced by external trigger");
	}

	private final void initialize() {
		decisionMade = false;
		this.removeAll();

		initializeAttributeCharacteristicsPanels();

//		if (!isShowRawValues())
//			setLayout(new GridLayout(0, attributeCharacteristicsPanels.size()));
//		else
		setLayout(new GridLayout(0, attributeCharacteristicsPanels.size() + 1));

		JPanel valuesPanel = new JPanel(new BorderLayout());

		JPanel southPanel = new JPanel();
		southPanel.setLayout(new GridLayout(2, 1));

		// Copy attribute to clip board
		JButton clipboardButton = new JButton("Title to Clipboard");
		southPanel.add(clipboardButton, BorderLayout.SOUTH);
		clipboardButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Toolkit toolkit = Toolkit.getDefaultToolkit();
				Clipboard clipboard = toolkit.getSystemClipboard();
				StringSelection strSel = new StringSelection(title);
				clipboard.setContents(strSel, null);
			}
		});

		// Skip functionality
		JButton skipButton = new JButton("Skip Attribute");
		southPanel.add(skipButton, BorderLayout.SOUTH);
		skipButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				forceNullReturn();
			}
		});

		valuesPanel.add(southPanel, BorderLayout.SOUTH);

		if (isShowRawValues()) {
			valuesPanel.add(new JLabel("Raw Values"), BorderLayout.NORTH);

			StringBuilder builder = new StringBuilder();
			for (Object o : values)
				if (o != null)
					builder.append((o.toString() + ", "));

			JTextPane textPane = new JTextPane();
			textPane.setText(builder.toString());
			textPane.setBackground(attributeCharacteristicsPanels.get(0).getBackground());
			valuesPanel.add(textPane, BorderLayout.CENTER);

			valuesPanel.setBorder(BorderFactory.createEtchedBorder());
			add(valuesPanel);
		}

		for (AttributeCharacteristicsPanel<?> panel : attributeCharacteristicsPanels) {
			panel.setBorder(BorderFactory.createEtchedBorder());
			add(panel);
		}
	}

	protected void initializeAttributeCharacteristicsPanels() {
		attributeCharacteristicsPanels = new ArrayList<>();

		CategoricalAttributeCharacteristicsPanel categoricalPanel = new CategoricalAttributeCharacteristicsPanel(values,
				new StringParser(), "");
		categoricalPanel.addActionListener(this);
		attributeCharacteristicsPanels.add(categoricalPanel);

		BooleanAttributeCharacteristicsPanel booleanPanel = new BooleanAttributeCharacteristicsPanel(values,
				new BooleanParser());
		booleanPanel.addActionListener(this);
		attributeCharacteristicsPanels.add(booleanPanel);

		NumericalIntegerAttributeCharacteristicsPanel numericalDiscretePanel = new NumericalIntegerAttributeCharacteristicsPanel(
				values, new IntegerParser(), null);
		numericalDiscretePanel.addActionListener(this);
		attributeCharacteristicsPanels.add(numericalDiscretePanel);

		NumericalLongAttributeCharacteristicsPanel numericalDiscreteLongPanel = new NumericalLongAttributeCharacteristicsPanel(
				values, new LongParser(), null);
		numericalDiscreteLongPanel.addActionListener(this);
		attributeCharacteristicsPanels.add(numericalDiscreteLongPanel);

		DoubleParser doubleParser = new DoubleParser(true);

		NumericalContinuousAttributeCharacteristicsPanel numericalPanel = new NumericalContinuousAttributeCharacteristicsPanel(
				values, doubleParser, Double.NaN);
		numericalPanel.addActionListener(this);
		attributeCharacteristicsPanels.add(numericalPanel);
	}

	public boolean isShowRawValues() {
		return showRawValues;
	}

}
