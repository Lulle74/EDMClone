/**
 * Copyright (c) 2015 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephane Bouchet (Intel Corporation) - initial API and implementation
 *    Olivier Constant (Thales Global Services) - tight integration
 */
package org.eclipse.emf.diffmerge.connector.core.ext;

import java.io.InputStream;

import org.eclipse.emf.diffmerge.api.scopes.IEditableModelScope;
import org.eclipse.emf.diffmerge.api.scopes.IPersistentModelScope;
import org.eclipse.emf.diffmerge.impl.scopes.AbstractModelScope;
import org.eclipse.emf.diffmerge.ui.specification.IModelScopeDefinition;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;


/**
 * A model scope definition that wraps a regular model scope definition and alters
 * its behavior through a given URI Converter.
 */
public class URIConvertingScopeDefinition implements IModelScopeDefinition {
  
  /** The non-null wrapped model scope definition */
  protected final IModelScopeDefinition _wrapped;
  
  /** The non-null URI converter */
  protected final URIConverter _uriConverter;
  
  /** The optional input stream for loading the scope */
  protected InputStream _loadingStream;
  
  /** The optional label for the scope */
  protected final String _label;
  
  
  /**
   * Constructor
   * @param wrapped_p a non-null model scope definition wrapped by this one
   * @param uriConverter_p a non-null URI converter for the resource set of the scope
   * @param label_p an optional label for the scope
   */
  public URIConvertingScopeDefinition(IModelScopeDefinition wrapped_p,
      URIConverter uriConverter_p, String label_p) {
    _wrapped = wrapped_p;
    _uriConverter = uriConverter_p;
    _label = label_p;
  }
  
  /**
   * @see org.eclipse.emf.diffmerge.ui.specification.IModelScopeDefinition#createScope(java.lang.Object)
   */
  public IEditableModelScope createScope(Object context_p) {
    IEditableModelScope result = _wrapped.createScope(null); // Ignore context for a fresh resource set
    if (result instanceof AbstractModelScope)
      ((AbstractModelScope)result).setOriginator(_label);
    if (result instanceof IPersistentModelScope) {
      IPersistentModelScope casted = (IPersistentModelScope)result;
      if (_loadingStream != null && casted instanceof IPersistentModelScope.Editable)
        ((IPersistentModelScope.Editable)casted).setStream(_loadingStream);
      Resource holdingResource = casted.getHoldingResource();
      if (holdingResource != null) {
        ResourceSet rs = holdingResource.getResourceSet();
        if (rs != null)
          rs.setURIConverter(_uriConverter);
      }
    }
    return result;
  }
  
  /**
   * @see org.eclipse.emf.diffmerge.ui.specification.IModelScopeDefinition#getEntrypoint()
   */
  public Object getEntrypoint() {
    return _wrapped.getEntrypoint();
  }
  
  /**
   * @see org.eclipse.emf.diffmerge.ui.specification.IModelScopeDefinition#getLabel()
   */
  public String getLabel() {
    return _wrapped.getLabel();
  }
  
  /**
   * @see org.eclipse.emf.diffmerge.ui.specification.IModelScopeDefinition#getShortLabel()
   */
  public String getShortLabel() {
    return _wrapped.getShortLabel();
  }
  
  /**
   * @see org.eclipse.emf.diffmerge.ui.specification.IModelScopeDefinition#isEditable()
   */
  public boolean isEditable() {
    return false; // Cannot modify element by default
  }
  
  /**
   * @see org.eclipse.emf.diffmerge.ui.specification.IModelScopeDefinition#isEditableSettable()
   */
  public boolean isEditableSettable() {
    return false; // Cannot modify element by default
  }
  
  /**
   * @see org.eclipse.emf.diffmerge.ui.specification.IModelScopeDefinition#setEditable(boolean)
   */
  public void setEditable(boolean editable_p) {
    // Cannot modify element by default
  }
  
  /**
   * Set an input stream for scope loading
   * @param stream_p a potentially null stream
   */
  public void setStream(InputStream stream_p) {
    _loadingStream = stream_p;
  }
  
}