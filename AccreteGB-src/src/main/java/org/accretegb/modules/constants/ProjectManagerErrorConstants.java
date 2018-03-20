package org.accretegb.modules.constants;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class ProjectManagerErrorConstants {
	private static final String collaboratorRemovedError = "You were removed from collaborators.";
	private static final String removeProjectOwnerError = "   You are project owner !";
	private static final String notProjectOwner= "Only project owners can edit collaborators.";
	private static final String notProjectOwnerDeleteError = "Only project owners can delete project";
	private final static String deleteProjectError = "Couldn't delete project: ";
	private final static String returnProjectError = "Can't return project: ";
	private final static String noTokenToReturnError = "No body has this token: ";
	private final static String notOwnerOfTokenToReturnError = "Not owner of this project's token : ";
	private final static String checkOutProjectError = "Can't checkout project: ";
	private final static String projectAlreadyCheckedOutError = "Can't add this token, already checked out: ";
	private final static String projectOwnerDeleteProjectWithTokenOutError = "Can't delete the project that has a token out";
	private final static String deleteCollaboratorError = "Couldn't delete collaboration";
	private final static String existingCollaboratorError = "Can't add a collaborator who already is a collaborator for ";
	private final static String noProjectSelectedError = "There's no project selected";
	private final static String couldNotPerformActionError = "Could not perform action";

	public static void promptDeleteProjectError(String projectName) {
		JOptionPane.showMessageDialog(new JFrame(), deleteProjectError
				+ projectName, "Dialog", JOptionPane.ERROR_MESSAGE);
	}

	public static void promptReturnProjectError(String projectName) {
		JOptionPane.showMessageDialog(new JFrame(), returnProjectError
				+ projectName, "Dialog", JOptionPane.ERROR_MESSAGE);
	}

	public static void promptNoTokenToReturnError(String projectName) {
		JOptionPane.showMessageDialog(new JFrame(), noTokenToReturnError
				+ projectName, "Dialog", JOptionPane.ERROR_MESSAGE);
	}

	public static void promptNotOwnerOfTokenToReturnError(String projectName) {
		JOptionPane.showMessageDialog(new JFrame(), notOwnerOfTokenToReturnError 
				+ projectName, "Dialog", JOptionPane.ERROR_MESSAGE);
	}

	public static void promptCheckoutProjectError(String projectName) {
		JOptionPane.showMessageDialog(new JFrame(), checkOutProjectError
				+ projectName, "Dialog", JOptionPane.ERROR_MESSAGE);
	}

	public static void promptProjectAlreadyCheckedOutError(String projectName) {
		JOptionPane.showMessageDialog(new JFrame(),
				projectAlreadyCheckedOutError + projectName, "Dialog",
				JOptionPane.ERROR_MESSAGE);
	}

	public static void promptProjectOwnerDeleteProjectWithTokenOutError(String projectName) {
		JOptionPane.showMessageDialog(new JFrame(),
				projectOwnerDeleteProjectWithTokenOutError + projectName,
				"Dialog", JOptionPane.ERROR_MESSAGE);
	}

	public static void promptDeleteCollaboratorError(String collaborator) {
		JOptionPane.showMessageDialog(new JFrame(),
				deleteCollaboratorError + collaborator, "Dialog", JOptionPane.ERROR_MESSAGE);
	}

	public static void promptExistingCollaboratorError(String projectName,String collaboratorName) {
		JOptionPane.showMessageDialog(new JFrame(), existingCollaboratorError
				+ projectName + " : " + collaboratorName, "Dialog",
				JOptionPane.ERROR_MESSAGE);
	}

	public static void promptNoProjectSelectedError() {
		JOptionPane.showMessageDialog(new JFrame(), noProjectSelectedError,
				"Dialog", JOptionPane.ERROR_MESSAGE);
	}

	public static void promptCouldNotPerformActionError() {
		JOptionPane.showMessageDialog(new JFrame(), couldNotPerformActionError,
				"Dialog", JOptionPane.ERROR_MESSAGE);
	}
	

	public static void promptNotProjectOwner() {
		JOptionPane.showMessageDialog(new JFrame(), notProjectOwner,
				"Dialog", JOptionPane.PLAIN_MESSAGE);
	}

	public static void promptCannotRemoveSelf() {
		
		JOptionPane.showMessageDialog(new JFrame(), removeProjectOwnerError,
				"Dialog", JOptionPane.PLAIN_MESSAGE);
		
	}

	public static void promptCollaboratorRemovedError(String projectName) {
		JOptionPane.showMessageDialog(new JFrame(), collaboratorRemovedError,
				"Dialog", JOptionPane.PLAIN_MESSAGE);
		
	}

	public static void promptNotProjectOwnerDeleteError() {
		JOptionPane.showMessageDialog(new JFrame(), notProjectOwnerDeleteError,
				"Dialog", JOptionPane.PLAIN_MESSAGE);
		
	}

}
