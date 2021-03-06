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
package org.eclipse.emf.diffmerge.ui;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.compare.CompareUI;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.emf.common.util.Logger;
import org.eclipse.emf.diffmerge.EMFDiffMergePlugin;
import org.eclipse.emf.diffmerge.api.Role;
import org.eclipse.emf.diffmerge.ui.diffuidata.DiffuidataPackage;
import org.eclipse.emf.diffmerge.ui.log.DiffMergeLogger;
import org.eclipse.emf.diffmerge.ui.setup.ComparisonSetupManager;
import org.eclipse.emf.diffmerge.ui.util.DifferenceKind;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The activator for this plug-in.
 * @author Olivier Constant
 */
public class EMFDiffMergeUIPlugin extends AbstractUIPlugin {
  
  /** The default file extension for UI diff models */
  public static final String UI_DIFF_DATA_FILE_EXTENSION = DiffuidataPackage.eNAME;
  
  /** Identifiers for UI images */
  @SuppressWarnings("javadoc")
  public static enum ImageID {
    CHECKED, CHECKED_DISABLED, CHECKIN_ACTION, CHECKOUT_ACTION, COLLAPSEALL, CONFLICT_STAT, DELETE,
    DOWN, EMPTY, EXPANDALL, FILTER, INC_STAT, INC_ADD_STAT, INC_REM_STAT, LEFT, LOCK, LOCK_CLOSED,
    LOCK_OPEN, MODIFIED_STAT, NEXT_CHANGE_NAV, NEXT_DIFF_NAV, OUT_STAT, OUT_ADD_STAT, OUT_REM_STAT,
    PLUS, PREV_CHANGE_NAV, PREV_DIFF_NAV, REDO, RIGHT, SHOW, SORT, SWAP, SYNCED, TREE, UNCHECKED,
    UNCHECKED_DISABLED, UNDO, UP, UPDATE, VIEW_MENU, WARNING }
  
  /** Identifiers for colors according to the side to which a difference presence is relative */
  @SuppressWarnings("javadoc")
  public static enum DifferenceColorKind {
    LEFT, RIGHT, BOTH, NONE,
    CONFLICT, DEFAULT
  }
  
  /** The local path to icons */
  protected static final String ICON_PATH = "icons/full/"; //$NON-NLS-1$
  
  
  
  /** A label for dialogs */
  public static final String LABEL = Messages.EMFDiffMergeUIPlugin_Label;
  
	/** The shared instance */
	private static EMFDiffMergeUIPlugin __plugin;
	
	/** The manager for comparison contexts */
	private final ComparisonSetupManager _comparisonSetupManager;
	
	/** The logger for diff/merge events */
	private final DiffMergeLogger _diffMergeLogger;
	
	/** A symbolic representation of the virtual ownership feature */
  private final EReference _ownershipFeature;
  
  /** The "very dark gray" non-system color (initially null) */
  private Color _veryDarkGray;
  
  /** A label provider based on the EMF Edit registry (initially null) */
  private AdapterFactoryLabelProvider _composedAdapterFactoryLabelProvider;
  
  /** A map from a subset of image IDs to the corresponding shared image identifiers */
  private final Map<ImageID, String> _sharedImageMap;
  
  
  /**
	 * Constructor
	 */
	public EMFDiffMergeUIPlugin() {
	  _diffMergeLogger = new DiffMergeLogger();
	  _comparisonSetupManager = new ComparisonSetupManager();
	  _ownershipFeature = createOwnershipFeature();
	  _veryDarkGray = null;
	  _composedAdapterFactoryLabelProvider = null;
	  _sharedImageMap = createImageMap();
	}
	
	/**
	 * Return a map from a subset of image IDs to the corresponding shared image identifiers
	 * @return a non-null map
	 */
	protected Map<ImageID, String> createImageMap() {
	  Map<ImageID, String> result = new HashMap<ImageID, String>();
	  result.put(ImageID.DELETE, ISharedImages.IMG_TOOL_DELETE);
    result.put(ImageID.LEFT, ISharedImages.IMG_TOOL_BACK);
    result.put(ImageID.REDO, ISharedImages.IMG_TOOL_REDO);
    result.put(ImageID.RIGHT, ISharedImages.IMG_TOOL_FORWARD);
    result.put(ImageID.SHOW, ISharedImages.IMG_OBJS_INFO_TSK);
    result.put(ImageID.UNDO, ISharedImages.IMG_TOOL_UNDO);
    result.put(ImageID.WARNING, ISharedImages.IMG_OBJS_WARN_TSK);
    return result;
  }
	
