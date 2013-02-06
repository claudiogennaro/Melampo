package it.cnr.isti.melampo.index.searching;

import it.cnr.isti.melampo.index.Parameters;
import it.cnr.isti.melampo.tools.Settings;
import it.cnr.isti.melampo.vir.bof.BoFSearcher;
import it.cnr.isti.melampo.vir.exceptions.VIRException;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
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

public class BOFSearcher implements MelampoSearcher {

	private static int m_topBoFQuery = -1;

	private static Searcher m_sBOF;

	private static BoFSearcher m_bofSearcher;
	private ScoreDoc[] m_hits;

	private static PerFieldAnalyzerWrapper m_wrapper;
	private static int m_retrieve = 1000;
	
	private IndexReader rBOF;
	
	private Analyzer analyzer = new WhitespaceAnalyzer();
	
	private String query;

	@Override
	public void OpenIndex(File propertyFile) throws IOException, VIRException {
		if (m_sBOF != null)
			return;

		Settings settings = new Settings(propertyFile);

		m_bofSearcher = new BoFSearcher(propertyFile);

		String indexPath = settings.getLuceneIndexPathBOF();
		if (indexPath != null) {
			m_topBoFQuery = settings.getTopBofQuery();

			Directory dir = FSDirectory.open(new File(indexPath));
			// Directory rd = new RAMDirectory(dir);
			// rBOF = IndexReader.open(rd,true);
			rBOF = IndexReader.open(dir, true);
			m_sBOF = new IndexSearcher(rBOF);
			System.out.println(indexPath + " open " + rBOF.numDocs() + " docs");
		}
		
		StandardAnalyzer sa = new StandardAnalyzer(Version.LUCENE_30);
		m_wrapper = new PerFieldAnalyzerWrapper(sa);
		m_wrapper.addAnalyzer(Parameters.BOFFIELD, analyzer);
	}

	@Override
	public String[][] getResults(int startFrom, int numElements)
			throws IOException {

		String[][] retval = new String[numElements][3];

		for (int i = 0; i < numElements; i++) {
			if (startFrom + i > m_hits.length - 1)
				break;

			float score = m_hits[startFrom + i].score;
			retval[i][0] = ((Float) score).toString();
			Document d = m_sBOF.doc(m_hits[startFrom + i].doc);
			retval[i][1] = d.get(Parameters.IDFIELD);
		}
		return retval;
	}

	@Override
	public String prepareQuery(String value, String field,
			boolean isQueryID) throws VIRException {
		query = m_bofSearcher.getBof(value,
						isQueryID, m_topBoFQuery);
		return query;
	}

	@Override
	public void query()	throws ParseException, IOException {
		BooleanClause.Occur[] flags = new BooleanClause.Occur[1];
		flags[0] = getOccur();

		String[] v = {query};
		String[] f = {Parameters.BOFFIELD};

		Query q = MultiFieldQueryParser.parse(Version.LUCENE_30, v,
				f, flags, m_wrapper);

		TopDocs td = null;
		System.out.println("Using standalone BOF index");
		td = m_sBOF.search(q, m_retrieve);
		m_hits = td.scoreDocs;
	}

	@Override
	public void reorderResults() {
		// TODO Auto-generated method stub

	}

	@Override
	public Searcher getIndex() {
		// TODO Auto-generated method stub
		return m_sBOF;
	}

	@Override
	public Occur getOccur() {
		return Occur.SHOULD;
	}

	@Override
	public IndexReader getIndexReader() {
		return rBOF;
	}

	@Override
	public Analyzer getAnalyzer() {
		return analyzer;
	}
}
