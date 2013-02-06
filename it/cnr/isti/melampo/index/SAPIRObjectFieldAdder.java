package it.cnr.isti.melampo.index;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import it.cnr.isti.melampo.tools.OrderTopKArrayList;
import it.cnr.isti.melampo.vir.exceptions.BoFException;
import it.cnr.isti.melampo.vir.sapir.SAPIRTextureMetric;
import it.cnr.isti.vir.features.IFeaturesCollector;
import it.cnr.isti.vir.features.mpeg7.SAPIRObject;
import it.cnr.isti.vir.similarity.metric.Metric;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

public class SAPIRObjectFieldAdder {
	
	protected String m_fieldname;
	protected SAPIRObject[] m_moPivots;
	protected Metric m_metric;
	protected String m_frmat;
	
	public SAPIRObjectFieldAdder(Metric metric){
		m_metric = metric;
		
		if (metric.toString().contains("SAPIRMetric")){
			m_fieldname = Parameters.MP7ALL;
			return;
		}
		
		if (metric.toString().contains("SAPIRColorMetric")){
			m_fieldname = Parameters.MP7COL;
			return;
		}
		
		if (metric.toString().contains("SAPIRTextureMetric")){
			m_fieldname = Parameters.MP7TEXTR;
			return;
		}
		
		if (metric.toString().contains("SAPIRSCMetric")){
			m_fieldname = Parameters.MP7SC;
			return;
		}
		
		if (metric.toString().contains("SAPIRCSMetric")){
			m_fieldname = Parameters.MP7CS;
			return;
		}
		
		if (metric.toString().contains("SAPIRCLMetric")){
			m_fieldname = Parameters.MP7CL;
			return;
		}
		
		if (metric.toString().contains("SAPIREHMetric")){
			m_fieldname = Parameters.MP7EH;
			return;
		}
		
		if (metric.toString().contains("SAPIRHTMetric")){
			m_fieldname = Parameters.MP7HT;
			return;
		}
	}
	
	public void addFieldToDoc(org.apache.lucene.document.Document doc, SAPIRObject o, int toppivs) throws IOException{
		
		String field = metricObjectToString(o, toppivs);
		
	    doc.add(new Field(m_fieldname, field, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES));
	}

	
	public SAPIRObjectFieldAdder(String fileadname){
		m_fieldname = fileadname;
	}
	
	//binary file
	public void loadBinPivots(String filename, int NumOfPivots) {
		System.out.println("Loading "+filename);
	
		m_moPivots = new SAPIRObject[NumOfPivots];
		
        try{
        	DataInputStream in = null;
        	in = new DataInputStream( new BufferedInputStream ( new FileInputStream(filename)));
            //ObjectInputStream ros_file=new ObjectInputStream(new FileInputStream(filename));
			
            for(int i=0;i<NumOfPivots;i++)
			{
            	m_moPivots[i] = new SAPIRObject(in);
			}
			
            in.close();
            
        }catch(Exception e){
            System.out.println("Cannot open file "+filename+" to load reference objects;");
            e.printStackTrace();
        }
        
        m_frmat = "%0"+(int)Math.ceil(Math.log10(NumOfPivots))+"d";
		
	}
	
	//to be used for indexing
	public String metricObjectToString(SAPIRObject mo, int toppivs){
		String strDoc = "";
		int j,k;

		OrderTopKArrayList pivdist = new OrderTopKArrayList(toppivs);
		
		for(j=0;j<m_moPivots.length;j++){
			
			pivdist.insert(pivDist(j, mo), j);
		}

				
		for(j=0;j<toppivs;j++){	

			for(k=0;k<toppivs-j;k++) strDoc = strDoc + String.format(m_frmat,pivdist.get(j))+" ";
			
		}
		return strDoc;
	
}
	
	// to be used only for querying

	public String metricObjectToStringQ(SAPIRObject mo, int toppivs, int maxpivs){
		String strDoc = "";
		int j,k;

		OrderTopKArrayList pivdist = new OrderTopKArrayList(toppivs);
		
		synchronized (SAPIRObjectFieldAdder.class) {
			for(j=0;j<m_moPivots.length;j++){
				
				pivdist.insert(pivDist(j, mo), j);
			}
		}
				
		for(j=0;j<toppivs;j++){	

			//strDoc = strDoc +String.format(m_frmat,pivdist.get(j))+"^"+(maxpivs-j)+" ";
			strDoc = strDoc +String.format(m_frmat,pivdist.get(j))+"^"+(toppivs-j)+" ";
			
		}
		return strDoc;
	
	}
	
	public String inverseMetricObjectToStringQ(SAPIRObject mo, int toppivs, int maxpivs){
		String strDoc = "";
		int j,k;

		OrderTopKArrayList pivdist = new OrderTopKArrayList(m_moPivots.length);
		
		for(j=0;j<m_moPivots.length;j++){
			
			pivdist.insert(pivDist(j, mo), j);
		}

		k = maxpivs; 
		for(j=m_moPivots.length-1;j>=m_moPivots.length-toppivs;j--){	

			strDoc = strDoc + String.format(m_frmat,pivdist.get(j))+"^"+(k)+" ";
			k--;
			
		}
		return strDoc;
	
}
	
	public double pivDist(int j, SAPIRObject mo) {
		return m_metric.distance(m_moPivots[j], mo);
	}
	
	public void AddIDField(Document doc, String id) throws CorruptIndexException, IOException, BoFException{
		
		doc.add(new Field(Parameters.IDFIELD, id, Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));		
	}
	
}
