/**
 * <copyright>
 * 
 * Copyright (c) 2014-2017 Thales Global Services S.A.S.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Thales Global Services S.A.S. - initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.emf.diffmerge.ui.viewers;

import org.eclipse.emf.diffmerge.ui.Messages;
import org.eclipse.emf.diffmerge.ui.util.DelegatingLabelProvider;
import org.eclipse.emf.diffmerge.ui.viewers.MergeImpactViewer.ImpactInput;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


/**
 * A message dialog which shows a MergeImpactViewer.
 * @see MergeImpactViewer
 * @author Olivier Constant
 */
public class MergeImpactMessageDialog extends MessageDialog {
  
  /** The non-null input */
  protected final ImpactInput _dialogInput;
  
  /** The non-null resource manager for SWT resources in this viewer */
  protected final ComparisonResourceManager _resourceManager;
  
  /** The optional label provider */
  protected final IBaseLabelProvider _labelProvider;
  
  
  /**
   * Constructor
   * @param parentShell_p a non-null shell
   * @param input_p a non-null input to represent
   * @param resourceManager_p a non-null resource manager for SWT resources
   * @param labelProvider_p an optional label provider
   */
  public MergeImpactMessageDialog(Shell parentShell_p, ImpactInput input_p,
      ComparisonResourceManager resourceManager_p, IBaseLabelProvider labelProvider_p) {
    super(parentShell_p, Messages.ComparisonViewer_MergeHeader, null,
        String.format(
            Messages.ComparisonViewer_ImpactDescription, input_p.isOnTheLeft()?
                Messages.ComparisonViewer_Left: Messages.ComparisonViewer_Right),
        MessageDialog.INFORMATION,
        new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0);
    _dialogInput = input_p;
    _resourceManager = resourceManager_p;
    _labelProvider = labelProvider_p;
    setShellStyle(getShellStyle() | SWT.RESIZE);
  }
  
  /**
   * @see MessageDialog#createCustomArea(Composite)
   */
  @Override
  protected Control createCustomArea(Composite parent_p) {
    MergeImpactViewer viewer = new MergeImpactViewer(parent_p, _resourceManager);
    // Reuse label provider from synthesis viewer
    if (_labelProvider instanceof DelegatingLabelProvider)
      viewer.setDelegateLabelProvider(((DelegatingLabelProvider)_labelProvider).getDelegate());
    viewer.setInput(_dialogInput);
    return viewer.getControl();
  }
  
  /**
   * Open the dialog and return whether the user pressed OK
   */
  public boolean openAndConfirm() {
    return open() == Window.OK;
  }
  
}