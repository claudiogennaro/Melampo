package it.cnr.isti.melampo.index;

import it.cnr.isti.vir.features.mpeg7.SAPIRObject;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class SapirObjectFileTestReader {
	public static void main(String[] args) throws IOException {
		SAPIRObject[] moPivots = new SAPIRObject[10000];
    	DataInputStream in = null;
    	in = new DataInputStream( new BufferedInputStream ( new FileInputStream("D:/demo/adhoc/Pivots/SapirObjectPivots10k.dat")));
        //ObjectInputStream ros_file=new ObjectInputStream(new FileInputStream(filename));
		
        for(int i=0;i<10000;i++)
		{
        	moPivots[i] = new SAPIRObject(in);
		}
		
        in.close();
	}
}
