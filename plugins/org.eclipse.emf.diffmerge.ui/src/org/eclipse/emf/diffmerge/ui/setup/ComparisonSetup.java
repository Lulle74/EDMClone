/**
 * <copyright>
 * 
 * Copyright (c) 2010-2017 Thales Global Services S.A.S.
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
package org.eclipse.emf.diffmerge.ui.setup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.compare.IPropertyChangeNotifier;
import org.eclipse.emf.diffmerge.api.Role;
import org.eclipse.emf.diffmerge.ui.EMFDiffMergeUIPlugin;
import org.eclipse.emf.diffmerge.ui.specification.IComparisonMethod;
import org.eclipse.emf.diffmerge.ui.specification.IComparisonMethodFactory;
import org.eclipse.emf.diffmerge.ui.specification.IModelScopeDefinition;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;


/**
 * A simple structure that associates scope definitions with corresponding
 * compatible comparison method factories and allows selecting one of the factories
 * for creating a comparison method.
 * @author Olivier Constant
 */
public class ComparisonSetup implements IPropertyChangeNotifier {
  
  /** An identifier for changes to the roles of the scope definitions */
  public static final String PROPERTY_ROLES = "ComparisonSetup.Property.Roles"; //$NON-NLS-1$
  
  /** An identifier for changes to the roles of the scope definitions */
  public static final String PROPERTY_COMPARISON_METHOD =
    "ComparisonSetup.Property.ComparisonMethod"; //$NON-NLS-1$
  
  /** The initially null lastly used comparison method factory */
  protected static IComparisonMethodFactory __lastComparisonMethodFactory = null;
  
  /** The map from roles to the corresponding scope definitions */
  private final Map<Role, IModelScopeDefinition> _roleToScopeDefinition;
  
  /** Whether scope definitions can be swapped by the end user */
  private boolean _canSwapScopeDefinitions;
  
  /** The potentially null role to use as a reference in a two-way comparison */
  private Role _twoWayReferenceRole;
  
  /** Whether the two-way reference role can be changed by the end user */
  private boolean _canChangeTwoWayReferenceRole;
  
  /** The non-null, non-empty list of applicable method factories */ 
  private final List<IComparisonMethodFactory> _compatibleMethodFactories;
  
  /** The potentially null selected factory (among the compatible factories) */ 
  private IComparisonMethodFactory _selectedFactory;
  
  /** The potentially null comparison method */ 
  protected IComparisonMethod _comparisonMethod;
  
  /** A non-null set of listeners on this object */
  protected final Set<IPropertyChangeListener> _listeners;
  
  
  /**
   * Constructor
   * @param method_p a non-null pre-selected and configured comparison method
   */
  public ComparisonSetup(IComparisonMethod method_p) {
    this(
        method_p.getModelScopeDefinition(Role.TARGET),
        method_p.getModelScopeDefinition(Role.REFERENCE),
        method_p.getModelScopeDefinition(Role.ANCESTOR),
        EMFDiffMergeUIPlugin.getDefault().getSetupManager().getApplicableComparisonMethodFactories(
            method_p.getModelScopeDefinition(Role.TARGET),
            method_p.getModelScopeDefinition(Role.REFERENCE),
            method_p.getModelScopeDefinition(Role.ANCESTOR))
        );
    _comparisonMethod = method_p;
    _twoWayReferenceRole = method_p.getTwoWayReferenceRole();
    _selectedFactory = method_p.getFactory();
  }
  
  /**
   * Constructor
   * @param scopeSpec1_p a non-null scope definition
   * @param scopeSpec2_p a non-null scope definition
   * @param scopeSpec3_p a potentially null scope definition
   * @param compatibleFactories_p a non-null, non-empty list
   */
  public ComparisonSetup(IModelScopeDefinition scopeSpec1_p, IModelScopeDefinition scopeSpec2_p,
      IModelScopeDefinition scopeSpec3_p, List<IComparisonMethodFactory> compatibleFactories_p) {
    this();
    _roleToScopeDefinition.put(Role.TARGET, scopeSpec1_p);
    _roleToScopeDefinition.put(Role.REFERENCE, scopeSpec2_p);
    _roleToScopeDefinition.put(Role.ANCESTOR, scopeSpec3_p);
    _compatibleMethodFactories.addAll(compatibleFactories_p);
  }
  
