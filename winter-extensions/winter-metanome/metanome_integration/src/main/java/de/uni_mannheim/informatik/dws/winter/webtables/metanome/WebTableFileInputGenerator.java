package de.uni_mannheim.informatik.dws.winter.webtables.metanome;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.configuration.ConfigurationSettingFileInput;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.backend.input.file.DefaultFileInputGenerator;

/**
 * uses column index instead of name as header & identifier (important if headers are empty!)
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class WebTableFileInputGenerator extends DefaultFileInputGenerator {

  protected WebTableFileInputGenerator() {
  }

  public WebTableFileInputGenerator(File inputFile) throws FileNotFoundException {
	  super(inputFile);
  }

  public WebTableFileInputGenerator(ConfigurationSettingFileInput setting)
    throws AlgorithmConfigurationException {
	  super(setting);
  }

  @Override
  public RelationalInput generateNewCopy() throws InputGenerationException {
    try {
      return new WebTableFileIterator(getInputFile().getName(), new FileReader(getInputFile()), getSetting());
    } catch (FileNotFoundException e) {
      throw new InputGenerationException("File not found!", e);
    } catch (InputIterationException e) {
      throw new InputGenerationException("Could not iterate over the first line of the file input", e);
    }
  }

}
