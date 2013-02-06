package it.cnr.isti.melampo.index;

import it.cnr.isti.melampo.metadata.MetadataSeacher;
import it.cnr.isti.melampo.tools.Settings;
import it.cnr.isti.melampo.vir.bof.BoFReader;
import it.cnr.isti.melampo.vir.exceptions.BoFException;
import it.cnr.isti.melampo.vir.exceptions.VIRException;
import it.cnr.isti.melampo.vir.sapir.*;
import it.cnr.isti.vir.features.mpeg7.SAPIRObject;
import it.cnr.isti.vir.similarity.metric.SAPIRMetric;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class LuceneIndexer {

	private IndexWriter m_w;
	private BoFFieldAdder m_bfa=null;
	private SAPIRObjectFieldAdder m_sfaSC=null;
	private SAPIRObjectFieldAdder m_sfaCS=null;
	private SAPIRObjectFieldAdder m_sfaCL=null;
	private SAPIRObjectFieldAdder m_sfaEH=null;
	private SAPIRObjectFieldAdder m_sfaHT=null;
	private SAPIRObjectFieldAdder m_sfaALL=null;
	private TextFieldAdder m_tfa=null;
	
	private SapirReader m_sr;
	private BoFReader m_bofr=null;
	private MetadataSeacher m_ms;
	private int m_itemsToIndex;
	private boolean m_create;
	private String m_lucenePath;
	private int m_toppivs;
	private int m_nPivots;
	private String m_PivFile;
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
	
		LuceneIndexer li = new LuceneIndexer();
		File p = new File(args[0]);
		//li.OpenIndexMetadata(p);
//		li.OpenIndexBOF(p);
//		li.StartIndexing();
		
//		li = new LuceneIndexer();
//		p = new File(args[0]);
//		
		li.OpenIndexMPG7C(p);
		li.StartIndexing();
	}
	
	public void OpenIndexBOF(File propertyFile) throws IOException{
		
		Directory index = null;
		Settings settings=null;
		try {
			settings = new Settings(propertyFile);
		} catch (VIRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PerFieldAnalyzerWrapper wrapper = CreateWrapperAnalyzer();

		m_itemsToIndex = settings.getItemsToIndex();
		m_create = settings.isCreateIndex();
		
		// to be fixed
		m_lucenePath = settings.getLuceneIndexPathBOF();
		
		File f = new File(m_lucenePath);
		
		index = FSDirectory.open(f);
		
		m_w = new IndexWriter(index, wrapper, m_create, IndexWriter.MaxFieldLength.UNLIMITED);
		
		m_bfa = new BoFFieldAdder();
		
		try {
			m_bofr = new BoFReader(propertyFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (VIRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

public void OpenIndexMPG7C(File propertyFile) throws IOException{
		
		Directory index = null;
		Settings settings=null;
		try {
			settings = new Settings(propertyFile);
		} catch (VIRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PerFieldAnalyzerWrapper wrapper = CreateWrapperAnalyzer();

		m_itemsToIndex = settings.getItemsToIndex();
		m_create = settings.isCreateIndex();
		
		// to be fixed
		m_lucenePath = settings.getLuceneIndexPathMPG7C();
		
		File f = new File(m_lucenePath);
		
		index = FSDirectory.open(f);
		
		m_w = new IndexWriter(index, wrapper, m_create, IndexWriter.MaxFieldLength.UNLIMITED);
		

		m_nPivots = settings.getnPivots();
		m_PivFile = settings.getPivotsPath();
			
		m_sfaALL = new SAPIRObjectFieldAdder(new SAPIRMetric());
		m_sfaALL.loadBinPivots(m_PivFile, m_nPivots);
				
		m_toppivs = settings.getToppivsI();
		
		try {
			m_sr = new SapirReader(propertyFile);
		} catch (VIRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
public void OpenIndexMPG7(File propertyFile) throws IOException{
		
		Directory index = null;
		Settings settings=null;
		try {
			settings = new Settings(propertyFile);
		} catch (VIRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PerFieldAnalyzerWrapper wrapper = CreateWrapperAnalyzer();

		m_itemsToIndex = settings.getItemsToIndex();
		m_create = settings.isCreateIndex();
		
		// to be fixed
		m_lucenePath = settings.getLuceneIndexPathMPG7();
		
		File f = new File(m_lucenePath);
		
		index = FSDirectory.open(f);
		
		m_w = new IndexWriter(index, wrapper, m_create, IndexWriter.MaxFieldLength.UNLIMITED);
		
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
		
		m_toppivs = settings.getToppivsI();
		
		try {
			m_sr = new SapirReader(propertyFile);
		} catch (VIRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

public void OpenIndexMetadata(File propertyFile) throws IOException{
	
	Directory index = null;
	Settings settings=null;
	try {
		settings = new Settings(propertyFile);
	} catch (VIRException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	PerFieldAnalyzerWrapper wrapper = CreateWrapperAnalyzer();

	m_itemsToIndex = settings.getItemsToIndex();
	m_create = settings.isCreateIndex();
	
	// to be fixed
	m_lucenePath = settings.getLuceneIndexPathMetadata();
	
	File f = new File(m_lucenePath);
	
	index = FSDirectory.open(f);
	
	m_w = new IndexWriter(index, wrapper, m_create, IndexWriter.MaxFieldLength.UNLIMITED);
	
	m_tfa = new TextFieldAdder();
	
	try {
		m_bofr = new BoFReader(propertyFile);
		m_ms = new MetadataSeacher(propertyFile);
		
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (VIRException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}

	public static PerFieldAnalyzerWrapper CreateWrapperAnalyzer(){
		
		WhitespaceAnalyzer wsa = new WhitespaceAnalyzer();
		StandardAnalyzer sa = new StandardAnalyzer(Version.LUCENE_30);
		PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(sa);
		
		wrapper.addAnalyzer(Parameters.BOFFIELD, (Analyzer)wsa);
		wrapper.addAnalyzer(Parameters.MP7ALL, (Analyzer)wsa);
		wrapper.addAnalyzer(Parameters.MP7COL, (Analyzer)wsa);
		wrapper.addAnalyzer(Parameters.MP7TEXTR, (Analyzer)wsa);
		wrapper.addAnalyzer(Parameters.MP7CS, (Analyzer)wsa);
		wrapper.addAnalyzer(Parameters.MP7SC, (Analyzer)wsa);
		wrapper.addAnalyzer(Parameters.MP7CL, (Analyzer)wsa);
		wrapper.addAnalyzer(Parameters.MP7EH, (Analyzer)wsa);
		wrapper.addAnalyzer(Parameters.MP7HT, (Analyzer)wsa);
		
		return wrapper;
		
	}

	
	public void StartIndexing(){
		
		int k = m_itemsToIndex;
		
		if (m_bofr!=null){
			if (m_itemsToIndex<0) 
				k = m_bofr.size();
		}
		else{
			if (m_itemsToIndex<0) 
				k = m_sr.size();
		} 
		
		if (m_bfa!=null){
			for (int index = 0; index < k; index++) {
				
				try {
					Document doc = new Document();
					
					// BOF
					String bofString = m_bofr.getBof(index);
					m_bfa.AddBofStringField(doc,bofString);
			
					// ID
					String id = m_bofr.getID();
					m_bfa.AddBofIDField(doc,id);
	
					m_w.addDocument(doc);
					
					System.out.println("indexed doc n. "+(index+1));
				} catch (BoFException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();			
				} catch (CorruptIndexException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (VIRException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		
		if (m_sfaCS!=null){
			for (int index = 0; index < k; index++) {
				
				try {
					Document doc = new Document();
					
					// MPEG-7
					SAPIRObject s = m_sr.getSapirObject(index);
					m_sfaCS.addFieldToDoc(doc, s, m_toppivs);
					m_sfaSC.addFieldToDoc(doc, s, m_toppivs);
					m_sfaCL.addFieldToDoc(doc, s, m_toppivs);
					m_sfaEH.addFieldToDoc(doc, s, m_toppivs);
					m_sfaHT.addFieldToDoc(doc, s, m_toppivs);
			
					// ID
					String id = m_sr.getID();
					m_sfaCS.AddIDField(doc, id);
					
					m_w.addDocument(doc);
					
					System.out.println("indexed doc n. "+(index+1));	
				} catch (CorruptIndexException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (VIRException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		
		if (m_sfaALL!=null){
			for (int index = 0; index < k; index++) {
				
				try {
					Document doc = new Document();
					
					// MPEG-7
					SAPIRObject s = m_sr.getSapirObject(index);
					m_sfaALL.addFieldToDoc(doc, s, m_toppivs);
			
					// ID
					String id = m_sr.getID();
					m_sfaALL.AddIDField(doc, id);
					System.out.println("id " + id);

					m_w.addDocument(doc);
					
					System.out.println("indexed doc n. "+(index+1));	
				} catch (CorruptIndexException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (VIRException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		
		if (m_tfa!=null){
			for (int index = 0; index < k; index++) {
				
				try {
					Document doc = new Document();
					
					// TEXT
					m_bofr.getBof(index);
					String id = m_bofr.getID();
					String text = m_ms.getMetadata(id);
					m_tfa.AddTextStringField(doc, text);
					
					// ID
					m_tfa.AddBofIDField(doc,id);
	
					m_w.addDocument(doc);
					
					System.out.println("indexed doc n. "+(index+1));
				} catch (BoFException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();			
				} catch (CorruptIndexException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (VIRException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		
		try {
			m_w.close();
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
