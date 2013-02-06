package it.cnr.isti.melampo.index.searching;

import it.cnr.isti.melampo.index.Parameters;
import it.cnr.isti.melampo.tools.Settings;
import it.cnr.isti.melampo.vir.exceptions.VIRException;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class TextSearcher implements MelampoSearcher {

	private static Searcher m_sText = null;

	private ScoreDoc[] m_hits;

	private static PerFieldAnalyzerWrapper m_wrapper;
	private static int m_retrieve = 1000;

	private IndexReader rText;
	private Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
	private String query;

	public void OpenIndex(File propertyFile) throws IOException, VIRException {
		if (m_sText != null)
			return;

		Settings settings = new Settings(propertyFile);
		String indexPath = settings.getLuceneIndexPathMetadata();
		if (indexPath != null) {
			Directory dir = FSDirectory.open(new File(indexPath));
			rText = IndexReader.open(dir, true);
			m_sText = new IndexSearcher(rText);
			System.out
					.println(indexPath + " open " + rText.numDocs() + " docs");
		}
		m_wrapper = new PerFieldAnalyzerWrapper(analyzer);
	}

	public String prepareQuery(String value, String field,
			boolean isQueryID) {
		query = value;
		return query;
	}

	public void query()
			throws ParseException, IOException {
		BooleanClause.Occur[] flags = new BooleanClause.Occur[1];
		flags[0] = getOccur();

		String[] v = {query};
		String[] f = {Parameters.TEXT};

		Query q = MultiFieldQueryParser.parse(Version.LUCENE_30, v,	f, flags, m_wrapper);

		TopDocs td = null;

		System.out.println("Using standalone Metadata index");
		td = m_sText.search(q, m_retrieve);

		m_hits = td.scoreDocs;
	}

	public String[][] getResults(int startFrom, int numElements)
			throws IOException {

		String[][] retval = new String[numElements][3];

		for (int i = 0; i < numElements; i++) {
			if (startFrom + i > m_hits.length - 1)
				break;

			float score = m_hits[startFrom + i].score;
			retval[i][0] = ((Float) score).toString();
			Document d = m_sText.doc(m_hits[startFrom + i].doc);
			retval[i][1] = d.get(Parameters.IDFIELD);
		}
		return retval;
	}

	@Override
	public void reorderResults() {
	}

	@Override
	public Searcher getIndex() {
		return m_sText;
	}

	@Override
	public Occur getOccur() {
		return Occur.MUST;
	}

	@Override
	public IndexReader getIndexReader() {
		return rText;
	}

	@Override
	public Analyzer getAnalyzer() {
		return analyzer;
	}
}
