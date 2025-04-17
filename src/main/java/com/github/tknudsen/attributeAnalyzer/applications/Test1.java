package com.github.tknudsen.attributeAnalyzer.applications;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObjects;
import com.github.TKnudsen.ComplexDataObject.data.entry.EntryWithComparableKey;
import com.github.TKnudsen.ComplexDataObject.data.ranking.Ranking;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.Parsers;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.ComplexDataObject.model.tools.StringUtils;
import com.github.tknudsen.attributeAnalyzer.model.io.AttributeCharacterization;

public class Test1 {

	static final String dataFile = "data\\faculty_publications_with_authors_curated2";

	private static String author_ids = "author_ids";
	private static String author_names = "author_names";
	public static String ChatGPT_rel_ = "ChatGPT_rel_";
	public static String ChatGPT_cont_ = "ChatGPT_cont_";

	public static String authorID = "authorID";
	public static String authorName = "authorName";

	private static boolean subtractExpectationValue = true;
	private static boolean normalizeByAuthorsOnPaperCount = false;
	private static boolean normalizeByAuthorPublicationsCount = false;
	private static boolean relInsteadOfCont = false;

	private static double[] expectationValuesRel = null;
	private static double expectationValueRel = 0.0;
	private static double[] expectationValuesCont = null;
	private static double expectationValueCont = 0.0;

	public static void main(String[] args) throws IOException {
		ComplexDataContainer container = loadData();

		Map<String, List<ComplexDataObject>> byAuthors = toAuthors(container);

		List<ComplexDataObject> authorsStatistics = authorStatistics(byAuthors, subtractExpectationValue,
				normalizeByAuthorsOnPaperCount, normalizeByAuthorPublicationsCount);

		for (int i = 1; i < 18; i++) {
			Ranking<EntryWithComparableKey<Double, String>> rankAuthors = rankAuthors(authorsStatistics, i,
					relInsteadOfCont);

			printRanking(rankAuthors, i, 100, "data\\Authors for SDG " + i + ".txt");
		}
	}

	public static ComplexDataContainer loadData() throws IOException {
		List<ComplexDataObject> raw = AttributeCharacterization.parseData(dataFile, "\t", 3);

		ComplexDataContainer container = new ComplexDataContainer(raw, "publication_zora_id");

		// dummy call so
		computeExpectationValues(container);

		return container;
	}

	public static void computeExpectationValues(ComplexDataContainer container) {
		double[] averages = new double[17];
		double[] count = new double[17];
		for (ComplexDataObject cdo : container)
			for (int i = 1; i < 18; i++) {
				Double v = Parsers.parseDouble(cdo.getAttribute(Test1.ChatGPT_rel_ + i));
				if (v != null && !v.isNaN()) {
					averages[i - 1] += v;
					count[i - 1]++;
				}
			}

		for (int i = 1; i < 18; i++)
			averages[i - 1] /= (double) count[i - 1];

		expectationValuesRel = averages;
		expectationValueRel = MathFunctions.getMean(expectationValuesRel);

		averages = new double[17];
		count = new double[17];
		for (ComplexDataObject cdo : container)
			for (int i = 1; i < 18; i++) {
				Double v = Parsers.parseDouble(cdo.getAttribute(Test1.ChatGPT_cont_ + i));
				if (v != null && !v.isNaN()) {
					averages[i - 1] += v;
					count[i - 1]++;
				}
			}

		for (int i = 1; i < 18; i++)
			averages[i - 1] /= (double) count[i - 1];

		expectationValuesCont = averages;
		expectationValueCont = MathFunctions.getMean(expectationValuesCont);
	}

	public static Map<String, List<ComplexDataObject>> toAuthors(ComplexDataContainer container) {
		Map<String, List<ComplexDataObject>> byAuthors = new HashMap<String, List<ComplexDataObject>>();

		for (ComplexDataObject cdo : container) {
			List<ComplexDataObject> authors = extractAuthors(cdo);

			for (ComplexDataObject author : authors) {
				String aID = Parsers.parseString(author.getAttribute(authorID));

				if (!byAuthors.containsKey(aID))
					byAuthors.put(aID, new ArrayList<ComplexDataObject>());

				byAuthors.get(aID).add(author);
			}
		}

		return byAuthors;
	}