  /**
   * Default constructor
   */
  protected ComparisonSetup() {
    _comparisonMethod = null;
    _compatibleMethodFactories = new ArrayList<IComparisonMethodFactory>();
    _canSwapScopeDefinitions = true;
    _canChangeTwoWayReferenceRole = true;
    _roleToScopeDefinition = new HashMap<Role, IModelScopeDefinition>();
    _twoWayReferenceRole = null;
    _listeners = new HashSet<IPropertyChangeListener>();
  }
  
  /**
   * Return whether the two-way reference role can be changed by the end user
   */
  public boolean canChangeTwoWayReferenceRole() {
    return _canChangeTwoWayReferenceRole;
  }
  
  /**
   * Return whether scope definitions can be swapped by the end user
   */
  public boolean canSwapScopeDefinitions() {
    return _canSwapScopeDefinitions;
  }
  
  /**
   * Return the list of comparison factories which are compatible with the
   * scope definitions
   * @return a non-null, non-empty list
   */
  public List<IComparisonMethodFactory> getApplicableComparisonMethodFactories() {
    return Collections.unmodifiableList(_compatibleMethodFactories);
  }
  
  /**
   * Return the comparison method created by the selected factory, if any
   * @return a potentially null object
   */
  public IComparisonMethod getComparisonMethod() {
    return _comparisonMethod;
  }
  
  /**
   * Return the scope definition that plays the given role
   * @param role_p a non-null role
   * @return a scope definition which may only be null if role is ANCESTOR
   */
  public IModelScopeDefinition getScopeDefinition(Role role_p) {
    return _roleToScopeDefinition.get(role_p);
  }
  
  /**
   * Return the selected factory among the compatible ones
   * @return a potentially null factory
   */
  public IComparisonMethodFactory getSelectedFactory() {
    return _selectedFactory;
  }
  
  /**
   * @see org.eclipse.emf.diffmerge.ui.specification.IComparisonMethod#getTwoWayReferenceRole()
   */
  public Role getTwoWayReferenceRole() {
    return _twoWayReferenceRole;
  }
  
  /**
   * Return whether the comparison will be three-way
   */
  public boolean isThreeWay() {
    return getScopeDefinition(Role.ANCESTOR) != null;
  }
  
  /**
   * Notify listeners of the given property change event
   * @param event_p a non-null event
   */
  protected void notify(PropertyChangeEvent event_p) {
    for (IPropertyChangeListener listener : _listeners) {
      listener.propertyChange(event_p);
    }
  }
  
  /**
   * Update the current comparison method with all available information
   * @param remember_p whether the configuration of this setup must be remembered.
   *          It affects the behavior of setSelectedFactoryToLast().
   */
  public void performFinish(boolean remember_p) {
    if (_comparisonMethod != null) {
      IComparisonMethodFactory selectedFactory = getSelectedFactory();
      if (remember_p && selectedFactory != null)
        __lastComparisonMethodFactory = selectedFactory;
      if (!isThreeWay())
        _comparisonMethod.setTwoWayReferenceRole(getTwoWayReferenceRole());
    }
  }
  
  /**
   * Set whether the two-way reference role can be changed by the end user
   * @param canChange_p whether it can be changed
   */
  public void setCanChangeTwoWayReferenceRole(boolean canChange_p) {
    _canChangeTwoWayReferenceRole = canChange_p;
  }
  
  /**
   * Set whether scope definitions can be swapped by the end user
   * @param canSwap_p whether they can be swapped
   */
  public void setCanSwapScopeDefinitions(boolean canSwap_p) {
    _canSwapScopeDefinitions = canSwap_p;
  }
  
