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
import org.eclipse.emf.diffmerge.ui.util.UIUtil;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;


/**
 * A ComparisonTreeViewer with a header.
 * Input, Elements: see ComparisonTreeViewer.
 * @see ComparisonTreeViewer
 * @author Olivier Constant
 */
public class EnhancedComparisonTreeViewer extends HeaderViewer<ComparisonTreeViewer> {
  
  /**
   * Constructor
   * @param parent_p a non-null composite
   */
  public EnhancedComparisonTreeViewer(Composite parent_p) {
    super();
    createControls(parent_p); 
  }
  
  /**
   * @see org.eclipse.emf.diffmerge.ui.viewers.HeaderViewer#createImageLabel(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Label createImageLabel(Composite parent_p) {
    return null;
  }
  
  /**
   * @see org.eclipse.emf.diffmerge.ui.viewers.HeaderViewer#createInnerViewer(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected ComparisonTreeViewer createInnerViewer(Composite parent_p) {
    return new ComparisonTreeViewer(parent_p);
  }
  
  /**
   * @see org.eclipse.emf.diffmerge.ui.viewers.HeaderViewer#createTextLabel(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Label createTextLabel(Composite parent_p) {
    Label result = super.createTextLabel(parent_p);
    result.setFont(UIUtil.getBold(result.getFont()));
    result.setText(getDefaultHeaderText());
    return result;
  }
  
  /**
   * Return the default text for the header
   * @return a non-null string
   */
  public String getDefaultHeaderText() {
    return Messages.EnhancedComparisonTreeViewer_DefaultHeader;
  }
  
  /**
   * @see org.eclipse.emf.diffmerge.ui.viewers.HeaderViewer#getInput()
   */
  @Override
  public EMFDiffNode getInput() {
    return (EMFDiffNode)super.getInput();
  }
  
  /**
   * @see org.eclipse.emf.diffmerge.ui.viewers.HeaderViewer#getSelection()
   */
  @Override
  public ITreeSelection getSelection() {
    return (ITreeSelection)super.getSelection();
  }
  
}
