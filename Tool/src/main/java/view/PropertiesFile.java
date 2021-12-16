package view;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import control.PipelineHandler;
import observerModel.ObserverModel;
/**Method to use the program when giving all the information in a properties file
 * 
 * @author Marc Carwehl
 *
 */
public class PropertiesFile {

	
	
	/**
	 * 
	 * @param args - [0] path to properties file
	 */
	public static void main(String[] args) {
		if(args.length == 0) {
			System.out.println("Please specify a path to a properties file");
			return;
		}
		try(InputStream input = new FileInputStream(args[0])){
			Properties prop;
			String observerPath;
			String pattern;
			String scope;
			List<String> parameters;
			prop = new Properties();
			prop.load(input);
			
			//get the system file
			String systemfile = prop.getProperty("systemfile"); 
			if(systemfile == null) 
				error("systemfile");
			else
				log("systemfile", systemfile);
			
			PipelineHandler control = new PipelineHandler(systemfile);
			//get the pattern
			pattern = prop.getProperty("pattern"); 
			if(pattern == null)
				error("pattern");
			else
				log("pattern", pattern);

			control.setPattern(pattern);
			//and the scope
			scope = prop.getProperty("scope"); 
			if(scope == null)
				error("scope");
			else
				log("scope", scope);

			//now look at our model and identify which parameters should be given
			ObserverModel observerModel = new ObserverModel();
			parameters = observerModel.getParameters(pattern, scope);
			HashMap<String, String> setParameters = new HashMap<String, String>();
			for(String element : parameters) {
				setParameters.put(element, prop.getProperty(element));
				if(prop.getProperty(element) == null)
					error(element);
				else
					log("property", element);

			}
			control.setScope(scope, setParameters);
			if(prop.get("timed") != null) {
				String lower = prop.getProperty("t1");
				String upper = prop.getProperty("t2");
				if(lower == null || upper == null) 
					error("timebounds");

				try {
					control.setTimed(true, Integer.parseInt(lower), Integer.parseInt(upper));
				} catch(NumberFormatException e) {
					error("timebounds");
				}
			}
			//get the observer's path, if none is given, then use the default one
			observerPath = prop.getProperty("observer", "observer_templates/");
			if(observerPath == null)
				error("observerPath");
			else
				log("observerPath", observerPath);

			String outputName = prop.getProperty("output");
			if(outputName == null)
				error("output");
			control.setTemplateLocation(observerPath);
			try {
				control.callPipeline(outputName);
			} catch (Exception e) {
				System.out.println("Something went terribly wrong!");
				System.out.println(e);
				System.exit(-1);
			}

			System.out.println("Work done!");
			System.out.println("Saved at: "+outputName);

		} catch (FileNotFoundException e) {
			System.out.println("The file you specified was not found.");
			System.out.println("File:" + args[0]);
		} catch (IOException e) {
			System.out.println("The file you specified could not be read.");
			System.out.println("File:" + args[0]);
		}

		

	}
	
	private static void error(String property) {
		System.out.println("Something went wrong with your specified properties file.");
		System.out.println("The property "+property+" was not found or not specified!");
		System.exit(-1);
	}
	private static void log(String key, String value) {
		System.out.println(key + ": " + value);
	}

}
