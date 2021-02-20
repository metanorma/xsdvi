package xsdvi;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.apache.xerces.xs.XSImplementation;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

import xsdvi.svg.AbstractSymbol;
import xsdvi.svg.SvgForXsd;
import xsdvi.utils.LoggerHelper;
import xsdvi.utils.TreeBuilder;
import xsdvi.utils.WriterHelper;
import xsdvi.utils.XsdErrorHandler;

/**
 * @author Václav Slavìtínský
 * 
 */
public final class XsdVi {
	private static final Logger logger = Logger.getLogger(LoggerHelper.LOGGER_NAME);
	
	private static List<String> inputs = new ArrayList<String>();
	private static String style = null;
	private static String styleUrl = null;
	private static String rootNodeName = null;
        private static boolean oneElementOnly = false;
       
        /**
	 * 
	 */
	public static final String ROOT_NODE_NAME = "rootNodeName";
        
        public static final String ONE_ELEMENT_ONLY = "oneElementOnly";
        
	/**
	 * 
	 */
	public static final String EMBODY_STYLE = "embodyStyle";
	
	/**
	 * 
	 */
	public static final String GENERATE_STYLE = "generateStyle";
	
	/**
	 * 
	 */
	public static final String USE_STYLE = "useStyle";
	
        static final Option optionRootNodeName = Option.builder(ROOT_NODE_NAME)
                    .desc(" schema root node name")
                    .hasArg()
                    .required(false)
                    .build();
        
        static final Option optionOneElementOnly = Option.builder(ONE_ELEMENT_ONLY)
                    .desc(" show only one element")
                    .required(false)
                    .build();
        
        static final Option optionEmbodyStyle = Option.builder(EMBODY_STYLE)
                    .desc(" css style will be embodied in each svg file, this is default")
                    .required(true)
                    .build();
        
        static final Option optionGenerateStyle = Option.builder(GENERATE_STYLE)
                    .desc(" new css file with specified name will be generated and used by svgs")
                    .hasArg()
                    .required(true)
                    .build();
       
        static final Option optionUseStyle = Option.builder(USE_STYLE)
                    .desc(" external css file at specified url will be used by svgs")
                    .hasArg()
                    .required(true)
                    .build();
         
        static final Options options = new Options() {
            { 
                addOption(optionRootNodeName);
                addOption(optionOneElementOnly);
            }
        };

        static final Options optionsEmbodyStyle = new Options() {
            {
                addOption(optionRootNodeName);
                addOption(optionOneElementOnly);
                addOption(optionEmbodyStyle);
            }
        };
        
        static final Options optionsGenerateStyle = new Options() {
            {
                addOption(optionRootNodeName);
                addOption(optionOneElementOnly);
                addOption(optionGenerateStyle);
            }
        };
        
        static final Options optionsUseStyle = new Options() {
            {
                addOption(optionRootNodeName);
                addOption(optionOneElementOnly);
                addOption(optionUseStyle);
            }
        };
        
        static final String CMD = "java -jar xsdvi.jar <input1.xsd> [<input2.xsd> [<input3.xsd> ...]] [-" + ROOT_NODE_NAME + " <name>] -" + ONE_ELEMENT_ONLY;
        static final String CMD_EmbodyStyle = "java -jar xsdvi.jar <input1.xsd> [<input2.xsd> [<input3.xsd> ...]] -" + EMBODY_STYLE + " [-" + ROOT_NODE_NAME + " <name>] -" + ONE_ELEMENT_ONLY;
        static final String CMD_GenerateStyle = "java -jar xsdvi.jar <input1.xsd> [<input2.xsd> [<input3.xsd> ...]] -" + GENERATE_STYLE + " [-" + ROOT_NODE_NAME + " <name>] -" + ONE_ELEMENT_ONLY;
        static final String CMD_UseStyle = "java -jar xsdvi.jar <input1.xsd> [<input2.xsd> [<input3.xsd> ...]] -" + USE_STYLE + " [-" + ROOT_NODE_NAME + " <name>] -" + ONE_ELEMENT_ONLY;
        
        static final String USAGE = getUsage();
        
        static final int ERROR_EXIT_CODE = -1;
        
	/**
	 * 
	 */
	/*public static final String USAGE =
		"\n" +
		"USAGE:\n" +
		"java -jar xsdvi.jar <input1.xsd> [<input2.xsd> [<input3.xsd> ...]] [style]\n" +
		"  STYLE:\n" +
		"    -" + EMBODY_STYLE + "                css style will be embodied in each svg file, this is default\n" +
		"    -" + GENERATE_STYLE + " <style.css>  new css file with specified name will be generated and used by svgs\n" +
		"    -" + USE_STYLE + "      <style.css>  external css file at specified url will be used by svgs\n";
	*/
	
