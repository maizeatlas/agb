package org.accretegb.modules.util;

import java.util.HashMap;

import org.json.JSONObject;

public class GlobalProjectInfo {
	private static String plantingKey = "planting";
	private static String samplingKey = "sampling";
	
	private static HashMap<Integer, HashMap<String, HashMap<String, Object>>> projectInfo = new HashMap<Integer, HashMap<String, HashMap<String, Object>>>();
	
	public static HashMap<Integer, HashMap<String, HashMap<String, Object>>> getProjectInfo () {
		return projectInfo;
	}
	
	public static void insertNewProject (int projectId) {
		projectInfo.put(projectId, new HashMap<String, HashMap<String, Object>>());
	}
	
	public static void insertNewPlantingInfo(int projectId, String key, Object value) {
		if (!projectInfo.containsKey(projectId)) {
			insertNewProject(projectId);
		}
		HashMap<String, HashMap<String, Object>> info = projectInfo.get(projectId);
		if (!info.containsKey(plantingKey)) {
			info.put(plantingKey, new HashMap<String, Object>());
		}
		projectInfo.get(projectId).get(plantingKey).put(key, value);
		JSONObject toPrint = new JSONObject( GlobalProjectInfo.getProjectInfo());
		System.out.println("Current Project Info - " + toPrint);
	}
	
	public static void insertNewSamplingInfo(int projectId, String key, Object value) {
		if (!projectInfo.containsKey(projectId)) {
			insertNewProject(projectId);
		}
		HashMap<String, HashMap<String, Object>> info = projectInfo.get(projectId);
		if (!info.containsKey(samplingKey)) {
			info.put(samplingKey, new HashMap<String, Object>());
		}
		projectInfo.get(projectId).get(samplingKey).put(key, value);
		JSONObject toPrint = new JSONObject( GlobalProjectInfo.getProjectInfo());
		System.out.println("Current Project Info - " + toPrint);
	}
	
	public static Object getPlantingInfo(int projectId, String key) {
		if (projectInfo.containsKey(projectId)) {
			if(projectInfo.get(projectId).containsKey(plantingKey)) {
				return projectInfo.get(projectId).get(plantingKey).get(key);
			}
		}
		return null;
	}
	
	public static Object getSamplingInfo(int projectId, String key) {
		if (projectInfo.containsKey(projectId)) {
			if(projectInfo.get(projectId).containsKey(samplingKey)) {
				return projectInfo.get(projectId).get(samplingKey).get(key);
			}
		}
		return null;
	}

}
