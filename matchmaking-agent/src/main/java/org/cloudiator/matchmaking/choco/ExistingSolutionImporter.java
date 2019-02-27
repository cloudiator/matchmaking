package org.cloudiator.matchmaking.choco;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.cloudiator.matchmaking.domain.NodeCandidate;
import org.cloudiator.matchmaking.domain.Solution;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

public class ExistingSolutionImporter {

  @Nullable
  private final Solution existingSolution;

  public ExistingSolutionImporter(@Nullable Solution existingSolution) {
    this.existingSolution = existingSolution;
  }

  public Map<Integer, Map<EAttribute, Object>> handle() {

    if (existingSolution == null) {
      return new HashMap<>();
    }

    Map<Integer, Map<EAttribute, Object>> map = new HashMap<>(
        existingSolution.getNodeCandidates().size() + 1);

    int node = 1;
    for (NodeCandidate nodeCandidate : existingSolution.getNodeCandidates()) {

      Map<EAttribute, Object> attributeMap = new HashMap<>();

      attributeMap.putAll(handleEObject(nodeCandidate.getCloud()));
      attributeMap.putAll(handleEObject(nodeCandidate.getHardware()));
      attributeMap.putAll(handleEObject(nodeCandidate.getImage()));
      attributeMap.putAll(handleEObject(nodeCandidate.getLocation()));

      map.put(node, attributeMap);
      node++;
    }

    return map;
  }

  private Map<EAttribute, Object> handleEObject(EObject eObject) {
    return handleEClass(eObject.eClass(), eObject);
  }

  private Map<EAttribute, Object> handleEClass(EClass eClass, EObject eObject) {

    Map<EAttribute, Object> map = new HashMap<>(eClass.getEAllAttributes().size());

    for (EAttribute eAttribute : eClass.getEAllAttributes()) {
      map.put(eAttribute, handleEAttribute(eAttribute, eObject));
    }

    return map;

  }

  private Object handleEAttribute(EAttribute eAttribute, EObject eObject) {
    //get the value
    return eObject.eGet(eAttribute);
  }

}
