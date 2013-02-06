package it.cnr.isti.melampo.index.searching;

import it.cnr.isti.melampo.index.CosineSimilarity;
import it.cnr.isti.melampo.index.Parameters;
import it.cnr.isti.melampo.index.LireObjectFieldAdder;
import it.cnr.isti.melampo.index.settings.LireSettings;
import it.cnr.isti.melampo.tools.OrderTopKArrayList;
import it.cnr.isti.melampo.vir.exceptions.VIRException;
import it.cnr.isti.melampo.vir.lire.LireReader;
import it.cnr.isti.melampo.vir.lire.LireSearcher;
import it.cnr.isti.vir.features.mpeg7.LireObject;
import it.cnr.isti.vir.similarity.metric.LireMetric;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class LireMP7CSearcher implements MelampoSearcher {

	private static IndexSearcher m_sMPG7C;

	private static LireSearcher m_soSearcher;
	private ScoreDoc[] m_hits;

	private static PerFieldAnalyzerWrapper m_wrapper;
	private static int m_hitsPerPage = 100;
	private static int m_retrieve = 100;

	private static LireObjectFieldAdder m_sfaALL;

	private static int m_toppivsQ;
	private static int m_toppivsI;
	private static int m_nPivots;
	private static String m_PivFile;

	// for reordering purposes
	private static LireObject[] m_so;
	private static LireReader m_sreader;

	private LireObject sq;
	private LireMetric sm;
	private IndexReader rMPG7C;
	
	private Analyzer analyzer = new WhitespaceAnalyzer();
	private String query;

	@Override
	public void OpenIndex(File propertyFile) throws IOException, VIRException {
		
		System.out.println("index conf file: " + propertyFile.getAbsolutePath());

		if (m_sMPG7C != null)
			return;

		LireSettings settings = new LireSettings(propertyFile);

		m_soSearcher = new LireSearcher(settings);

		m_sreader = new LireReader(settings);
		//m_so = m_sreader.getAllSapirObject();

		String indexPath = settings.getLuceneIndexPathLire();
		if (indexPath != null) {
			Directory dir = FSDirectory.open(new File(indexPath));
			rMPG7C = IndexReader.open(dir, true);
			m_sMPG7C = new IndexSearcher(rMPG7C);
			m_sMPG7C.setSimilarity(new CosineSimilarity());
			System.out.println("mpeg-7 Pre-Combined using cosine similarity");
			System.out.println(indexPath + " open " + rMPG7C.numDocs()
					+ " docs");
		}

		StandardAnalyzer sa = new StandardAnalyzer(Version.LUCENE_30);
		m_wrapper = new PerFieldAnalyzerWrapper(sa);
		m_wrapper.addAnalyzer(Parameters.LIRE_MP7ALL, analyzer);

		m_nPivots = settings.getnPivots();
		m_PivFile = settings.getPivotsPath();

		m_sfaALL = new LireObjectFieldAdder(new LireMetric());
		m_sfaALL.loadBinPivots(m_PivFile, m_nPivots);

		m_toppivsQ = settings.getToppivsQ();

		m_toppivsI = settings.getToppivsI();

		// for reordering
		double clw = 1.0;
		double csw = 1.0;
		double ehw = 1.0;
		double htw = 1.0;
		double scw = 1.0;
		sm = new LireMetric();
	}

	@Override
	public String prepareQuery(String value, String field,
			boolean isQueryID) throws VIRException {
		sq = m_soSearcher.getObject(value, isQueryID);
		query = m_sfaALL.metricObjectToStringQ(sq, m_toppivsQ,
					m_toppivsI);
		return query;
	}

	@Override
	public void query()
			throws ParseException, IOException {
		BooleanClause.Occur[] flags = new BooleanClause.Occur[1];
		flags[0] = getOccur();

		String[] v = {query};
		String[] f = {Parameters.LIRE_MP7ALL};

		Query q = MultiFieldQueryParser.parse(Version.LUCENE_30, v,
				f, flags, m_wrapper);

		TopDocs td = null;

		System.out.println("Using standalone MPEG-7 combined features index");
		td = m_sMPG7C.search(q, m_retrieve);

		m_hits = td.scoreDocs;
	}
	
	@Override
	public void reorderResults() {
		ScoreDoc[] m_hitsNew = null;
		if (sq == null)
			return;
		int nresults = 0;
		if (m_hits.length < m_hitsPerPage)
			nresults = m_hits.length;
		else
			nresults = m_hitsPerPage;
		m_hitsNew = new ScoreDoc[nresults];
		OrderTopKArrayList knn = new OrderTopKArrayList(nresults);
		for (int i = 0; i < m_hits.length; i++) {
			int d = m_hits[i].doc;
			LireObject s=null;
			try {
				s = m_sreader.getObject(d);
			} catch (VIRException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			knn.insert(sm.distance(sq, s), i);
		}
		for (int i = 0; i < nresults; i++) {
			m_hitsNew[i] = m_hits[knn.get(i)];
		}
		m_hits = m_hitsNew;
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
			Document d = null;
			d = m_sMPG7C.doc(m_hits[startFrom + i].doc);
			retval[i][1] = d.get(Parameters.IDFIELD);
		}
		return retval;
	}

	@Override
	public Searcher getIndex() {
		return m_sMPG7C;
	}

	@Override
	public Occur getOccur() {
		return Occur.SHOULD;
	}

	@Override
	public IndexReader getIndexReader() {
		return rMPG7C;
	}

	@Override
	public Analyzer getAnalyzer() {
		return analyzer;
	}

}