  /**
	 * Return a reference representing the virtual "ownership" feature
	 * @return a non-null reference
	 */
	protected EReference createOwnershipFeature() {
	  EReference result = EcoreFactory.eINSTANCE.createEReference();
	  result.setName("container"); //$NON-NLS-1$
	  result.setEType(EcorePackage.eINSTANCE.getEObject());
	  result.setLowerBound(0);
	  result.setUpperBound(1);
	  return result;
	}
	
	/**
	 * Return a label provider that is based on the EMF Edit registry
	 * @return a non-null object
	 */
	public AdapterFactoryLabelProvider getAdapterFactoryLabelProvider() {
	  if (_composedAdapterFactoryLabelProvider == null)
	    _composedAdapterFactoryLabelProvider = new AdapterFactoryLabelProvider(
	        EMFDiffMergePlugin.getDefault().getAdapterFactory());
	  return _composedAdapterFactoryLabelProvider;
	}
	
  /**
   * Return the shared instance of this activator
   * @return a non-null object
   */
  public static EMFDiffMergeUIPlugin getDefault() {
    return __plugin;
  }
  
  /**
   * Return the default role for the left-hand side in a comparison
   * @return a non-null role which is TARGET or REFERENCE
   */
  public Role getDefaultLeftRole() {
    return Role.TARGET;
  }
  
  /**
   * Return the color kind that corresponds to the given difference kind
   * @param originKind_p a potentially null difference kind
   * @return a non-null color kind
   */
  public DifferenceColorKind getDifferenceColorKind(DifferenceKind originKind_p) {
    DifferenceColorKind result;
    if (originKind_p == null) {
      result = DifferenceColorKind.DEFAULT;
    } else {
      switch (originKind_p) {
        case NONE:
          result = DifferenceColorKind.NONE; break;
        case CONFLICT:
          result = DifferenceColorKind.CONFLICT; break;
        case MODIFIED: case FROM_LEFT: case FROM_RIGHT: case FROM_BOTH:
          result = DifferenceColorKind.BOTH; break;
        case FROM_LEFT_ADD: case FROM_RIGHT_DEL:
          result = DifferenceColorKind.LEFT; break;
        case FROM_RIGHT_ADD: case FROM_LEFT_DEL:
          result = DifferenceColorKind.RIGHT; break;
        default:
          result = DifferenceColorKind.DEFAULT; break;
      }
    }
    return result;
  }
  
  /**
   * Return the image ID that corresponds to the given difference origin kind
   * @param originKind_p a non-null difference origin kind
   * @return a potentially null image ID
   */
  public ImageID getDifferenceOverlay(DifferenceKind originKind_p) {
    ImageID result;
    switch (originKind_p) {
      case FROM_LEFT:
        result = ImageID.OUT_STAT; break;
      case FROM_LEFT_ADD:
        result = ImageID.OUT_ADD_STAT; break;
      case FROM_LEFT_DEL:
        result = ImageID.OUT_REM_STAT; break;
      case FROM_RIGHT:
        result = ImageID.INC_STAT; break;
      case FROM_RIGHT_ADD:
        result = ImageID.INC_ADD_STAT; break;
      case FROM_RIGHT_DEL:
        result = ImageID.INC_REM_STAT; break;
      case MODIFIED:
      case FROM_BOTH:
        result = ImageID.MODIFIED_STAT; break;
      case CONFLICT:
        result = ImageID.CONFLICT_STAT; break;
      default:
        result = null; break;
    }
    return result;
  }
  
  /**
   * Return the prefix that corresponds to the given difference kind
   * @param originKind_p a non-null difference origin kind
   * @return a non-null string
   */
  public String getDifferencePrefix(DifferenceKind originKind_p) {
    String result;
    switch (originKind_p) {
      case FROM_LEFT:
        result = "|> "; break; //$NON-NLS-1$
      case FROM_LEFT_ADD:
        result = "+> "; break; //$NON-NLS-1$
      case FROM_LEFT_DEL:
        result = "-> "; break; //$NON-NLS-1$
      case FROM_RIGHT:
        result = "<| "; break; //$NON-NLS-1$
      case FROM_RIGHT_ADD:
        result = "<+ "; break; //$NON-NLS-1$
      case FROM_RIGHT_DEL:
        result = "<- "; break; //$NON-NLS-1$
      case CONFLICT:
        result = "! "; break; //$NON-NLS-1$
      case MODIFIED:
      case FROM_BOTH:
        result = "| "; break; //$NON-NLS-1$
      default:
        result = ""; break; //$NON-NLS-1$
    }
    return result;
  }
  
