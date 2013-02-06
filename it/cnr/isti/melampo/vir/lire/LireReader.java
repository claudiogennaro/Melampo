package it.cnr.isti.melampo.vir.lire;

import it.cnr.isti.melampo.index.settings.LireSettings;
import it.cnr.isti.melampo.vir.exceptions.VIRException;
import it.cnr.isti.vir.features.FeaturesCollectorException;
import it.cnr.isti.vir.features.IFeaturesCollector;
import it.cnr.isti.vir.features.mpeg7.LireObject;
import it.cnr.isti.vir.file.ArchiveException;
import it.cnr.isti.vir.file.FeaturesCollectorsArchives;
import it.cnr.isti.vir.id.IHasID;
import it.cnr.isti.vir.id.IID;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class LireReader {

	private FeaturesCollectorsArchives fcArchives;
	private LireSettings settings;
	
	private LireObject obj;
	private String id;
	
	public LireReader() throws IOException, VIRException {
		settings = new LireSettings();
		setArchives();
	}

	public LireReader(File propertyFile) throws IOException, VIRException {
		settings = new LireSettings(propertyFile);
		setArchives();
	}
	
	public LireReader(LireSettings settings) throws IOException, VIRException {
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
			obj = new LireObject(featureColl);
			IID id = ((IHasID) featureColl).getID();
			this.id = id.toString();
		} catch (ArchiveException e) {
			throw new VIRException(e);
		} catch (FeaturesCollectorException e) {
			throw new VIRException(e);
		}
	}
	
	public LireObject getObject(int index) throws VIRException {
		retrieveFC(index);
		return this.obj;
	}
	
	public LireObject[] getAllObject() throws VIRException {
		LireObject[] sapirObjects = null;
		try {
			//long time = -System.currentTimeMillis();
			ArrayList<IFeaturesCollector> arr = fcArchives.getAll();
			//time += System.currentTimeMillis();
			//System.out.println("time1: " + time);
			sapirObjects = new LireObject[arr.size()];
			//time = -System.currentTimeMillis();
			for (int i = 0; i < arr.size(); i++) {
				sapirObjects[i] = new LireObject(arr.get(i));
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
			LireReader reader = new LireReader();
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
	
	public LireSettings getSettings(){
		return settings;
	}
}
