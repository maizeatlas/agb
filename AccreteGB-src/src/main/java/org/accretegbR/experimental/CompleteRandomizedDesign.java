package org.accretegbR.experimental;

public class CompleteRandomizedDesign extends ExperimentDesign {

	@Override
	public StringBuffer buildRCode() {
		StringBuffer sb = new StringBuffer();
		sb.append("library(agricolae);");
		sb.append("trt<-c(" + convertListToString(getTreatments().get(0)) + ");");
		sb.append("crd <- design.crd(trt, " + getReps() + ", serie=2, " + getSeeds() + ", \"" + getMethodName() + "\");");
		sb.append("plots <- as.numeric(crd$book[,1]);");
		sb.append("blocks <- as.numeric(crd$book[,2]);");
		sb.append("trts <- as.character(crd$book[,3]);");
		sb.append("my.all <- list(my.plots=plots, my.trts=trts, my.blocks=blocks);");
		return sb;
	}

	
}
