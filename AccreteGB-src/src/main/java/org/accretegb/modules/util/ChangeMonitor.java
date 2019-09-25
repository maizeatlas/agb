package org.accretegb.modules.util;

import java.util.HashMap;

public class ChangeMonitor {
	public static HashMap<Integer, Boolean> changedProject = new HashMap<Integer, Boolean>();
	public static HashMap<Integer, String> projectIdName = new HashMap<Integer, String>();
	
	public static void markAsChanged(int projectID){
		if (changedProject.containsKey(projectID)) {
			if (!ChangeMonitor.changedProject.get(projectID)) {
				ChangeMonitor.changedProject.put(projectID, true);
			}
		}
	}

}
