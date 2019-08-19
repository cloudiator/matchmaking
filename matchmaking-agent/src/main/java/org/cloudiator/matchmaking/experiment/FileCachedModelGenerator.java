package org.cloudiator.matchmaking.experiment;

import cloudiator.CloudiatorFactory;
import cloudiator.CloudiatorModel;
import cloudiator.CloudiatorPackage;
import com.google.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import javax.inject.Named;
import org.cloudiator.matchmaking.ocl.MetaSolver;
import org.cloudiator.matchmaking.ocl.ModelGenerationException;
import org.cloudiator.matchmaking.ocl.ModelGenerator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileCachedModelGenerator implements ModelGenerator {

  private static final String FILE_ENDING = "cloudiator";
  private static final String FILE_DESC = "discovery";
  private static final String FILE_NAME = FILE_DESC + "." + FILE_ENDING;

  private static final CloudiatorFactory CLOUDIATOR_FACTORY = CloudiatorFactory.eINSTANCE;
  private static final Logger LOGGER = LoggerFactory.getLogger(MetaSolver.class);
  private final ModelGenerator delegate;

  @Inject
  public FileCachedModelGenerator(@Named("Base") ModelGenerator delegate) {
    this.delegate = delegate;
  }

  private static void saveModel(CloudiatorModel model) throws IOException {
    Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
    Map<String, Object> m = reg.getExtensionToFactoryMap();
    m.put(FILE_ENDING, new XMIResourceFactoryImpl());

    ResourceSet resSet = new ResourceSetImpl();
    Resource resource = resSet.createResource(URI
        .createURI(FILE_NAME));

    resource.getContents().add(model);

    resource.save(Collections.EMPTY_MAP);
  }

  public CloudiatorModel load() {
    // Initialize the model
    CloudiatorPackage.eINSTANCE.eClass();

    // Register the XMI resource factory for the .website extension

    Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
    Map<String, Object> m = reg.getExtensionToFactoryMap();
    m.put(FILE_ENDING, new XMIResourceFactoryImpl());

    // Obtain a new resource set
    ResourceSet resSet = new ResourceSetImpl();

    // Get the resource
    Resource resource = resSet.getResource(URI
        .createURI(FILE_NAME), true);
    // Get the first model element and cast it to the right type, in my
    // example everything is hierarchical included in this first node
    return (CloudiatorModel) resource.getContents().get(0);
  }

  @Override
  public CloudiatorModel generateModel(String userId) throws ModelGenerationException {
    try {
      final CloudiatorModel load = load();
      //make all locations assignable
      load.getClouds().stream().flatMap(c -> c.getLocations().stream())
          .forEach(l -> l.setAssignable(true));
      return load;
    } catch (Exception e) {
      LOGGER.warn("Error while loading cached file. Regenerating.", e);
      CloudiatorModel cloudiatorModel = delegate.generateModel(userId);
      try {
        saveModel(cloudiatorModel);
      } catch (IOException io) {
        throw new IllegalStateException(io);
      }
      return cloudiatorModel;
    }
  }
}
