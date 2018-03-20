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

import org.accretegb.modules.tab.TabComponent;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;

/**
 * @author nkumar
 * This node  stores defines the nodes of the Tree
 * and type of node
 */
public class ProjectTreeNode extends DefaultMutableTreeNode {

    private static final long serialVersionUID = 1L;

    private List<ProjectTreeNode> parentNodes;
    private NodeType type;
    private String nodeName;
    private TabComponent tabComponent;
    private boolean modified = false;

    public ProjectTreeNode(String nodeName) {
        super(nodeName);
        this.nodeName = nodeName;
    }

    public enum NodeType {

        STOCK_SELECTION_PARENT(ProjectConstants.STOCK_SELECTION),
        EXPERIMENTAL_DESIGN_PARENT(ProjectConstants.EXPERIMENTS),
        PLANTING_PARENT(ProjectConstants.PLANTINGS),
        PHENOTYPE_PARENT(ProjectConstants.PHENOTYPE),
        HARVESTING_PARENT(ProjectConstants.HARVESTINGS),
        STOCK_PACKING_PARENT(ProjectConstants.STOCK_PACKAGING),
        STOCK_SELECTION_NODE(ProjectConstants.STOCK_SELECTION_NODE),
        EXPERIMENTAL_DESIGN_NODE(ProjectConstants.EXPERIMENTS_NODE),
        PLANTING_NODE(ProjectConstants.PLANTINGS_NODE),
        PHENOTYPE_NODE(ProjectConstants.PHENOTYPE_NODE),
        SAMPLING_NODE(ProjectConstants.SAMPLING_NODE),
        HARVESTING_NODE(ProjectConstants.HARVESTINGS_NODE),
        STOCK_PACKING_NODE(ProjectConstants.STOCK_PACKAGING_NODE),
        PROJECT_NODE(ProjectConstants.PROJECT_NODE);
        private String type;

        private NodeType(String type) {
            this.type = type;
        }

        public String getNodeType() {
            return type;
        }
    }

    public List<ProjectTreeNode> getParentNodes() {
        return parentNodes;
    }

    public void setParentNodes(List<ProjectTreeNode> parentNodes) {
        this.parentNodes = parentNodes;
    }

    public TabComponent getTabComponent() {
        return tabComponent;
    }

    public void setTabComponent(TabComponent tabComponent) {
        this.tabComponent = tabComponent;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }
    

    public boolean isModified() {
		return modified;
	}

	public void setModified(boolean modified) {
		this.modified = modified;
	}



}
