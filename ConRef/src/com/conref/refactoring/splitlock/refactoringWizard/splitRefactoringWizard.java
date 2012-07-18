package com.conref.refactoring.splitlock.refactoringWizard;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard
;

public class splitRefactoringWizard extends RefactoringWizard {

	public splitRefactoringWizard(Refactoring refactoring) {
		super(refactoring, WIZARD_BASED_USER_INTERFACE);
		
	}

	@Override
	protected void addUserInputPages() {}

}
