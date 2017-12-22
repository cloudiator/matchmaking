package org.cloudiator.choco;

import static com.google.common.base.Preconditions.checkState;

import cloudiator.CloudiatorModel;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.Variable;
import org.cloudiator.ocl.OclCsp;
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;

public class ModelGenerationContext {

  private final Map<String, ObjectMapper> objectMappers;
  private final CloudiatorModel cloudiatorModel;
  private final Model model;
  private final int numberOfNodes;
  private final VariableStore variableStore;
  private final ObjectIdentifierGenerator oidGenerator;
  private final OclCsp oclCsp;

  public ModelGenerationContext(CloudiatorModel cloudiatorModel, Model model, int numberOfNodes,
      OclCsp oclCsp) {
    this.cloudiatorModel = cloudiatorModel;
    this.model = model;
    this.numberOfNodes = numberOfNodes;
    objectMappers = new HashMap<>();
    this.variableStore = new VariableStore();
    this.oidGenerator = ObjectIdentifierGenerator.create();
    this.oclCsp = oclCsp;
  }

  public ObjectIdentifierGenerator getOidGenerator() {
    return oidGenerator;
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

  public Model getModel() {
    return model;
  }

  public CloudiatorModel getCloudiatorModel() {
    return cloudiatorModel;
  }

  public VariableStore getVariableStore() {
    return variableStore;
  }

  public int nodeSize() {
    return numberOfNodes;
  }

  public static class VariableStore {

    private Table<Integer, EAttribute, Variable> variableStore = HashBasedTable.create();
    private Table<Integer, EClass, Variable> idStore = HashBasedTable.create();

    public void storeVariable(int node, EAttribute eAttribute, Variable variable) {
      if (eAttribute.isID()) {
        storeIdVariable(node, eAttribute.getEContainingClass(), variable);
      }
      variableStore.put(node, eAttribute, variable);
    }

    public void storeIdVariable(int node, EClass eClass, Variable variable) {
      idStore.put(node, eClass, variable);
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

  }

}