  /**
   * Set the selected method factory and consequently update the comparison method
   * @param selectedFactory_p a factory which is null or belongs to getCompatibleFactories()
   */
  public void setSelectedFactory(IComparisonMethodFactory selectedFactory_p) {
    _selectedFactory = selectedFactory_p;
    if (_selectedFactory != null) {
      _comparisonMethod = _selectedFactory.createComparisonMethod(
          getScopeDefinition(Role.TARGET), getScopeDefinition(Role.REFERENCE),
          getScopeDefinition(Role.ANCESTOR));
    } else {
      _comparisonMethod = null;
    }
    notify(new PropertyChangeEvent(this, PROPERTY_COMPARISON_METHOD, null, null));
  }
  
  /**
   * Set the selected method factory to the one lastly used, if any and applicable
   * @return whether the operation had any impact
   */
  public boolean setSelectedFactoryToLast() {
    boolean result = false;
    if (__lastComparisonMethodFactory != null &&
        __lastComparisonMethodFactory != getSelectedFactory() &&
        getApplicableComparisonMethodFactories().contains(__lastComparisonMethodFactory)) {
      setSelectedFactory(__lastComparisonMethodFactory);
      result = true;
    }
    return result;
  }
  
  /**
   * Set the role that corresponds to the target of the merge
   * @param role_p TARGET, REFERENCE or null
   */
  public void setTargetRole(Role role_p) {
    setTwoWayReferenceRole(role_p);
    if (Role.TARGET == role_p || Role.REFERENCE == role_p) {
      IModelScopeDefinition roleDef = getScopeDefinition(role_p);
      IModelScopeDefinition oppositeRoleDef = getScopeDefinition(role_p.opposite());
      if (roleDef.isEditableSettable())
        roleDef.setEditable(true);
      oppositeRoleDef.setEditable(false);
    } else {
      // No reference role defined
      IModelScopeDefinition targetRoleDef = getScopeDefinition(Role.TARGET);
      if (targetRoleDef.isEditableSettable())
        targetRoleDef.setEditable(true);
      IModelScopeDefinition referenceRoleDef = getScopeDefinition(Role.REFERENCE);
      if (referenceRoleDef.isEditableSettable())
        referenceRoleDef.setEditable(true);
    }
    notify(new PropertyChangeEvent(this, PROPERTY_ROLES, null, null));
  }
  
  /**
   * @see org.eclipse.emf.diffmerge.ui.specification.IComparisonMethod#setTwoWayReferenceRole(org.eclipse.emf.diffmerge.api.Role)
   */
  public void setTwoWayReferenceRole(Role role_p) {
    if (!isThreeWay())
      _twoWayReferenceRole = role_p;
  }
  
  /**
   * Swap the scope definitions that play the given roles
   * @param role1_p a non-null role
   * @param role2_p a non-null role
   * @return whether the operation succeeded (it may only fail to prevent inconsistencies)
   */
  public boolean swapScopeDefinitions(Role role1_p, Role role2_p) {
    boolean result = true;
    if (_comparisonMethod != null)
      result = _comparisonMethod.swapScopeDefinitions(role1_p, role2_p);
    if (result) {
      IModelScopeDefinition scope1 = getScopeDefinition(role1_p);
      IModelScopeDefinition scope2 = getScopeDefinition(role2_p);
      if (scope1 != null && scope2 != null) {
        _roleToScopeDefinition.put(role1_p, scope2);
        _roleToScopeDefinition.put(role2_p, scope1);
      }
      notify(new PropertyChangeEvent(this, PROPERTY_ROLES, null, null));
    }
    return result;
  }
  
  /**
   * @see org.eclipse.compare.IPropertyChangeNotifier#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
   */
  public void addPropertyChangeListener(IPropertyChangeListener listener_p) {
    _listeners.add(listener_p);
  }
  
  /**
   * @see org.eclipse.compare.IPropertyChangeNotifier#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
   */
  public void removePropertyChangeListener(IPropertyChangeListener listener_p) {
    _listeners.remove(listener_p);
  }
  
  /**
   * Remove all property change listeners
   */
  public void removePropertyChangeListeners() {
    _listeners.clear();
  }
  
}