	public static List<ComplexDataObject> extractAuthors(ComplexDataObject cdo) {
		List<ComplexDataObject> authors = new ArrayList<ComplexDataObject>();

		String authorsString = Parsers.parseString(cdo.getAttribute(author_ids));
		String authorsNamesString = Parsers.parseString(cdo.getAttribute(author_names));

		List<String> aIDs = StringUtils.tokenize(authorsString, "|");
		List<String> aNames = StringUtils.tokenize(authorsNamesString, "|");

		if (aIDs.size() != aNames.size())
			throw new IllegalArgumentException("Problem in the authors data");

		for (int i = 0; i < aIDs.size(); i++) {
			String a = aIDs.get(i);
			String au = aNames.get(i);
			ComplexDataObject clone = ComplexDataObjects.clone(cdo);
			clone.add(authorID, a.trim());
			clone.add(authorName, au.replace("\"", "").trim());
			clone.removeAttribute(author_ids);
			clone.removeAttribute(author_names);

			clone.add("author count", aIDs.size());
			authors.add(clone);
		}

		return authors;
	}

	/**
	 * 
	 * @param byAuthors                          input data
	 * @param subtractExpectationValue           subtracts the overall average SDG
	 *                                           score per SDG. Avoids that noise
	 *                                           and random scores add up and become
	 *                                           meaningful if someone has published
	 *                                           sufficiently many publications.
	 * @param normalizeByAuthorsOnPaperCount     true if scores shall be divided by
	 *                                           author count to allow fair
	 *                                           comparison
	 * @param normalizeByAuthorPublicationsCount if the author statistics shall be
	 *                                           divided by the authors' pub count.
	 * @return
	 */
	public static List<ComplexDataObject> authorStatistics(Map<String, List<ComplexDataObject>> byAuthors,
			boolean subtractExpectationValue, boolean normalizeByAuthorsOnPaperCount,
			boolean normalizeByAuthorPublicationsCount) {

		List<ComplexDataObject> authors = new ArrayList<ComplexDataObject>();

		for (String aID : byAuthors.keySet()) {
			SortedSet<String> nameVariants = new TreeSet<String>();
			LinkedHashMap<Integer, Double> rels = new LinkedHashMap<Integer, Double>();
			for (int i = 1; i < 18; i++)
				rels.put(i, 0.0);
			LinkedHashMap<Integer, Double> conts = new LinkedHashMap<Integer, Double>();
			for (int i = 1; i < 18; i++)
				conts.put(i, 0.0);

			for (ComplexDataObject cdo : byAuthors.get(aID)) {
				int acount = Parsers.parseInteger(cdo.getAttribute("author count"));
				nameVariants.add(Parsers.parseString(cdo.getAttribute(authorName)));
				for (int i = 1; i < 18; i++) {
					Double rel = Parsers.parseDouble(cdo.getAttribute(ChatGPT_rel_ + i));

					if (subtractExpectationValue)
						rel = Math.max(0.0, rel - expectationValueRel);
					if (normalizeByAuthorsOnPaperCount)
						rel /= (double) acount;
					rels.put(i, rels.get(i) + rel);

					Double cont = Parsers.parseDouble(cdo.getAttribute(ChatGPT_cont_ + i));

					if (subtractExpectationValue)
						cont = Math.max(0.0, cont - expectationValueCont);
					if (normalizeByAuthorsOnPaperCount)
						cont /= (double) acount;
					conts.put(i, conts.get(i) + cont);
				}
			}

			if (normalizeByAuthorPublicationsCount)
				for (int i = 1; i < 18; i++) {
					rels.put(i, MathFunctions.round(rels.get(i) / Math.pow(byAuthors.get(aID).size(), 0.66), 3));
					conts.put(i, MathFunctions.round(conts.get(i) / Math.pow(byAuthors.get(aID).size(), 0.66), 3));
				}

			ComplexDataObject a = new ComplexDataObject(aID);
			a.add(authorID, aID);
			if (nameVariants.size() > 1)
				System.out.println(nameVariants.size());
			a.add(authorName, nameVariants.first());
			for (int i = 1; i < 18; i++) {
				a.add(ChatGPT_rel_ + i, MathFunctions.round(rels.get(i), 3));
				a.add(ChatGPT_cont_ + i, MathFunctions.round(conts.get(i), 3));
			}

			a.add(ChatGPT_rel_ + "SUM", MathFunctions.round(MathFunctions.getSum(rels.values(), true), 3));
			a.add(ChatGPT_cont_ + "SUM", MathFunctions.round(MathFunctions.getSum(conts.values(), true), 3));

			authors.add(a);
		}

		return authors;
	}

