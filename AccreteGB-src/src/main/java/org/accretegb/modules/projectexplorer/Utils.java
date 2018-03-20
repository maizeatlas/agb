package org.accretegb.modules.projectexplorer;
/*
 * Licensed to Openaccretegb-common under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Openaccretegb-common licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

/**
 * @author nkumar
 * Utilities for project creation
 */
public class Utils {

    public static final String SEPERATOR = ".";
    /**
     * creates String path from treeNodes
     * @param projectNodes - treeNodes
     * @return - path in String format
     */
    public static String getPathStr(TreeNode[] projectNodes) {
        String str = new String();
        for (int counter = 0; counter < projectNodes.length; counter++) {
            if (counter == projectNodes.length - 1) {
                str = str + projectNodes[counter];
                continue;
            }
            str = str + projectNodes[counter] + SEPERATOR;
        }
        return str;
    }

    /**
     * gets path from the current node to high up in the Tree in
     * TreePath format
     * @param treeNode - current TreeNode
     * @return - TreePath from the current treeNode
     */
    public static TreePath getTreePath(TreeNode treeNode) {
        List<Object> nodes = new ArrayList<Object>();
        if (treeNode != null) {
            nodes.add(treeNode);
            treeNode = treeNode.getParent();
            while (treeNode != null) {
                nodes.add(0, treeNode);
                treeNode = treeNode.getParent();
            }
        }

        return nodes.isEmpty() ? null : new TreePath(nodes.toArray());
    }
}
