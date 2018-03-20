package org.accretegbR.experimental;

import org.accretegbR.main.Utils;

public class RandomizedCompleteBlockDesign extends ExperimentDesign {
	
	public StringBuffer buildRCode() {
		StringBuffer sb = new StringBuffer();
		//sb.append(Utils.getRCodeHeader().getCode());
		sb.append("library(agricolae);\n");
		sb.append("trt<-c(" + convertListToString(getTreatments().get(0)) + ");\n");
		sb.append("rcbd <- design.rcbd(trt, " + getReps() + ", serie=2, " + getSeeds() + ", \"" + getMethodName() + "\");\n");
		sb.append("plots <- as.numeric(rcbd$book[,1]);\n");
		sb.append("blocks <- as.numeric(rcbd$book[,2]);\n");
		sb.append("trts <- as.character(rcbd$book[,3]);\n");
		sb.append("my.all <- list(my.plots=plots, my.trts=trts, my.blocks=blocks);\n");
		return sb;
    }
}