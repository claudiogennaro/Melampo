package it.cnr.isti.melampo.index;

import it.cnr.isti.melampo.tools.OrderTopKArrayList;
import it.cnr.isti.melampo.tools.Settings;
import it.cnr.isti.melampo.vir.bof.BoFReader;
import it.cnr.isti.melampo.vir.bof.BoFSearcher;
import it.cnr.isti.melampo.vir.exceptions.BoFException;
import it.cnr.isti.melampo.vir.exceptions.VIRException;
import it.cnr.isti.melampo.vir.sapir.SAPIRCLMetric;
import it.cnr.isti.melampo.vir.sapir.SAPIRCSMetric;
import it.cnr.isti.melampo.vir.sapir.SAPIRColorMetric;
import it.cnr.isti.melampo.vir.sapir.SAPIREHMetric;
import it.cnr.isti.melampo.vir.sapir.SAPIRHTMetric;
import it.cnr.isti.melampo.vir.sapir.SAPIRParaMetric;
import it.cnr.isti.melampo.vir.sapir.SAPIRSCMetric;
import it.cnr.isti.melampo.vir.sapir.SAPIRTextureMetric;
import it.cnr.isti.melampo.vir.sapir.SapirReader;
import it.cnr.isti.melampo.vir.sapir.SapirSearcher;
import it.cnr.isti.vir.features.mpeg7.SAPIRObject;
import it.cnr.isti.vir.similarity.metric.SAPIRMetric;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.ParallelMultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.index.ParallelReader;


public class LuceneSearcher {

	private static int m_topBoFQuery=-1;
	

	private static boolean m_VerboseMode = false;
	
	protected static Searcher m_s=null;
	protected static Searcher m_sBOF=null;
	protected static Searcher m_sMPG7=null;
	protected static Searcher m_sText=null;
	protected static IndexSearcher m_sMPG7C=null;
	
	protected static IndexReader m_r;
	//protected static Searcher m_sBOF;
	//protected static Searcher m_sMP7;
	protected static BoFSearcher m_bofSearcher;
	protected static SapirSearcher m_soSearcher;
	protected ScoreDoc[] m_hits;

	protected static PerFieldAnalyzerWrapper m_wrapper;
	protected static int m_hitsPerPage = 100;
	protected static int m_retrieve = 1000;
	
	protected static SAPIRObjectFieldAdder m_sfaALL;
//	protected static SAPIRObjectFieldAdder m_sfaColor;
//	protected static SAPIRObjectFieldAdder m_sfaTexture;
	
	protected static SAPIRObjectFieldAdder m_sfaSC;
	protected static SAPIRObjectFieldAdder m_sfaCS;
	protected static SAPIRObjectFieldAdder m_sfaCL;
	protected static SAPIRObjectFieldAdder m_sfaEH;
	protected static SAPIRObjectFieldAdder m_sfaHT;
	
	protected static int m_toppivsQ;
	protected static int m_toppivsI;
	protected static int m_nPivots;
	protected static String m_PivFile;
	
	protected static File m_propertyFile;

