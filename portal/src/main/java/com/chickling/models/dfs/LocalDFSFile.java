//package com.chickling.models.dfs;
//
//import org.apache.hadoop.fs.FileSystem;
//
//import java.io.IOException;
//
///**
// *
// * @author lg22
// *
// */
//public class LocalDFSFile extends FSFile{
//
//	public LocalDFSFile(){
//		super();
//	}
//
//	@Override
//	public FileSystem genFileSysteam() {
//		try {
//			return FileSystem.getLocal(conf);
//		} catch (IOException e) {
//			return null;
//		}
//	}
//
//}
