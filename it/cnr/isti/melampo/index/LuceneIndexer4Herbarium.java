package it.cnr.isti.melampo.index;

import it.cnr.isti.melampo.metadata.MetadataSeacher;
import it.cnr.isti.melampo.tools.Settings;
import it.cnr.isti.melampo.tools.Tools;
import it.cnr.isti.melampo.vir.bof.BoFReader;
import it.cnr.isti.melampo.vir.bof.BoFSearcher;
import it.cnr.isti.melampo.vir.exceptions.BoFException;
import it.cnr.isti.melampo.vir.exceptions.VIRException;
import it.cnr.isti.melampo.vir.sapir.*;
import it.cnr.isti.vir.features.mpeg7.SAPIRObject;
import it.cnr.isti.vir.similarity.metric.SAPIRMetric;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

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

import com.davidsoergel.conja.Function;
import com.davidsoergel.conja.Parallel;

public class LuceneIndexer4Herbarium {

	private IndexWriter m_w;
	private BoFFieldAdder m_bfa=null;
	
	private BoFSearcher m_bofr=null;
	private MetadataSeacher m_ms;
	private int m_itemsToIndex;
	private boolean m_create;
	private String m_lucenePath;
	private int m_toppivs;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
	
		LuceneIndexer4Herbarium li = new LuceneIndexer4Herbarium();
		File p = new File(args[0]);
		File fileToIndex = new File(args[1]);
		li.OpenIndexBOF(p);
		li.StartIndexing(fileToIndex);
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
			m_bofr = new BoFSearcher(propertyFile);
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

	
	public void StartIndexing(File fileToIndex){
		
		File[] files = fileToIndex.listFiles();
		if (m_bfa!=null){
			
			List<File> listFiles = new Vector<File>();
			for (File file : files) {
				listFiles.add(file);
			}
			
			 Parallel.forEach(listFiles, new Function<File, Void>() {
					@Override
					public Void apply(File id) {
						doSomething(id);
						return null;
					}
		        });
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
	
	private void doSomething(File file) {
		try {
			Document doc = new Document();
			
			// BOF
			String bofString = m_bofr.getBof(Tools.file2String(file), false, -1);
			m_bfa.AddBofStringField(doc,bofString);
	
			// ID
			String id = file.getName().substring(0, file.getName().lastIndexOf("."));
			m_bfa.AddBofIDField(doc,id);

			m_w.addDocument(doc);
			
			System.out.println("indexed doc n. "+ " id: " + id);
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
