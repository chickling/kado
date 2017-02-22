package com.chickling.util;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.chickling.models.job.PrestoContent;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;

/**
 * 
 * @author lg22
 *
 */
public class PrestoGrammarUtil {
	
	private static Logger  log = LogManager.getLogger(PrestoGrammarUtil.class);

	/**
	 *   select * from tt where dt=$TODAY-1$ and hour=$HOUR-1$
	 * @param cmd
	 * @return
	 */
	public static Map<String,Object> parseConst(String cmd,Map<String,String> conditionMap) {
		cmd = parseHiveCommand(cmd, extractCondition(conditionMap),1);
		Matcher mch = PrestoContent.CONDITION_CONST.matcher(cmd);
		Matcher subMch = null;
		Map<String,Object> constMap = new HashMap<String,Object>();
		JSONObject json = new JSONObject();

		String group = "" ,value = "",sign = "", year="" ,dm="", dt = "",hour = "";
		String upperGroup = "";
		String c_dt="",c_hour="",$dt="",$hour="";
		String[]values = null;
		Calendar cal = null;
		try {
			while (mch.find()) {
				group = mch.group();
				upperGroup = group.toUpperCase();
				log.info("*******SQL Group*******:" + group);
				if ((subMch = PrestoContent.CONDITION_CONST_TODAY.matcher(upperGroup)).find()) {
					cal = Calendar.getInstance();
					sign = subMch.group(2);
					if ("+".equals(sign)) {
						value = subMch.group(3);
						cal.add(Calendar.DAY_OF_YEAR, Integer.parseInt(value));
					} else if ("-".equals(sign)) {
						value = subMch.group(1);
						cal.add(Calendar.DAY_OF_YEAR, Integer.parseInt(value));
					}
					dt = TimeUtil.formatDateToStr(cal.getTime(), TimeUtil.SHORT_FORMAT);
					json.put(group, "'" + dt + "'");
				} else if ((subMch = PrestoContent.CONDITION_CONST_YEAR.matcher(upperGroup)).find()) {
					cal = Calendar.getInstance();
					sign = subMch.group(2);
					if ("+".equals(sign)) {
						value = subMch.group(3);
						cal.add(Calendar.YEAR, Integer.parseInt(value));
					} else if ("-".equals(sign)) {
						value = subMch.group(1);
						cal.add(Calendar.YEAR, Integer.parseInt(value));
					}
					year = TimeUtil.formatDateToStr(cal.getTime(), TimeUtil.SHORT_YEAR_FORMAT);
					json.put(group, "'" + year + "'");
				} else if ((subMch = PrestoContent.CONDITION_CONST_MONTH.matcher(upperGroup)).find()) {
					cal = Calendar.getInstance();
					sign = subMch.group(2);
					if ("+".equals(sign)) {
						value = subMch.group(3);
						cal.add(Calendar.MONTH, Integer.parseInt(value));
					} else if ("-".equals(sign)) {
						value = subMch.group(1);
						cal.add(Calendar.MONTH, Integer.parseInt(value));
					}
					dm = TimeUtil.formatDateToStr(cal.getTime(), TimeUtil.SHORT_MONTH_FORMAT);
					json.put(group, "'" + dm + "'");
				} else if ((subMch = PrestoContent.CONDITION_CONST_DAY.matcher(upperGroup)).find()) {
					cal = Calendar.getInstance();
					sign = subMch.group(2);
					if ("+".equals(sign)) {
						value = subMch.group(3);
						cal.add(Calendar.DAY_OF_YEAR, Integer.parseInt(value));
					} else if ("-".equals(sign)) {
						value = subMch.group(1);
						cal.add(Calendar.DAY_OF_YEAR, Integer.parseInt(value));
					}
					dt = TimeUtil.formatDateToStr(cal.getTime(), TimeUtil.SHORT_DAY_FORMAT);
					json.put(group, "'" + dt + "'");
				} else if ((subMch = PrestoContent.CONDITION_CONST_YEARMONTH.matcher(upperGroup)).find()) {
					cal = Calendar.getInstance();
					sign = subMch.group(2);
					if ("+".equals(sign)) {
						value = subMch.group(3);
						cal.add(Calendar.MONTH, Integer.parseInt(value));
					} else if ("-".equals(sign)) {
						value = subMch.group(1);
						cal.add(Calendar.MONTH, Integer.parseInt(value));
					}
					dm = TimeUtil.formatDateToStr(cal.getTime(), TimeUtil.SHORT_YEARMONTH_FORMAT);
					json.put(group, "'" + dm + "'");
				} else if ((subMch = PrestoContent.CONDITION_CONST_YEARMONTH_DAY.matcher(upperGroup)).find()) {
					cal = Calendar.getInstance();
					sign = subMch.group(2);
					StringBuffer sbf = new StringBuffer();
					if ("+".equals(sign)) {
						value = subMch.group(3);
						cal.add(Calendar.DAY_OF_YEAR, Integer.parseInt(value));
					} else if ("-".equals(sign)) {
						value = subMch.group(1);
						cal.add(Calendar.DAY_OF_YEAR, Integer.parseInt(value));
					}
					dm = TimeUtil.formatDateToStr(cal.getTime(), TimeUtil.SHORT_YEARMONTH_FORMAT);
					dt = TimeUtil.formatDateToStr(cal.getTime(), TimeUtil.SHORT_DAY_FORMAT);
					sbf.append("dm='" + dm + "' and dt='" + dt + "'");
					json.put(group, sbf);
				} else if ((subMch = PrestoContent.CONDITION_CONST_HOUR.matcher(upperGroup)).find()) {
					cal = Calendar.getInstance();
					sign = subMch.group(2);
					if ("+".equals(sign)) {
						value = subMch.group(3);
						cal.add(Calendar.HOUR_OF_DAY, Integer.parseInt(value));
					} else if ("-".equals(sign)) {
						value = subMch.group(1);
						cal.add(Calendar.HOUR_OF_DAY, Integer.parseInt(value));
					}
					hour = TimeUtil.formatDateToStr(cal.getTime(), TimeUtil.HOUR_FORMAT);
					json.put(group, "'" + hour + "'");
				} else if ((subMch = PrestoContent.CONDITION_CURRENT_TIMESTAMP.matcher(upperGroup)).find()) {
					cal = Calendar.getInstance();
					json.put(group, "'" + cal.getTimeInMillis() + "'");
				} else if ((subMch = PrestoContent.CONDITION_LAST_HOUR_CONST.matcher(upperGroup)).find()) {
					StringBuffer sbf = new StringBuffer();
					cal = Calendar.getInstance();
					c_hour = TimeUtil.formatDateToStr(cal.getTime(), TimeUtil.HOUR_FORMAT);
					c_dt = TimeUtil.formatDateToStr(cal.getTime(), TimeUtil.SHORT_FORMAT);
					value = subMch.group(1);
					cal.add(Calendar.HOUR_OF_DAY, Integer.parseInt("-" + value));
					dt = TimeUtil.formatDateToStr(cal.getTime(), TimeUtil.SHORT_FORMAT);
					hour = TimeUtil.formatDateToStr(cal.getTime(), TimeUtil.HOUR_FORMAT);
					if (c_dt.equals(dt)) {
						sbf.append("(dt='" + dt + "' and (hour>='" + hour + "' and hour<'" + (c_hour) + "'))");
					} else {
						sbf.append("((dt='" + dt + "' and hour>='" + hour + "') or (dt>'" + dt + "'))");
					}
					json.put(group, sbf);
				} else if ((subMch = PrestoContent.CONDITION_BETWEEN_LAST_CONST.matcher(upperGroup)).find()) {
					StringBuffer sbf = new StringBuffer();
					values = new String[2];
					values[0] = subMch.group(1);
					values[1] = subMch.group(2);
					if (Integer.parseInt(values[0]) > Integer.parseInt(values[1])) {
						cal = Calendar.getInstance();
						c_hour = TimeUtil.formatDateToStr(cal.getTime(), TimeUtil.HOUR_FORMAT);
						c_dt = TimeUtil.formatDateToStr(cal.getTime(), TimeUtil.SHORT_FORMAT);
						cal.add(Calendar.HOUR_OF_DAY, 0 - Integer.parseInt(values[0]));
						dt = TimeUtil.formatDateToStr(cal.getTime(), TimeUtil.SHORT_FORMAT);
						hour = TimeUtil.formatDateToStr(cal.getTime(), TimeUtil.HOUR_FORMAT);
						cal = Calendar.getInstance();
						cal.add(Calendar.HOUR_OF_DAY, 0 - Integer.parseInt(values[1]) + 1);
						$dt = TimeUtil.formatDateToStr(cal.getTime(), TimeUtil.SHORT_FORMAT);
						$hour = TimeUtil.formatDateToStr(cal.getTime(), TimeUtil.HOUR_FORMAT);
						if (c_dt.equals(dt) && c_dt.equals($dt)) {
							sbf.append("(dt='" + dt + "' and (hour>='" + hour + "' and hour<'" + ($hour) + "'))");
						} else {
							sbf.append("((dt='" + dt + "' and hour>'" + hour + "')");
							int $ytf = Integer.parseInt($dt) - Integer.parseInt(dt);
							if ($ytf > 2) {
								sbf.append(" or (dt>'" + dt + "' and dt<'" + $dt + "')");
							} else if ($ytf == 2) {
								sbf.append(" or (dt='" + (Integer.parseInt($dt) - 1) + "')");
							}
							sbf.append(" or (dt='" + $dt + "' and hour<'" + ("00".equals($hour) ? "24" : $hour) + "'))");
						}
						json.put(group, sbf);
					} else {
						log.error("Grammer " + group + " error:[" + values[0] + " less than " + values[1] + "]");
					}
				} else if ((subMch = PrestoContent.CONDITION_LAST_HOUR_FROM.matcher(upperGroup)).find()) {
					StringBuffer sbf = new StringBuffer();
					values = new String[2];
					values[0] = subMch.group(1);
					values[1] = subMch.group(2);
					cal = Calendar.getInstance();
					cal.setTime(DateUtils.parseDate(values[1], new String[]{"yyyy/MM/dd HH:mm", "yyyy-MM-dd HH:mm"}));
					c_hour = TimeUtil.formatDateToStr(cal.getTime(), TimeUtil.HOUR_FORMAT);
					c_dt = TimeUtil.formatDateToStr(cal.getTime(), TimeUtil.SHORT_FORMAT);
					cal.add(Calendar.HOUR_OF_DAY, Integer.parseInt("-" + values[0]));
					dt = TimeUtil.formatDateToStr(cal.getTime(), TimeUtil.SHORT_FORMAT);
					hour = TimeUtil.formatDateToStr(cal.getTime(), TimeUtil.HOUR_FORMAT);
					if (c_dt.equals(dt)) {
						sbf.append("(dt='" + dt + "' and (hour>='" + hour + "' and hour<'" + c_hour + "'))");
					} else {
						sbf.append("((dt='" + dt + "' and hour>='" + hour + "')");
						int $ytf = Integer.parseInt(c_dt) - Integer.parseInt(dt);
						if ($ytf > 2) {
							sbf.append(" or (dt>'" + dt + "' and dt<'" + c_dt + "')");
						} else if ($ytf == 2) {
							sbf.append(" or (dt='" + (Integer.parseInt(c_dt) - 1) + "')");
						}
						sbf.append(" or (dt='" + c_dt + "' and hour<'" + (c_hour) + "'))");
					}
					json.put(group, sbf);
				} else if ((subMch = PrestoContent.PARTITION_YEARMONTH_DAY.matcher(upperGroup)).find()) {
					cal = Calendar.getInstance();
					sign = subMch.group(2);
					StringBuffer sbf = new StringBuffer();
					if ("+".equals(sign)) {
						value = subMch.group(3);
						cal.add(Calendar.DAY_OF_YEAR, Integer.parseInt(value));
					} else if ("-".equals(sign)) {
						value = subMch.group(1);
						cal.add(Calendar.DAY_OF_YEAR, Integer.parseInt(value));
					}
					dm = TimeUtil.formatDateToStr(cal.getTime(), TimeUtil.SHORT_YEARMONTH_FORMAT);
					dt = TimeUtil.formatDateToStr(cal.getTime(), TimeUtil.SHORT_DAY_FORMAT);
					sbf.append("dm='" + dm + "',dt='" + dt + "'");
					json.put(group, sbf);
				} else if ((subMch = PrestoContent.PARTITION_YEARMONTH.matcher(upperGroup)).find()) {
					cal = Calendar.getInstance();
					sign = subMch.group(2);
					StringBuffer sbf = new StringBuffer();
					if ("+".equals(sign)) {
						value = subMch.group(3);
						cal.add(Calendar.MONTH, Integer.parseInt(value));
					} else if ("-".equals(sign)) {
						value = subMch.group(1);
						cal.add(Calendar.MONTH, Integer.parseInt(value));
					}
					dm = TimeUtil.formatDateToStr(cal.getTime(), TimeUtil.SHORT_YEARMONTH_FORMAT);
					sbf.append("dm='" + dm + "'");
					json.put(group, sbf);
				} else if ((subMch = PrestoContent.PARTITION_DAY.matcher(upperGroup)).find()) {
					cal = Calendar.getInstance();
					sign = subMch.group(2);
					StringBuffer sbf = new StringBuffer();
					if ("+".equals(sign)) {
						value = subMch.group(3);
						cal.add(Calendar.DAY_OF_YEAR, Integer.parseInt(value));
					} else if ("-".equals(sign)) {
						value = subMch.group(1);
						cal.add(Calendar.DAY_OF_YEAR, Integer.parseInt(value));
					}
					dt = TimeUtil.formatDateToStr(cal.getTime(), TimeUtil.SHORT_FORMAT);
					sbf.append("dt='" + dt + "'");
					json.put(group, sbf);
				}
			}
		}catch (ParseException | JSONException e) {
			log.error(ExceptionUtils.getStackTrace(e));
		}
		constMap.put("constConditions", json);
		return constMap;
	}
	
