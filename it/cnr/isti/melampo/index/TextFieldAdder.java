package it.cnr.isti.melampo.index;

import it.cnr.isti.melampo.vir.exceptions.BoFException;
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

public class TextFieldAdder {
	
	public TextFieldAdder(){
		
	}
	
	public void AddTextStringField(Document doc, String textString) throws CorruptIndexException, IOException, BoFException{
		
		doc.add(new Field(Parameters.TEXT, textString, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES));		
	}
	
	public void AddBofIDField(Document doc, String id) throws CorruptIndexException, IOException, BoFException{
				
		doc.add(new Field(Parameters.IDFIELD, id, Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));		
	}
	
}
