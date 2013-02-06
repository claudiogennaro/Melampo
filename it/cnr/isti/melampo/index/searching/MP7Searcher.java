package it.cnr.isti.melampo.index.searching;

import it.cnr.isti.melampo.index.CosineSimilarity;
import it.cnr.isti.melampo.index.Parameters;
import it.cnr.isti.melampo.index.SAPIRObjectFieldAdder;
import it.cnr.isti.melampo.tools.OrderTopKArrayList;
import it.cnr.isti.melampo.tools.Settings;
import it.cnr.isti.melampo.vir.exceptions.VIRException;
import it.cnr.isti.melampo.vir.sapir.SAPIRCLMetric;
import it.cnr.isti.melampo.vir.sapir.SAPIRCSMetric;
import it.cnr.isti.melampo.vir.sapir.SAPIREHMetric;
import it.cnr.isti.melampo.vir.sapir.SAPIRHTMetric;
import it.cnr.isti.melampo.vir.sapir.SAPIRParaMetric;
import it.cnr.isti.melampo.vir.sapir.SAPIRSCMetric;
import it.cnr.isti.melampo.vir.sapir.SapirReader;
import it.cnr.isti.melampo.vir.sapir.SapirSearcher;
import it.cnr.isti.vir.features.mpeg7.SAPIRObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

public class MP7Searcher implements MelampoSearcher {

	private static Searcher m_sMPG7 = null;

	private static SapirSearcher m_soSearcher;
	private ScoreDoc[] m_hits;

	private static PerFieldAnalyzerWrapper m_wrapper;
	private static int m_hitsPerPage = 100;
	private static int m_retrieve = 1000;

	private static SAPIRObjectFieldAdder m_sfaSC;
	private static SAPIRObjectFieldAdder m_sfaCS;
	private static SAPIRObjectFieldAdder m_sfaCL;
	private static SAPIRObjectFieldAdder m_sfaEH;
	private static SAPIRObjectFieldAdder m_sfaHT;

	private static int m_toppivsQ;
	private static int m_toppivsI;
	private static int m_nPivots;
	private static String m_PivFile;

	// for reordering purposes
	private static SAPIRObject[] m_so;
	private static SapirReader m_sreader;
	private static IndexReader rMPG7;
	private Analyzer analyzer = new WhitespaceAnalyzer();

	private SAPIRObject sq;
	private static SAPIRParaMetric sm;
	private List<String> queries = new ArrayList<String>();
	private List<String> fields = new ArrayList<String>();

	// for reordering
	private double clw = 0.0;
	private double csw = 0.0;
	private double ehw = 0.0;
	private double htw = 0.0;
	private double scw = 0.0;

	public void OpenIndex(File propertyFile) throws IOException, VIRException {

		if (m_sMPG7 != null)
			return;

		Settings settings = null;
		m_soSearcher = new SapirSearcher(propertyFile);

		m_sreader = new SapirReader(propertyFile);
		//m_so = m_sreader.getAllSapirObject();

		settings = new Settings(propertyFile);

		int Indexes = 0;

		String indexPath = settings.getLuceneIndexPathMPG7();
		if (indexPath != null) {
			Directory dir = FSDirectory.open(new File(indexPath));
			rMPG7 = IndexReader.open(dir, true);
			m_sMPG7 = new IndexSearcher(rMPG7);
			m_sMPG7.setSimilarity(new CosineSimilarity());
			System.out.println("mpeg-7 using cosine similarity");
			System.out
					.println(indexPath + " open " + rMPG7.numDocs() + " docs");
			Indexes++;
		}

		StandardAnalyzer sa = new StandardAnalyzer(Version.LUCENE_30);
		m_wrapper = new PerFieldAnalyzerWrapper(sa);
		m_wrapper.addAnalyzer(Parameters.MP7CS, analyzer);
		m_wrapper.addAnalyzer(Parameters.MP7SC, analyzer);
		m_wrapper.addAnalyzer(Parameters.MP7CL, analyzer);
		m_wrapper.addAnalyzer(Parameters.MP7EH, analyzer);
		m_wrapper.addAnalyzer(Parameters.MP7HT, analyzer);

		m_nPivots = settings.getnPivots();
		m_PivFile = settings.getPivotsPath();

		m_sfaSC = new SAPIRObjectFieldAdder(new SAPIRSCMetric());
		m_sfaSC.loadBinPivots(m_PivFile, m_nPivots);

		m_sfaCS = new SAPIRObjectFieldAdder(new SAPIRCSMetric());
		m_sfaCS.loadBinPivots(m_PivFile, m_nPivots);

		m_sfaCL = new SAPIRObjectFieldAdder(new SAPIRCLMetric());
		m_sfaCL.loadBinPivots(m_PivFile, m_nPivots);

		m_sfaEH = new SAPIRObjectFieldAdder(new SAPIREHMetric());
		m_sfaEH.loadBinPivots(m_PivFile, m_nPivots);

		m_sfaHT = new SAPIRObjectFieldAdder(new SAPIRHTMetric());
		m_sfaHT.loadBinPivots(m_PivFile, m_nPivots);
		m_toppivsQ = settings.getToppivsQ();

		m_toppivsI = settings.getToppivsI();
	}