	/**
	 * 
	 * @param minRel                             filter that excludes authors with
	 *                                           less than the given value to the
	 *                                           sum of relations of the 17 SDGs.
	 * @param minCont                            filter that excludes authors with
	 *                                           less than the given value to the
	 *                                           sum of potential contributions of
	 *                                           the 17 SDGs.
	 * @param subtractExpectationValue           subtracts the overall average SDG
	 *                                           score per SDG. Avoids that noise
	 *                                           and random scores add up and become
	 *                                           meaningful if someone has published
	 *                                           sufficiently many publications.
	 * @param normalizeByAuthorsOnPaperCount     true if scores shall be divided by
	 *                                           author count to allow fair
	 *                                           comparison
	 * @param normalizeByAuthorPublicationsCount if the author statistics shall be
	 *                                           divided by the authors' pub count.
	 * @return
	 */
	public static ComplexDataContainer loadAuthorStatistics(double minRel, double minCont,
			boolean subtractExpectationValue, boolean normalizeByAuthorsOnPaperCount,
			boolean normalizeByAuthorPublicationsCount) {
		Map<String, List<ComplexDataObject>> byAuthors;
		try {
			byAuthors = Test1.toAuthors(Test1.loadData());
			List<ComplexDataObject> authorsStatistics = Test1.authorStatistics(byAuthors, subtractExpectationValue,
					normalizeByAuthorsOnPaperCount, normalizeByAuthorPublicationsCount);

			List<ComplexDataObject> filtered = new ArrayList<ComplexDataObject>();
			for (ComplexDataObject cdo : authorsStatistics)
				if (Parsers.parseDouble(cdo.getAttribute(ChatGPT_rel_ + "SUM")) >= minRel)
					if (Parsers.parseDouble(cdo.getAttribute(ChatGPT_cont_ + "SUM")) >= minCont)
						filtered.add(cdo);

			System.out.println("Test1.loadAuthorStatistics: identified " + authorsStatistics.size()
					+ " authors, filtering by rel and cont sum led to " + filtered.size() + " authors.");
			ComplexDataContainer container = new ComplexDataContainer(filtered, "authorID");

			return container;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 
	 * @param authorsStatistics
	 * @param SDG
	 * @param relInsteadOfCont
	 */
	public static Ranking<EntryWithComparableKey<Double, String>> rankAuthors(List<ComplexDataObject> authorsStatistics,
			int SDG, boolean relInsteadOfCont) {

		Ranking<EntryWithComparableKey<Double, String>> ranking = new Ranking<>();

		for (ComplexDataObject author : authorsStatistics) {
			String attribute = (relInsteadOfCont ? ChatGPT_rel_ : ChatGPT_cont_) + SDG;

			String name = Parsers.parseString(author.getAttribute(authorName));
			double score = Parsers.parseDouble(author.getAttribute(attribute));

			if (score > 0)
				ranking.add(new EntryWithComparableKey<Double, String>(score, name));
		}

		return ranking;
	}

	public static void printRanking(Ranking<EntryWithComparableKey<Double, String>> rankAuthors, int SDG, int maxCount,
			String fileName) {

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {

			int count = 1;
			System.out.println("RANKING FOR SDG " + SDG);
			writer.write("RANKING FOR SDG " + SDG);

			for (int r = rankAuthors.size() - 1; r >= 0; r--) {
				System.out.println("SDG " + SDG + "\tRank " + count + "\t" + rankAuthors.get(r).getValue() + "\tScore: "
						+ rankAuthors.get(r).getKey());
				writer.write("SDG " + SDG + "\tRank " + count + "\t" + rankAuthors.get(r).getValue() + "\tScore: "
						+ rankAuthors.get(r).getKey() + "\n");
				count++;
				if (count > maxCount)
					break;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
