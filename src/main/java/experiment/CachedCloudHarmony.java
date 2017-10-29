package experiment;

import cloudiator.CloudiatorFactory;
import cloudiator.CloudiatorModel;
import cloudiator.CloudiatorPackage;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import org.cloudiator.ocl.ModelGenerator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

public class CachedCloudHarmony implements ModelGenerator {

  private static final String FILE_ENDING = "cloudiator";
  private static final String FILE_DESC = "discovery";
  private static final String FILE_NAME = FILE_DESC + "." + FILE_ENDING;

  private static final CloudiatorFactory CLOUDIATOR_FACTORY = CloudiatorFactory.eINSTANCE;
  private final CloudHarmony cloudHarmony;

  public CachedCloudHarmony() {
    cloudHarmony = new CloudHarmony();
  }

  public static void main(String[] args) {
    new CachedCloudHarmony().generateModel("blub");
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

  @Override
  public CloudiatorModel generateModel(String userId) {
    try {
      return load();
    } catch (Exception e) {
      e.printStackTrace();
      CloudiatorModel cloudiatorModel = cloudHarmony.generateModel(userId);
      try {
        saveModel(cloudiatorModel);
      } catch (IOException io) {
        throw new IllegalStateException(io);
      }
      return cloudiatorModel;
    }
  }
}
