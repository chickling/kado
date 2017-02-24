//import com.chickling.models.jdbc.SqlContent;
//import org.apache.commons.collections.MapUtils;
//import org.apache.commons.lang.StringUtils;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.util.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
///**
// * Created by gl08 on 2016/1/4.
// */
//public class ResultToSQLTest {
//
//
//
//
//    public static void main(String[] args) {
//
//        String sql="INSERT INTO [Presto].[dbo].[syn] (item,country,number,icc,time,col1,col2,Intcol3,Intcol4)  #{'$1$','$2$','00-000-1',10,'$3$','$4$',$5$,$6$,'shoppingcart'}";
//        String sql5="INSERT INTO [ECommerce].[dbo].[EC_LogonSuspendList] (Content,SuspendType,Memo)  #{'$1$','I','shoppingcart'}:1,2,3";
//        String sql1="INSERT INTO [ECommerce].[dbo].[EC_LogonSuspendList] (Content,SuspendType,Memo)  #{'$1$','I','shoppingcart'}";
//        String sql2="INSERT INTO [ECommerce].[dbo].[EC_LogonSuspendList] (Content,SuspendType,Memo)  Select * from (#{ '$1$','I','shoppingcart' }) a " +
//                "Where not exit (select top 1 1 from [ECommerce].[dbo].[EC_LogonSuspendList] (Content,SuspendType,Memo) with(nolock) where content=a.content )";
//
//        String sql3="UPDATE YOP SET A=$1$ WHERE A=$2$";
//
//        String sql4="SELECT '$1$',$2$";
////        #{...} --> (SELECT ... UNION ALL SELECT ... )
////        #{...}:1,2,3  :1,2,3 --> HIVE RESULT FILE INDEX
//
////          SQL_PATTERN = Pattern.compile("#\\{([^#\\{\\}]+)\\}(:([\\d][\\,]?)+){0,1}");
//        Matcher matcher1=SqlContent.SQL_UNION_PATTERN2.matcher(sql);
//        HashMap patternMap = new HashMap<String,Object>();
////       r = SqlContent.SQL_UNION_PATTERN.matcher(sql);
//
////        final Pattern SQL_PATTERN = Pattern.compile("(\\$([\\d]+)\\$)+");
////        Pattern.compile("\\$([\\d]+)\\$");
////        Matcher matcher=SQL_PATTERN.matcher(sql4);
//
////        Matcher matcher=SQL_PATTERN.matcher(sql);
////        System.out.println(matcher.group());
//
//        int pause=0;
//    }
//
//    	public static List<String> getColumns(BufferedReader br) throws IOException {
//		String head ="";
//		List<String> columns = new ArrayList<String>();
//		br.skip(0);
//		head = br.readLine();
//		if(StringUtils.isNotEmpty(head)){
//			columns = Arrays.asList(head.split(","));
//		}
//		return columns;
//	}
//
//    	public static List<Integer> parseFieldsIndex(String values){
//		Matcher matcher = SqlContent.FINDEX_PATTERN.matcher(values);
//		List<Integer> fieldIndex = new ArrayList<Integer>();
//		while (matcher.find()) {
//			fieldIndex.add(Integer.parseInt(matcher.group(1)));
//		}
//		return fieldIndex;
//	}
//
//    public List<String> parseSingleSql(BufferedReader br,String sql,Map<String,Object> patternMap) throws Exception {
//		String line ="";
//		int lread=0;
//		String tmpSql = "";
//		List<String> contents = new ArrayList<String>();
//		List<String> sqls = new ArrayList<String>();
//		if(MapUtils.isEmpty(patternMap)) {
//			return sqls;
//		}
//		Long batchSize = (Long)patternMap.get(SqlContent.BATCH_SIZE);
//		List<Integer> fieldIndex = (List<Integer>)patternMap.get(SqlContent.PTN_FIELD_INDEX);
//		Integer columnSize = (Integer)patternMap.get(SqlContent.FIELD_COLUMN_SIZE);
//		while( (line = br.readLine()) != null ){
//			line=line.replaceAll("'", "''");
//			line=line.replaceAll("\\\\", "\\\\\\\\");
//			line=line.replaceAll("\\$", "\\\\\\$");
//			contents = Arrays.asList(line.split("\t|\001"));
//			try{
//				tmpSql = sql;
//				for(Integer index : fieldIndex) {
//					if (index<=0 || index > columnSize) {
//						tmpSql = tmpSql.replaceAll("\\$"+index+"\\$", "");
//					} else {
//						tmpSql = tmpSql.replaceAll("\\$"+index+"\\$",contents.get(index-1));
//					}
//				}
//				sqls.add(tmpSql);
//			}catch(Exception e){
//                e.fillInStackTrace();
////				log.error("Parse error: "+contents,e);
//			}
//			lread++;
//			if(lread>=batchSize)  break;
//		}
//		return sqls;
//	}
//
//    public List<String> parseUnionSql(BufferedReader br,String sql,Map<String,Object> patternMap) throws Exception {
//		String line ="";
//		int lread=0;
//		int commitRows=0;
//		List<String> contents = new ArrayList<String>();
//		String tmpValuesql = "";
//		List<String> sqls = new ArrayList<String>();
//		List<String> unionSqls = null;
//		StringBuffer sbf = null;
//		if(MapUtils.isEmpty(patternMap)) {
//			return sqls;
//		}
//		Long batchSize = (Long)patternMap.get(SqlContent.BATCH_SIZE);
//		Long commitSize = batchSize/SqlContent.UNION_ROWS;
//		String values_fields_sql = (String)patternMap.get(SqlContent.UNION_PTN_FIELDS);
//		String fieldPatern = (String)patternMap.get(SqlContent.UNION_PTN_ORIGINAL);
//		List<Integer> fieldIndex = (List<Integer>)patternMap.get(SqlContent.PTN_FIELD_INDEX);
//		Integer columnSize = (Integer)patternMap.get(SqlContent.FIELD_COLUMN_SIZE);
//		for(;commitRows<commitSize;commitRows++){
//			lread=0;
//			unionSqls = new ArrayList<String>();
//			while( (line = br.readLine()) != null ){
//				line=line.replaceAll("'", "''");
//				line=line.replaceAll("\\\\", "\\\\\\\\");
//				line=line.replaceAll("\\$", "\\\\\\$");
//				contents = Arrays.asList(line.split("\t|\001"));
//				try{
//					sbf = new StringBuffer();
//					sbf.append(SqlContent.SQL_SELECT);
//					tmpValuesql =  values_fields_sql;
//					for(Integer index : fieldIndex) {
//						if (index<=0 || index > columnSize) {
//							tmpValuesql = tmpValuesql.replaceAll("\\$"+index+"\\$", "");
//						} else {
//							tmpValuesql = tmpValuesql.replaceAll("\\$"+index+"\\$",contents.get(index-1));
//						}
//					}
//					sbf.append(tmpValuesql);
//					unionSqls.add(sbf.toString());
//
//				}catch(Exception e){
////					log.error("Parse error: "+contents,e);
//				}
//				lread++;
//				if(lread>=SqlContent.UNION_ROWS)  break;
//			}
////			if(CollectionUtils.isNotEmpty(unionSqls)){
////				sqls.add(StringUtils.replace(sql, fieldPatern, StringUtils.join(unionSqls,SqlContent.SQL_UNION_ALL)));
////			}
//		}
//		return sqls;
//	}
//}
