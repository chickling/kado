package com.chickling.models.jdbc;

import java.util.regex.Pattern;

/**
 * 
 * @author lg22
 *
 */
public class SqlContent {

	public static final int PAGE_ROWS = 100;
	public static final int MAX_BATCH_ROWS = 10000;
	public static final int UNION_ROWS = 20;
	public static final int COMMIT_ROWS = PAGE_ROWS/UNION_ROWS;
	public static final Pattern FINDEX_PATTERN = Pattern.compile("\\$([\\d]+)\\$");
	public static final Pattern SQL_UNION_PATTERN = Pattern.compile("#\\{([^#\\{\\}]+)\\}(:([\\d][\\,]?)+)");
	public static final Pattern SQL_UNION_PATTERN2 = Pattern.compile("#\\{([^#\\{\\}]+)\\}");
	public static final Pattern SQL_UDML_CMD_PATTERN = Pattern.compile("^(insert|update|delete)+");
	public static final Pattern SQL_SDML_CMD_PATTERN = Pattern.compile("^(select)+");
	public static final Pattern SQL_DDL_CMD_PATTERN = Pattern.compile("([\\W](drop|alter)[\\W])+");
	public static final String SQL_PATTERN_PREX = "#{";
	public static final String SQL_PATTERN_FREX = "}";
	public static final String SQL_UNION_ALL = " union all ";
	public static final String SQL_SELECT = " select ";
	public static final String SQL_INSERT = "insert";
	public static final String SQL_UPDATE = "update";
	public static final String SQL_DELETE = "delete";
	public static final String SQL_DROP = "drop";
	public static final String SQL_ALTER = "alter";
	public static final String BATCH_SIZE = "BATCH_SIZE";
	public static final String BATCH_SLEEP = "BATCH_SLEEP";
	public static final String UNION_PTN_ORIGINAL = "ORIGINAL";
	public static final String UNION_PTN_FIELDS = "FIELDS";
	public static final String UNION_PTN_FIDX = "FILE_IDX";
	public static final String PTN_FIELD_INDEX = "FIELD_INDEX";
	public static final String FIELD_COLUMN_SIZE = "FIELD_COLUMN_SIZE";
	
	public static enum SQL_OP_TYPE {
		INSERT,UPDATE,DELETE,SELECT,DROP,ALERT
	};
	
	public static enum SQL_PATTERN_TYPE {
		UNION,SINGLE
	};
	
	}
