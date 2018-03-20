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

import javax.swing.tree.DefaultMutableTreeNode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author nkumar
 * This class stores the higher level things related to a project
 */
public class ProjectTree {

    private String projectName;
    private ProjectTreeNode projectRootNode;
    private ProjectTreeNode stockSelectionNode;
    private ProjectTreeNode experimentNode;
    private ProjectTreeNode plantingNode;
    private ProjectTreeNode phenotypeNode;
    private ProjectTreeNode samplingNode;
    private ProjectTreeNode harvestingNode;
   
	public ProjectTree(String projectName) {
        this.projectName = projectName;
        this.projectRootNode = new ProjectTreeNode(projectName);
        this.projectRootNode.setType(ProjectTreeNode.NodeType.PROJECT_NODE);

        List<ProjectTreeNode> parents = new ArrayList<ProjectTreeNode>();
        parents.add(projectRootNode);

        this.stockSelectionNode = new ProjectTreeNode(ProjectConstants.STOCK_SELECTION);
        this.stockSelectionNode.setType(ProjectTreeNode.NodeType.STOCK_PACKING_PARENT);
        this.stockSelectionNode.setParentNodes(parents);
        this.projectRootNode.add(stockSelectionNode);

        this.experimentNode = new ProjectTreeNode(ProjectConstants.EXPERIMENTS);
        this.experimentNode.setType(ProjectTreeNode.NodeType.EXPERIMENTAL_DESIGN_PARENT);
        this.experimentNode.setParentNodes(parents);
        this.projectRootNode.add(experimentNode);

        this.plantingNode = new ProjectTreeNode(ProjectConstants.PLANTINGS);
        this.plantingNode.setType(ProjectTreeNode.NodeType.PLANTING_PARENT);
        this.plantingNode.setParentNodes(parents);
        this.projectRootNode.add(plantingNode);

        this.phenotypeNode = new ProjectTreeNode(ProjectConstants.PHENOTYPE);
        this.phenotypeNode.setType(ProjectTreeNode.NodeType.PHENOTYPE_NODE);
        this.phenotypeNode.setParentNodes(parents);
        this.projectRootNode.add(phenotypeNode);
        
        this.samplingNode = new ProjectTreeNode(ProjectConstants.SAMPLING);
        this.samplingNode.setType(ProjectTreeNode.NodeType.SAMPLING_NODE);
        this.samplingNode.setParentNodes(parents);
        this.projectRootNode.add(samplingNode);

        this.harvestingNode = new ProjectTreeNode(ProjectConstants.HARVESTINGS);
        this.harvestingNode.setType(ProjectTreeNode.NodeType.HARVESTING_PARENT);
        this.harvestingNode.setParentNodes(parents);
        this.projectRootNode.add(harvestingNode);

    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    public DefaultMutableTreeNode getProjectRootNode() {
        return projectRootNode;
    }

    public ProjectTreeNode getStockSelectionNode() {
        return stockSelectionNode;
    }

    public void setStockSelectionNode(ProjectTreeNode stockSelectionNode) {
        this.stockSelectionNode = stockSelectionNode;
    }

    public ProjectTreeNode getExperimentNode() {
        return experimentNode;
    }

    public void setExperimentNode(ProjectTreeNode experimentNode) {
        this.experimentNode = experimentNode;
    }

    public ProjectTreeNode getPlantingNode() {
        return plantingNode;
    }
    
    public ProjectTreeNode getPhenotypeNode() {
		return phenotypeNode;
	}
    
    public ProjectTreeNode getSamplingNode() {
		return samplingNode;
	}

    public void setPlantingNode(ProjectTreeNode plantingNode) {
        this.plantingNode = plantingNode;
    }
    
    public void setPhenotypeNode(ProjectTreeNode phenotypeNode) {
        this.phenotypeNode = phenotypeNode;
    }
    
    public void setSamplingNode(ProjectTreeNode samplingNode) {
        this.samplingNode = samplingNode;
    }

    public ProjectTreeNode getHarvestingNode() {
        return harvestingNode;
    }

    public void setHarvestingNode(ProjectTreeNode harvestingNode) {
        this.harvestingNode = harvestingNode;
    }

    public void setProjectRootNode(ProjectTreeNode projectRootNode) {
        this.projectRootNode = projectRootNode;
    }
    
    

	

}