	private static Map<String,String> extractCondition(Map<String,String> conditionMap){
		Map<String,String> fresult = new HashMap<String,String>();
		Iterator<Map.Entry<String, String>> iter = conditionMap.entrySet().iterator();
		Map.Entry<String, String> entry = null;
		Matcher mch = null;
		while(iter.hasNext()){
			entry = iter.next();
			mch = PrestoContent.CONDITION_PT.matcher(entry.getKey());
			if(mch.find()){
				fresult.put(entry.getKey(), entry.getValue());
			}
		}
		return fresult;
	}
	public static String parseHiveCommand(String cmd, Map<String, String> map, int times) {
		String result = cmd;
		int k = 0;
		do {
			if (MapUtils.isNotEmpty(map)) {
				String[] searchList = map.keySet().toArray(new String[] {});
				String[] valueList = map.values().toArray(new String[] {});
				result = StringUtils.replaceEach(result, searchList, valueList);
			}
			k++;
		} while (k < times);
		return result;
	}

//	public static void main(String[]args) throws JSONException {
//		Map<String,String> conditionMap = new HashMap<String,String>();
//		conditionMap.put("{ty}", "1");
//		System.out.println(parseConst("$p_DT-1$", conditionMap));
////		System.out.println(parseConst("$ym_d-1$",conditionMap));
//
//		JSONObject result= (JSONObject) parseConst("$between last 5 and {ty} hour$   $last {ty} hour$ ", conditionMap).get("constConditions");
//		Iterator keys=result.keys();
//		while (keys.hasNext()){
//			String key= (String) keys.next();
//			System.out.println("Key= "+key+ " replace value= "+result.getString(key));
//		}
//// System.out.println(parseConst("$between last 5 and {ty} hour$", conditionMap));
//		System.out.println(parseConst("$last {ty} hour$", conditionMap));
//		System.out.println(parseConst("$last 24 hour$", conditionMap));
//		System.out.println(parseConst("$last 48 hour$", conditionMap));
//		System.out.println(parseConst("$last 24 hour from 2012-03-12 10:00$",conditionMap));
//
//	}
}