	// for reordering purposes
	protected static SAPIRObject[] m_so;
	protected static SapirReader m_sreader;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	
public static void OpenIndex(File propertyFile) throws IOException {
		
		if (m_s != null || m_sBOF != null || m_sMPG7C != null) return;
		
		Settings settings = null;
		
		try {
			try {
				m_bofSearcher = new BoFSearcher(propertyFile);
				m_soSearcher = new SapirSearcher(propertyFile);
				
				m_sreader = new SapirReader(propertyFile);
				m_so = m_sreader.getAllSapirObject();
				
			} catch (VIRException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			settings = new Settings(propertyFile);
			m_propertyFile = propertyFile;
		} catch (VIRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int Indexes=0;
		
		IndexReader rBOF = null;
		String indexPath = settings.getLuceneIndexPathBOF();
		if(indexPath!=null){
			m_topBoFQuery = settings.getTopBofQuery();
		
			Directory dir = FSDirectory.open(new File(indexPath));
			//Directory rd = new RAMDirectory(dir);
			//rBOF = IndexReader.open(rd,true);
			rBOF = IndexReader.open(dir,true);
			m_sBOF = new IndexSearcher(rBOF);
			System.out.println(indexPath+" open "+rBOF.numDocs()+" docs");
			Indexes++;
		}
		
		IndexReader rMPG7C = null;
		indexPath = settings.getLuceneIndexPathMPG7C();
		if(indexPath!=null){
			Directory dir = FSDirectory.open(new File(indexPath));		
			rMPG7C = IndexReader.open(dir,true);
			m_sMPG7C = new IndexSearcher(rMPG7C);
			m_sMPG7C.setSimilarity(new CosineSimilarity());
			System.out.println("mpeg-7 Pre-Combined using cosine similarity");
			System.out.println(indexPath+" open "+rMPG7C.numDocs()+" docs");
			Indexes++;
		}
		
		IndexReader rMPG7 = null;
		indexPath = settings.getLuceneIndexPathMPG7();
		if(indexPath!=null){
			Directory dir = FSDirectory.open(new File(indexPath));		
			rMPG7 = IndexReader.open(dir,true);
			m_sMPG7 = new IndexSearcher(rMPG7);
			m_sMPG7.setSimilarity(new CosineSimilarity());
			System.out.println("mpeg-7 using cosine similarity");
			System.out.println(indexPath+" open "+rMPG7.numDocs()+" docs");
			Indexes++;
		}
				
		IndexReader rText = null;
		indexPath = settings.getLuceneIndexPathMetadata();
		if(indexPath!=null){
			Directory dir = FSDirectory.open(new File(indexPath));
			rText = IndexReader.open(dir,true);
			m_sText = new IndexSearcher(rText);
			System.out.println(indexPath+" open "+rText.numDocs()+" docs");
			Indexes++;
		}
		
		if (Indexes>1){
			m_r = new ParallelReader();
			if (rBOF!=null)
				((ParallelReader)m_r).add(rBOF);
			
			if (rMPG7!=null)
				((ParallelReader)m_r).add(rMPG7);
			
			if (rMPG7C!=null)
				((ParallelReader)m_r).add(rMPG7C);
			
			if (rText!=null)
				((ParallelReader)m_r).add(rText);
			
			m_s = new IndexSearcher(m_r);
			System.out.println("Parallel index open");
		}		

		m_wrapper = LuceneIndexer.CreateWrapperAnalyzer();
		
		m_nPivots = settings.getnPivots();
		m_PivFile = settings.getPivotsPath();
		
		m_sfaALL = new SAPIRObjectFieldAdder(new SAPIRMetric());
		m_sfaALL.loadBinPivots(m_PivFile, m_nPivots);
//		
//		m_sfaColor = new SAPIRObjectFieldAdder(new SAPIRColorMetric());
//		m_sfaColor.loadBinPivots(m_PivFile, m_nPivots);
//		
//		m_sfaTexture = new SAPIRObjectFieldAdder(new SAPIRTextureMetric());
//		m_sfaTexture.loadBinPivots(m_PivFile, m_nPivots);
		
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

public static int NumOfDocs(){
		return m_r.numDocs();
	}

//high level query that takes an ID or an XML and other future fields
public void Query(ArrayList<String> values, ArrayList<String> fields, boolean isQueryID){
		
		ArrayList<String> vals = (ArrayList<String>)values.clone();
		ArrayList<String> flds = (ArrayList<String>)fields.clone();
		SAPIRObject sq = null;
		
		// test if BOF is queried too, if true downgrade the number of pivots of query for mpeg7
		
		int toppivsQ = m_toppivsQ;
		
		for(int i=0;i<flds.size();i++){
			if(flds.get(i).contentEquals(Parameters.BOFFIELD)) toppivsQ = 10;
		}
		
		// for reordering
		double clw=0.0; 
		double csw=0.0;
		double ehw=0.0;
		double htw=0.0;
		double scw=0.0;
		
		for(int i=0;i<flds.size();i++){
			if(flds.get(i).contentEquals(Parameters.BOFFIELD)){
				try {
					String bofString = m_bofSearcher.getBof(vals.get(i), isQueryID, m_topBoFQuery);
					vals.set(i, bofString);
					//ma a che serve???? is a bug!!!!
					//break;
				} catch (BoFException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else if (flds.get(i).contains("MP7")){
				try {
					sq = m_soSearcher.getSapirObject(vals.get(i), isQueryID);
					
					String soString = "";
					
					if (flds.get(i).contentEquals(Parameters.MP7SC))
					{
						soString = m_sfaSC.metricObjectToStringQ(sq, toppivsQ, m_toppivsI);
						scw=1.0;
					}
					else if (flds.get(i).contentEquals(Parameters.MP7CS)){						
						soString = m_sfaCS.metricObjectToStringQ(sq, toppivsQ, m_toppivsI);
						csw=1.0;
					}
					else if (flds.get(i).contentEquals(Parameters.MP7CL))
					{
						soString = m_sfaCL.metricObjectToStringQ(sq, toppivsQ, m_toppivsI);
						clw=1.0;
					}
					else if (flds.get(i).contentEquals(Parameters.MP7EH)){
						soString = m_sfaEH.metricObjectToStringQ(sq, toppivsQ, m_toppivsI);						
						ehw=1.0;
					}
					else if (flds.get(i).contentEquals(Parameters.MP7HT)){
						soString = m_sfaHT.metricObjectToStringQ(sq, toppivsQ, m_toppivsI);
						htw=1.0;
					}
					else if (flds.get(i).contentEquals(Parameters.MP7ALL)){
						soString = m_sfaALL.metricObjectToStringQ(sq, toppivsQ, m_toppivsI);
						scw=1.0;
						csw=1.0;
						clw=1.0;
						ehw=1.0;
						htw=1.0;
					}					
					vals.set(i, soString);
				} catch (VIRException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		SAPIRParaMetric sm = new SAPIRParaMetric(clw, csw, ehw, htw, scw);
		
		try {
			_Query(vals, flds, sq, sm);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

//low level query that takes a BOF as text and other future fields
public void _Query(ArrayList<String> vals, ArrayList<String> flds, SAPIRObject sq, SAPIRParaMetric sm) throws ParseException, IOException{

		Query q=null;
		
		//remove empty fields
		boolean bof=false; 
		boolean mp7=false;
		boolean mp7c=false;
		boolean text=false;
		for(int i=0;i<flds.size();i++){
			if(vals.get(i).length()==0){
				vals.remove(i);
				flds.remove(i);
			}else if (m_sMPG7 != null && 
					 (flds.get(i).contentEquals(Parameters.MP7CL) ||
					  flds.get(i).contentEquals(Parameters.MP7CS) ||
					  flds.get(i).contentEquals(Parameters.MP7SC) ||
					  flds.get(i).contentEquals(Parameters.MP7EH) ||
					  flds.get(i).contentEquals(Parameters.MP7HT) )) mp7=true;			
			else if (flds.get(i).contentEquals("MP7ALL") && m_sMPG7C != null) mp7c=true;
			else if (flds.get(i).contentEquals("BOF") && m_sBOF != null) bof=true;
			else if (flds.get(i).contentEquals("TEXT") && m_sText != null) text=true;
		}
		
		BooleanClause.Occur[] flags = new BooleanClause.Occur[flds.size()];
		
		for(int i=0;i<flds.size();i++){
			if(flds.get(i).contentEquals("TEXT"))
				flags[i] = BooleanClause.Occur.MUST;
			else
				flags[i] = BooleanClause.Occur.SHOULD;
		}
		
		
		String[] v  = new String[vals.size()];
		String[] f  = new String[flds.size()];

		q = MultiFieldQueryParser.parse(Version.LUCENE_30,vals.toArray(v), flds.toArray(f), flags, m_wrapper);

		TopDocs td = null;
	    if (bof && !mp7 && !text && !mp7c){
	    	System.out.println("Using standalone BOF index");
	    	td = m_sBOF.search(q, m_retrieve);
	    }
	    else if (mp7 && !bof && !text && !mp7c){
	    	System.out.println("Using standalone MPEG-7 single features index");
	    	td = m_sMPG7.search(q, m_retrieve);
	    }
	    else if (mp7c && !bof && !text && !mp7){
	    	System.out.println("Using standalone MPEG-7 combined features index");
	    	td = m_sMPG7C.search(q, m_retrieve);
	    }
	    else if (text && !bof && !mp7 && !mp7c){
	    	System.out.println("Using standalone Metadata index");
	    	td = m_sText.search(q, m_retrieve);
	    }
	    else{
	    	System.out.println("Using combined index");
	    	td = m_s.search(q, m_retrieve);
	    }
	    
	    m_hits = td.scoreDocs;
	    ReorderResults(sq ,sm);
	}

private void ReorderResults(SAPIRObject sq, SAPIRParaMetric sm){
	
	ScoreDoc[] m_hitsNew=null; 
	
	if (sq==null) return;
	
	int nresults = 0;
	
	if (m_hits.length < m_hitsPerPage)
		nresults = m_hits.length;
	else
		nresults = m_hitsPerPage;
	
	m_hitsNew = new ScoreDoc[nresults];
	
	OrderTopKArrayList knn = new OrderTopKArrayList(nresults);
	
	for(int i=0;i<m_hits.length;i++){
		int d = m_hits[i].doc;
		SAPIRObject s = m_so[d];
		/*SAPIRObject s=null;
		try {
			s = m_sreader.getSapirObject(d);
		} catch (VIRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		knn.insert(sm.distance(sq, s), i);
	}
	
	for(int i=0;i<nresults;i++){
		m_hitsNew[i] = m_hits[knn.get(i)];
	}
	
	m_hits = m_hitsNew;
	
}

public String[][] getResults(int startFrom, int numElements) throws IOException{
		
		Searcher s=null;
		
		if (m_s!=null) s=m_s;
		else if (m_sBOF!=null) s=m_sBOF;
		else if (m_sMPG7!=null) s=m_sMPG7;
		else if (m_sMPG7C!=null) s=m_sMPG7C;
		else if (m_sText!=null) s=m_sText;
		
		String[][] retval = new String[numElements][3];
	
		for(int i=0;i<numElements;i++){
			
			if (startFrom+i > m_hits.length-1) break;
			
			float score = m_hits[startFrom+i].score;
			
			retval[i][0] = ((Float)score).toString();
			Document d = null;
			
	    	d = s.doc(m_hits[startFrom+i].doc);			
			
			retval[i][1] = d.get(Parameters.IDFIELD);
		}
		
		return retval;
	
	}
}
