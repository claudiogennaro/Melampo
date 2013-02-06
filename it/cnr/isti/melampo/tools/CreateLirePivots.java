package it.cnr.isti.melampo.tools;

import it.cnr.isti.melampo.vir.exceptions.VIRException;
import it.cnr.isti.melampo.vir.lire.LireReader;
import it.cnr.isti.vir.features.mpeg7.LireObject;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class CreateLirePivots {

	ArrayList<Integer> m_rind;
//	LireObject[] m_so;
	
	public static void main(String[] args) {
		try {
			
			LireReader reader = new LireReader();
			int np = reader.getSettings().getnPivots();
			String path = reader.getSettings().getPivotsPath();
			
			
			CreateLirePivots csp = new CreateLirePivots();
			System.out.println("Loading dataset...");
//			csp.LoadDataset(reader);
			System.out.println("Generating pivots...");
			csp.CreateRandomPivots(np, reader.size());
			csp.StorePivots(path, reader);

		} catch (VIRException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

//	public void LoadDataset(LireReader reader){
//		m_so = new LireObject[reader.size()];
//		
//		for(int i=0;i<reader.size();i++){
//			try {
//				m_so[i] = reader.getObject(i);
//			} catch (VIRException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
	
//	public boolean GoodPivot(int piv){
//		
//
//		SAPIRSCMetric msc = new SAPIRSCMetric();
//		SAPIRCSMetric mcs = new SAPIRCSMetric();
//		SAPIRCLMetric mcl = new SAPIRCLMetric();
//		SAPIREHMetric meh = new SAPIREHMetric();
//		SAPIRHTMetric mht = new SAPIRHTMetric();
//		
//		OrderTopKArrayList knnSC = new OrderTopKArrayList(2);
//		OrderTopKArrayList knnCS = new OrderTopKArrayList(2);
//		OrderTopKArrayList knnCL = new OrderTopKArrayList(2);
//		OrderTopKArrayList knnEH = new OrderTopKArrayList(2);
//		OrderTopKArrayList knnHT = new OrderTopKArrayList(2);
//		
//		//SC
//		for(int j=0;j<m_so.length;j++){
//			
//			knnSC.insert(msc.distance(m_so[j], m_so[piv]), j);
//		}
//		
//		if (knnSC.m_list.get(1).dist == 0.0){
//			System.out.println("discarded pivot n. " + piv +" SC null");
//			return false;
//		}
//		
//		//CS
//		for(int j=0;j<m_so.length;j++){
//			
//			knnCS.insert(mcs.distance(m_so[j], m_so[piv]), j);
//		}
//		
//		if (knnCS.m_list.get(1).dist == 0.0){
//			System.out.println("discarded pivot n. " + piv +" CS null");
//			return false;
//		}
//		
//		//CL
//		for(int j=0;j<m_so.length;j++){
//			
//			knnCL.insert(mcl.distance(m_so[j], m_so[piv]), j);
//		}
//		
//		if (knnCL.m_list.get(1).dist == 0.0){
//			System.out.println("discarded pivot n. " + piv +" CL null");
//			return false;
//		}
//		
//		//EH
//		for(int j=0;j<m_so.length;j++){
//			
//			knnEH.insert(meh.distance(m_so[j], m_so[piv]), j);
//		}
//		
//		if (knnSC.m_list.get(1).dist == 0.0){
//			System.out.println("discarded pivot n. " + piv +" EH null");
//			return false;
//		}
//		
//		//HT
//		for(int j=0;j<m_so.length;j++){
//			
//			knnHT.insert(mht.distance(m_so[j], m_so[piv]), j);
//		}
//		
//		if (knnSC.m_list.get(1).dist == 0.0){
//			System.out.println("discarded pivot n. " + piv +" HT null");
//			return false;
//		}
//		
//		return true;
//		
//	}
	
	public void StorePivots(String path, LireReader reader) {
        try{
            //ObjectOutputStream pivFile = new ObjectOutputStream(new FileOutputStream(path));
        	DataOutputStream pivFile = new DataOutputStream( new BufferedOutputStream ( new FileOutputStream(path) ));
        	
            // just for debugging            
    		java.io.OutputStream outFile  = new FileOutputStream(path+".txt",false);
    		    		
            for(int i=0;i<m_rind.size();i++){
            	LireObject s = reader.getObject(m_rind.get(i));
            	s.writeData(pivFile);
    			String str = m_rind.get(i) + " " +reader.getID() + "\n";
    			outFile.write(str.getBytes());
            }
            
            pivFile.close();    		
    		outFile.close();
            
            System.out.println("Stored in "+path+" "+m_rind.size()+" pivots");
    		
        }catch(Exception e){
            System.out.println("Cannot open file "+path+" to save reference objects;");
            e.printStackTrace();
        }   
		
	}

	public void CreateRandomPivots(int np, int size) throws VIRException {
		
		Random num = new Random(42);
		//Random num = new Random(1123);
		m_rind = new ArrayList<Integer>();
		
		for(int i=0;i<np;i++){
			int r = num.nextInt(size);
			
//			while(m_rind.contains(r) || !GoodPivot(r)){
			while(m_rind.contains(r)){
				r = num.nextInt(size);
			}
			
			m_rind.add(r);
			System.out.println("Pivots n. "+i+" generated");
		}		
	}
}
