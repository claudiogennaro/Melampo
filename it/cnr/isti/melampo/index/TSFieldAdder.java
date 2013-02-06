package it.cnr.isti.melampo.index;

import it.cnr.isti.melampo.vir.bof.BoFReader;
import it.cnr.isti.melampo.vir.exceptions.BoFException;
import it.cnr.isti.melampo.vir.exceptions.VIRException;

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

public class TSFieldAdder {
	
	public TSFieldAdder(){
		
	}
	
	
	public void AddTSStringField(Document doc, String tsString) throws CorruptIndexException, IOException, BoFException{
		
		doc.add(new Field(Parameters.TS, tsString, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES));		
	}
	
	public void AddTSIDField(Document doc, String id) throws CorruptIndexException, IOException, BoFException{
				
		doc.add(new Field(Parameters.IDFIELD, id, Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));		
	}
	
}
