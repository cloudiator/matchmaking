package org.cloudiator.matchmaking.cmpl;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.chocosolver.solver.variables.Variable;
import org.cloudiator.matchmaking.choco.ExistingSolutionImporter;
import org.cloudiator.matchmaking.choco.ObjectMapper;
import org.cloudiator.matchmaking.choco.ObjectMappers;
import org.cloudiator.matchmaking.domain.Solution;
import org.cloudiator.matchmaking.ocl.NodeCandidates;
import org.cloudiator.matchmaking.ocl.OclCsp;
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;

public class CMPLGenerationContext {

  private final Map<String, ObjectMapper> objectMappers;
  private final NodeCandidates nodeCandidates;
  private final CMPLModel cmplModel;
  private final int numberOfNodes;
  private final OclCsp oclCsp;
  private Map<Integer, Map<EAttribute, Object>> existingValues;


  public CMPLGenerationContext(NodeCandidates nodeCandidates,
      CMPLModel cmplModel, int numberOfNodes,
      OclCsp oclCsp, @Nullable Solution existingSolution) {
    this.nodeCandidates = nodeCandidates;
    this.cmplModel = cmplModel;
    this.numberOfNodes = numberOfNodes;
    objectMappers = new HashMap<>();
    this.oclCsp = oclCsp;
    existingValues = new ExistingSolutionImporter(existingSolution).handle();
  }

  public boolean hasExistingValue(int node, EAttribute eAttribute) {
    if (!existingValues.containsKey(node)) {
      return false;
    }
    return existingValues.get(node).containsKey(eAttribute);
  }

  public Object getExistingValue(int node, EAttribute eAttribute) {
    checkState(hasExistingValue(node, eAttribute),
        String.format("Has no initial value for node %s and attribute %s.", node, eAttribute));
    return existingValues.get(node).get(eAttribute);
  }

  @SuppressWarnings("unchecked")
  private ObjectMapper<Object> getObjectMapper(String attribute, Class clazz) {
    if (!objectMappers.containsKey(attribute)) {
      objectMappers.put(attribute, ObjectMappers.getObjectMapperForType(clazz));
    }
    return objectMappers.get(attribute);
  }

  @SuppressWarnings("unchecked")
  private ObjectMapper<Object> getObjectMapper(String attribute) {
    final ObjectMapper objectMapper = objectMappers.get(attribute);
    checkState(objectMapper != null, "Could not find object mapper for attribute " + attribute);
    return objectMapper;
  }

  public OclCsp getOclCsp() {
    return oclCsp;
  }

  public int mapValue(Object o, EAttribute eAttribute) {

    if (eAttribute.getEAttributeType().getInstanceClass() == Integer.class) {
      if (o == null) {
        return 0;
      }
      return (Integer) o;
    }

    if (eAttribute.getEType() instanceof EEnum) {
      Enumerator enumerator = (Enumerator) o;
      return ((Enumerator) o).getValue();
    }
    return this
        .mapValue(o, eAttribute.getEContainingClass().getName() + "." + eAttribute.getName(),
            eAttribute.getEAttributeType().getInstanceClass());
  }

  public int mapValue(Object o, String attribute, Class type) {
    //noinspection unchecked
    return getObjectMapper(attribute, type).applyAsInt(o);
  }

  public <F> F mapBack(int mappedValue, EAttribute eAttribute, Class<F> clazz) {
    return mapBack(mappedValue,
        eAttribute.getEContainingClass().getName() + "." + eAttribute.getName(), clazz);
  }

  public <F> F mapBack(int mappedValue, String attribute, Class<F> clazz) {
    //noinspection unchecked
    return (F) getObjectMapper(attribute).applyBack(mappedValue);
  }

  public int nodeSize() {
    return numberOfNodes;
  }

  public NodeCandidates getNodeCandidates() {
    return nodeCandidates;
  }

  public CMPLModel getCmplModel() {
    return cmplModel;
  }

  public static class VariableStore {

    private Table<Integer, EAttribute, Variable> variableStore = HashBasedTable.create();
    private Table<Integer, EClass, Variable> idStore = HashBasedTable.create();
    private Table<Integer, String, Variable> custom = HashBasedTable.create();

    public void storeVariable(int node, EAttribute eAttribute, Variable variable) {
      if (eAttribute.isID()) {
        storeIdVariable(node, eAttribute.getEContainingClass(), variable);
      }
      variableStore.put(node, eAttribute, variable);
    }

    public void storeIdVariable(int node, EClass eClass, Variable variable) {
      idStore.put(node, eClass, variable);
    }

    public void storeCustomVariable(int node, String s, Variable variable) {
      custom.put(node, s, variable);
    }

    public Collection<Variable> getVariables(EAttribute eAttribute) {
      return variableStore.column(eAttribute).values();
    }

    public Map<EAttribute, Variable> getVariables(int node) {
      return variableStore.row(node);
    }

    public Map<EClass, Variable> getIdVariables(int node) {
      return idStore.row(node);
    }

    public Variable getCustomVariable(int node, String s) {
      return custom.row(node).get(s);
    }

    public Collection<Variable> getCustomVariables(String s) {
      return custom.column(s).values();
    }

  }

}
