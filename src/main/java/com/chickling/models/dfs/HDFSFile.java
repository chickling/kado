package com.chickling.models.dfs;

import org.apache.hadoop.fs.FileSystem;

import java.io.IOException;
import java.util.Map;

/**
 * 
 * @author lg22
 *
 */
public class HDFSFile extends FSFile{

	public HDFSFile(){
		   super();	
	}
	
	@Override
	public FileSystem genFileSysteam() {
		try {
			return FileSystem.get(conf);
		} catch (IOException e) {
			return null;
		}
	}

}
