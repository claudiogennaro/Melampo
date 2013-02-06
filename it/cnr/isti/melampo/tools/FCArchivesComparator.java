package it.cnr.isti.melampo.tools;

import java.io.File;
import java.io.IOException;

import it.cnr.isti.melampo.vir.exceptions.VIRException;
import it.cnr.isti.melampo.vir.sapir.SapirReader;

public class FCArchivesComparator {

	/**
	 * @param args
	 * @throws VIRException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, VIRException {
		
		File f1 = new File("D:/Projects/melampo/conf/conf1.properties");
		SapirReader m_sreader1 = new SapirReader(f1);
		
		File f2 = new File("D:/Projects/melampo/conf/conf2.properties");
		SapirReader m_sreader2 = new SapirReader(f2);
		
		int sizemax;
		int size1 = m_sreader1.size();
		int size2 = m_sreader2.size();
		
		System.out.println(f1.getAbsolutePath()+" size="+size1);
		System.out.println(f2.getAbsolutePath()+" size="+size2);
		
		if (m_sreader1.size() > m_sreader2.size())
			sizemax = m_sreader1.size();
		else
			sizemax = m_sreader2.size();

		int i1=0; 
		int i2=0;
		
		while(i1<size1){
			m_sreader1.getSapirObject(i1);
			String id1 = m_sreader1.getID();
			
			m_sreader2.getSapirObject(i2);
			String id2 = m_sreader2.getID();
			if (i1%1000 == 0){
				System.out.println("compared "+i1+" ids");
			}
			if (id1.contentEquals(id2)){
				i1++;
				i2++;
			}else if(size1>size2){
				while(!id1.contentEquals(id2)){
					System.out.println(i1+" "+id1);
					i1++;
					m_sreader1.getSapirObject(i1);
					id1 = m_sreader1.getID();
				}
			}else{
				while(!id1.contentEquals(id2)){
					System.out.println(i2+" "+id2);
					i2++;
					m_sreader1.getSapirObject(i2);
					id2 = m_sreader1.getID();
				}
			}
			
		}
	}

}
