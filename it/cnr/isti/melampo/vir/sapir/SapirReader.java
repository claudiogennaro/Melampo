package it.cnr.isti.melampo.vir.sapir;

import it.cnr.isti.melampo.index.settings.MP7CSettings;
import it.cnr.isti.melampo.vir.exceptions.VIRException;
import it.cnr.isti.vir.features.FeaturesCollectorException;
import it.cnr.isti.vir.features.IFeaturesCollector;
import it.cnr.isti.vir.features.mpeg7.SAPIRObject;
import it.cnr.isti.vir.file.ArchiveException;
import it.cnr.isti.vir.file.FeaturesCollectorsArchives;
import it.cnr.isti.vir.id.IHasID;
import it.cnr.isti.vir.id.IID;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SapirReader {

	private FeaturesCollectorsArchives fcArchives;
	private MP7CSettings settings;
	
	private SAPIRObject obj;
	private String id;
	
	public SapirReader() throws IOException, VIRException {
		settings = new MP7CSettings();
		setArchives();
	}

	public SapirReader(File propertyFile) throws IOException, VIRException {
		settings = new MP7CSettings(propertyFile);
		setArchives();
	}
	
	public SapirReader(MP7CSettings settings) throws IOException, VIRException {
		this.settings = settings;
		setArchives();
	}

	private void setArchives() {
		fcArchives = settings.getFCArchives();
	}

	public int size() {
		return fcArchives.size();
	}

	private void retrieveFC(int index) throws VIRException {
		IFeaturesCollector featureColl;
		try {
			featureColl = fcArchives.get(index);
			obj = new SAPIRObject(featureColl);
			IID id = ((IHasID) featureColl).getID();
			this.id = id.toString();
		} catch (ArchiveException e) {
			throw new VIRException(e);
		} catch (FeaturesCollectorException e) {
			throw new VIRException(e);
		}
	}
	
	public SAPIRObject getSapirObject(int index) throws VIRException {
		retrieveFC(index);
		return this.obj;
	}
	
	public SAPIRObject[] getAllSapirObject() throws VIRException {
		SAPIRObject[] sapirObjects = null;
		try {
			//long time = -System.currentTimeMillis();
			ArrayList<IFeaturesCollector> arr = fcArchives.getAll();
			//time += System.currentTimeMillis();
			//System.out.println("time1: " + time);
			sapirObjects = new SAPIRObject[arr.size()];
			//time = -System.currentTimeMillis();
			for (int i = 0; i < arr.size(); i++) {
				sapirObjects[i] = new SAPIRObject(arr.get(i));
			}
			//time += System.currentTimeMillis();
			//System.out.println("time2: " + time);
		} catch (Exception e) {
			throw(new VIRException(e));
		}
		return sapirObjects;
	}
		
	public String getID() {
		return id;
	}

	public static void main(String[] args) {
		try {
			SapirReader reader = new SapirReader();
			int size = reader.size();
			System.out.println("size: " + size);
//			for (int index = 0; index < size; index++) {
//				SAPIRObject obj;			
//					obj = reader.getSapirObject(index);
//					System.out.println(reader.getID());
//					System.out.println(obj.getFeatures().toString());
//			}
			
			//Reading All
//			SAPIRObject[] temp = reader.getAllSapirObject();
//			System.out.println("getAllSapirObject found " + temp.length + " objects.");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (VIRException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public MP7CSettings getSettings(){
		return settings;
	}
}
