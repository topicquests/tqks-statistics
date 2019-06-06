/*
 * Copyright 2017, TopicQuests
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.topicquests.ks;

import java.io.*;
import java.util.Iterator;
import java.util.Map;

import org.topicquests.os.asr.api.IStatisticsClient;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;
import org.topicquests.support.config.Configurator;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

/**
 * @author jackpark
 * <p>Simply a counter against any Key</p>
 */
public class StatisticsUtility implements IStatisticsClient {
	private static StatisticsUtility instance = null;
	private final String PATH;
	private JSONObject data;
	private boolean isSaved = false;

	/**
	 * 
	 */
	private StatisticsUtility() throws Exception {
		Map<String,Object>properties = Configurator.getProperties("stats-config.xml");
		PATH = (String)properties.get("StatsPath");
		bootData();
		isSaved = false;
	}

	/**
	 * The only way to get this as a singleton
	 * @return
	 * @throws Exception
	 */
	public static StatisticsUtility getInstance() throws Exception {
		if (instance == null)
			instance = new StatisticsUtility();
		return instance;
	}
	
	/**
	 * General purpose: any routine can choose a key
	 * and add to it, avoiding the reserved keys defined here
	 * @param key
	 */
	public IResult addToKey(String key) {
		//reset isSaved if someone is using this after it was saved
		if (isSaved)
			isSaved = false;
		synchronized(data) {
			Long v = (Long)data.get(key);
			if (v == null)
				v = new Long(1);
			else
				v += 1;
			data.put(key, v);
		}
		return null;
	}
	
	/**
	 * Called as needed, typically at shutDown time
	 * @throws Exception
	 */
	public void saveData() throws Exception {
		if (isSaved)
			return;
		synchronized(data) {
			File f = new File(PATH);
			FileOutputStream fos = new FileOutputStream(f);
			OutputStreamWriter out = new OutputStreamWriter(fos);
			data.writeJSONString(out);
			out.close();
			isSaved = true;
		}
	}
	
	/**
	 * Formatted view of data
	 * @return
	 */
	public String getStats() {
		System.out.println("GETSTATS "+data);
		synchronized(data) {
			StringBuffer buf = new StringBuffer();
			Long x = null;
			String key;
			Iterator<String>itr = data.keySet().iterator();
			while (itr.hasNext()) {
				key = itr.next();
				x = (Long)data.get(key);
				if (x != null) {
					buf = buf.append(key).append(": ").append(x.toString()).append("\n");
				}
			}
			return buf.toString();
		}
	}
	/**
	 * A way to grab snapshots of the stats
	 * @return
	 */
	public String getDataAsString() {
		synchronized(data) {
			return data.toJSONString();
		}
	}
	
	void bootData() throws Exception {
		File f = new File(PATH);
		if (f.exists()) {
			FileInputStream fis = new FileInputStream(f);
			JSONParser p = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
			data = (JSONObject)p.parse(fis);
			fis.close();
		} else
			data = new JSONObject();
	}

	@Override
	public IResult getStatistics() {
		IResult result = new ResultPojo();
		JSONObject jo = new JSONObject();
		JSONParser p = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
		synchronized(data) {
			try {
				// clone data
				jo = (JSONObject)p.parse(data.toJSONString());
				result.setResultObject(jo);
			} catch (Exception e) {
				e.printStackTrace();
				result.addErrorString(e.getMessage());
			}
		}
		return result;
	}

	@Override
	public IResult getValueOfKey(String key) {
		// TODO Auto-generated method stub
		return null;
	}

}