	@Override
	public String prepareQuery(String value, String field, boolean isQueryID)
			throws VIRException {

		sq = null;

		sq = m_soSearcher.getSapirObject(value, isQueryID);
		String query = null;

		if (field.contentEquals(Parameters.MP7SC)) {
			query = m_sfaSC.metricObjectToStringQ(sq, m_toppivsQ, m_toppivsI);
			scw = 1.0;
		} else if (field.contentEquals(Parameters.MP7CS)) {
			query = m_sfaCS.metricObjectToStringQ(sq, m_toppivsQ, m_toppivsI);
			csw = 1.0;
		} else if (field.contentEquals(Parameters.MP7CL)) {
			query = m_sfaCL.metricObjectToStringQ(sq, m_toppivsQ, m_toppivsI);
			clw = 1.0;
		} else if (field.contentEquals(Parameters.MP7EH)) {
			query = m_sfaEH.metricObjectToStringQ(sq, m_toppivsQ, m_toppivsI);
			ehw = 1.0;
		} else if (field.contentEquals(Parameters.MP7HT)) {
			query = m_sfaHT.metricObjectToStringQ(sq, m_toppivsQ, m_toppivsI);
			htw = 1.0;
		}
		queries.add(query);
		fields.add(field);
		return query;
	}

	@Override
	public void query() throws ParseException, IOException {
		BooleanClause.Occur[] flags = new BooleanClause.Occur[queries.size()];
		flags[0] = getOccur();

		String[] v = new String[queries.size()];
		String[] f = new String[fields.size()];

		Query q = MultiFieldQueryParser.parse(Version.LUCENE_30,
				queries.toArray(v), fields.toArray(f), flags, m_wrapper);

		TopDocs td = null;

		System.out.println("Using standalone MPEG-7 single features index");
		td = m_sMPG7.search(q, m_retrieve);

		m_hits = td.scoreDocs;

		queries.clear();
		fields.clear();
	}

	@Override
	public void reorderResults() {
		ScoreDoc[] m_hitsNew = null;
		if (sq == null)
			return;
		System.out.println("reordering...");
		sm = new SAPIRParaMetric(clw, csw, ehw, htw, scw);

		int nresults = 0;
		if (m_hits.length < m_hitsPerPage)
			nresults = m_hits.length;
		else
			nresults = m_hitsPerPage;
		m_hitsNew = new ScoreDoc[nresults];
		OrderTopKArrayList knn = new OrderTopKArrayList(nresults);
		for (int i = 0; i < m_hits.length; i++) {
			int d = m_hits[i].doc;
			//SAPIRObject s = m_so[d];
			SAPIRObject s=null;
			try {
				s = m_sreader.getSapirObject(d);
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

		clw = 0.0;
		csw = 0.0;
		ehw = 0.0;
		htw = 0.0;
		scw = 0.0;
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
			d = m_sMPG7.doc(m_hits[startFrom + i].doc);
			retval[i][1] = d.get(Parameters.IDFIELD);
		}
		return retval;
	}

	@Override
	public Searcher getIndex() {
		return m_sMPG7;
	}

	@Override
	public Occur getOccur() {
		return Occur.SHOULD;
	}

	@Override
	public IndexReader getIndexReader() {
		return rMPG7;
	}

	@Override
	public Analyzer getAnalyzer() {
		return analyzer;
	}
}