  /**
   * Return the logger for diff/merge events
   * @return a non-null logger
   */
  public Logger getDiffMergeLogger() {
    return _diffMergeLogger;
  }
  
  /**
   * Return the image of the given ID
   * @param id_p a non-null image ID
   * @return a (normally) non-null image
   */
  public Image getImage(ImageID id_p) {
    Image result;
    String sharedID = _sharedImageMap.get(id_p);
    if (sharedID != null) {
      result = PlatformUI.getWorkbench().getSharedImages().getImage(sharedID);
    } else {
      result = getImageRegistry().get(id_p.name());
    }
    return result;
  }
  
  /**
   * Return the image descriptor of the given ID
   * @param id_p a non-null image ID
   * @return a (normally) non-null image
   */
  public ImageDescriptor getImageDescriptor(ImageID id_p) {
    ImageDescriptor result;
    String sharedID = _sharedImageMap.get(id_p);
    if (sharedID != null) {
      result = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(sharedID);
    } else {
      result = getImageRegistry().getDescriptor(id_p.name());
    }
    return result;
  }
  
  /**
   * Return a symbolic reference that represents the virtual "ownership" feature
   * @return a non-null EReference
   */
  public EReference getOwnershipFeature() {
    return _ownershipFeature;
  }
  
  /**
   * Return the plug-in ID according to MANIFEST.MF
   * @return a non-null String
   */
  public String getPluginId() {
    return getBundle().getSymbolicName();
  }
  
  /**
   * Return the comparison setup manager
   * @return a non-null object
   */
  public ComparisonSetupManager getSetupManager() {
    return _comparisonSetupManager;
  }
  
  /**
   * Return the "very dark gray" non-system color
   * @return a non-null color
   */
  public Color getVeryDarkGray() {
    if (_veryDarkGray == null)
      _veryDarkGray = new Color(Display.getDefault(), 75, 75, 75);
    return _veryDarkGray;
  }
  
  /**
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
   */
  @Override
  protected void initializeImageRegistry(ImageRegistry reg_p) {
    super.initializeImageRegistry(reg_p);
    reg_p.put(ImageID.UP.name(), CompareUI.DESC_CTOOL_PREV);
    reg_p.put(ImageID.DOWN.name(), CompareUI.DESC_CTOOL_NEXT);
    Set<ImageID> toRegister = new HashSet<ImageID>(
        Arrays.asList(ImageID.values()));
    toRegister.removeAll(_sharedImageMap.keySet());
    toRegister.removeAll(Arrays.asList(ImageID.DOWN, ImageID.UP));
    for (ImageID imageId : toRegister) {
      registerLocalIcon(imageId, reg_p);
    }
  }
  
  /**
   * Register and return the image descriptor obtained from the given ID of a local icon
   * @param imageID_p a non-null image ID
   * @param reg_p the non-null image registry in which to register
   * @return a potentially null image descriptor
   */
  protected ImageDescriptor registerLocalIcon(ImageID imageID_p, ImageRegistry reg_p) {
    ImageDescriptor result = null;
    String path = ICON_PATH + imageID_p.name().toLowerCase() + ".gif"; //$NON-NLS-1$
    try {
      result = ImageDescriptor.createFromURL(FileLocator.toFileURL(
          getBundle().getEntry(path)));
    } catch (IOException e) {
      // Nothing needed
    }
    if (result != null)
      reg_p.put(imageID_p.name(), result);
    return result;
  }
  
	/**
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context_p) throws Exception {
		super.start(context_p);
		__plugin = this;
	}
	
	/**
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context_p) throws Exception {
	  _diffMergeLogger.close();
	  if (_veryDarkGray != null)
	    _veryDarkGray.dispose();
	  if (_composedAdapterFactoryLabelProvider != null)
	    _composedAdapterFactoryLabelProvider.dispose();
		__plugin = null;
		super.stop(context_p);
	}
	
}
