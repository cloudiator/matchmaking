package org.cloudiator.matchmaking;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

public class EMFUtil {

  private final EObject model;

  private EMFUtil(EObject model) {
    this.model = model;
  }

  public static EMFUtil of(EObject model) {
    return new EMFUtil(model);
  }

  public Collection<EObject> getAllObjectsOfClass(EClass eClass) {
    List<EObject> objectsOfClass = new LinkedList<>();
    for (TreeIterator<EObject> i = model.eAllContents();
        i.hasNext(); ) {
      final EObject eObject = i.next();
      if (eClass.isInstance(eObject)) {
        objectsOfClass.add(eObject);
      }
    }
    return ImmutableList.copyOf(objectsOfClass);
  }

  public Collection<Object> getValuesOfAttribute(EAttribute eAttribute) {
    return ImmutableList.copyOf(
        getAllObjectsOfClass(eAttribute.getEContainingClass()).stream().map(
            eObject -> eObject.eGet(eAttribute)).filter(Objects::nonNull)
            .collect(Collectors.toList()));
  }
}