	/**
	 * 
	 */
	private XsdVi() {
		// no instances
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LoggerHelper.setupLogger();
		
		parseArgs(args);
		
		XSLoader schemaLoader = getSchemaLoader();
		
		TreeBuilder builder = new TreeBuilder();
		XsdHandler xsdHandler = new XsdHandler(builder);
		WriterHelper writerHelper = new WriterHelper();
		SvgForXsd svg = new SvgForXsd(writerHelper);
		svg.setHideMenuButtons(oneElementOnly);
                
		if (style.equals(EMBODY_STYLE)) {
			logger.info("The style will be embodied");
			svg.setEmbodyStyle(true);
		}
		else {
			logger.info("Using external style " + styleUrl);
			svg.setEmbodyStyle(false);
			svg.setStyleUri(styleUrl);
		}
		if (style.equals(GENERATE_STYLE)) {
			logger.info("Generating style " + styleUrl + "...");
			svg.printExternStyle();
			logger.info("Done.");
		}
		
		for (String input : inputs) {
			String output = outputUrl(input);
			
			logger.info("Parsing " + input + "...");
			XSModel model = schemaLoader.loadURI(input);
			logger.info("Processing XML Schema model...");
                        xsdHandler.setRootNodeName(rootNodeName);
                        xsdHandler.setOneElementOnly(oneElementOnly);
			xsdHandler.processModel(model);
			logger.info("Drawing SVG " + output + "...");
			writerHelper.newWriter(output);
			svg.draw((AbstractSymbol) builder.getRoot());
			logger.info("Done.");
		}
		
		//new xsdvi.svg.SvgSymbols(writerHelper).drawSymbols();
		//logger.info("Symbols saved.");
	}

	/**
	 * @param input
	 * @return
	 */
	private static String outputUrl(String input) {
		String[] field = input.split("[/\\\\]");
		String in = field[field.length-1];
		if (in.toLowerCase().endsWith(".xsd")) {
			return in.substring(0, in.length()-4) + ".svg";
		}
		return in + ".svg";
	}

	/**
	 * @param args
	 */
	private static void parseArgs(String[] args) {
            
            CommandLineParser parser = new DefaultParser();
               
            boolean cmdFail = false;

            CommandLine cmd = null;
            
            try {
                cmd = parser.parse(options, args);
                style = EMBODY_STYLE;
                
            } catch (ParseException exp) {
                cmdFail = true;
            }

            if(cmdFail) {
                try {
                    cmd = parser.parse(optionsEmbodyStyle, args);
                    style = EMBODY_STYLE;
                    
                } catch (ParseException exp) {
                    cmdFail = true;
                }
            }

            if(cmdFail) {
                try {
                    cmd = parser.parse(optionsGenerateStyle, args);
                    style = GENERATE_STYLE;
                    styleUrl = cmd.getOptionValue(GENERATE_STYLE);
                    
                } catch (ParseException exp) {
                    cmdFail = true;
                }
            }

            if(cmdFail) {
                try {
                    cmd = parser.parse(optionsUseStyle, args);
                    style = USE_STYLE;
                    styleUrl = cmd.getOptionValue(USE_STYLE);
                    
                } catch (ParseException exp) {
                    cmdFail = true;
                }
            }

            if (cmdFail) {
                printUsage();
                System.exit(ERROR_EXIT_CODE);
            } else {
                rootNodeName = cmd.getOptionValue(ROOT_NODE_NAME);
                
                oneElementOnly = cmd.hasOption(ONE_ELEMENT_ONLY);
                
                inputs.addAll(cmd.getArgList());
                
                return;
            }
            
            
            
            
		/*if (args.length < 1 || args[0].equalsIgnoreCase(EMBODY_STYLE) || args[0].equalsIgnoreCase(GENERATE_STYLE) || args[0].equalsIgnoreCase(USE_STYLE)) {
			printUsage();
			System.exit(1);
		}
		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase(EMBODY_STYLE)) {
				if (args.length != i+1) {
					printUsage();
					System.exit(1);
				}
				style = EMBODY_STYLE;
				return;
			}
			else if (args[i].equalsIgnoreCase(GENERATE_STYLE)) {
				if (args.length != i+2) {
					printUsage();
					System.exit(1);
				}
				style = GENERATE_STYLE;
				styleUrl = args[i+1];
				return;
			}
			else if (args[i].equalsIgnoreCase(USE_STYLE)) {
				if (args.length != i+2) {
					printUsage();
					System.exit(1);
				}
				style = USE_STYLE;
				styleUrl = args[i+1];
				return;
			}
			else {
				inputs.add(args[i]);
			}
		}*/
		
	}

	/**
	 * 
	 */
	private static void printUsage() {
		logger.severe(USAGE);
	}

	/**
	 * @return
	 */
	private static XSLoader getSchemaLoader() {
		XSLoader schemaLoader = null;
		try {
			System.setProperty(DOMImplementationRegistry.PROPERTY, "org.apache.xerces.dom.DOMXSImplementationSourceImpl");
			DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
			XSImplementation impl = (XSImplementation) registry.getDOMImplementation("XS-Loader");
			schemaLoader = impl.createXSLoader(null);
			DOMConfiguration config = schemaLoader.getConfig();
			DOMErrorHandler errorHandler = new XsdErrorHandler();
			config.setParameter("error-handler", errorHandler);
			config.setParameter("validate", Boolean.TRUE);
		} catch (ClassCastException e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} catch (ClassNotFoundException e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} catch (InstantiationException e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} catch (IllegalAccessException e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		return schemaLoader;
	}
        
    private static String getUsage() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter pw = new PrintWriter(stringWriter);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(pw, 100, CMD, "", options, 0, 0, "");
        pw.write("\nOR\n\n");
        formatter.printHelp(pw, 100, CMD_EmbodyStyle, "", optionsEmbodyStyle, 0, 0, "");
        pw.write("\nOR\n\n");
        formatter.printHelp(pw, 100, CMD_GenerateStyle, "", optionsGenerateStyle, 0, 0, "");
        pw.write("\nOR\n\n");
        formatter.printHelp(pw, 100, CMD_UseStyle, "", optionsUseStyle, 0, 0, "");
        pw.flush();
        return stringWriter.toString();
    }

}