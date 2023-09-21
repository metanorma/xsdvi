package xsdvi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;

import org.apache.xerces.xs.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import xsdvi.svg.*;
import xsdvi.utils.LoggerHelper;
import xsdvi.utils.TreeBuilder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * @author Václav Slavìtínský
 *
 */
public class XsdHandler {
	private static final Logger logger = Logger.getLogger(LoggerHelper.LOGGER_NAME);
	private TreeBuilder builder;
	private Stack<XSElementDeclaration> stack;
	
        private String rootNodeName;
        private boolean oneNodeOnly = false;
        
        private String schemaNamespace;
        
	/**
	 * @param xsdSymbols
	 */
	public XsdHandler(TreeBuilder xsdSymbols) {
		this.builder = xsdSymbols;
		stack = new Stack<XSElementDeclaration>();
	}
        
        public void setRootNodeName(String rootNodeName) {
            this.rootNodeName = rootNodeName;
        }
        
        public void setOneNodeOnly(boolean oneNodeOnly) {
            this.oneNodeOnly = oneNodeOnly;
        }
        
	/**
	 * @param model
	 */
	public void processModel(XSModel model) {
    	if (model == null) {
    		return;
    	}
    	AbstractSymbol symbol = new SymbolSchema();
        if (rootNodeName == null) {
            builder.setRoot(symbol);
        }
        processElementDeclarations(model.getComponents(XSConstants.ELEMENT_DECLARATION));
        
        if (rootNodeName == null) {
            builder.levelUp();
        }
	}
        
        public List<String> getElementsNames(XSModel model) {
            ArrayList<String> names = new ArrayList<>();
            if (model == null) {
                return names;
            }
            XSNamedMap map = model.getComponents(XSConstants.ELEMENT_DECLARATION);
            for(int i=0; i<map.getLength(); i++) {
                names.add(map.item(i).getName());
            }
            return names;
        }
        
        public void setSchemaNamespace(XSModel model, String elementName) {
            XSNamedMap map = model.getComponents(XSConstants.ELEMENT_DECLARATION);
            for(int i=0; i<map.getLength(); i++) {
                if (map.item(i).getName().equals(elementName)) {
                    schemaNamespace = map.item(i).getNamespace();
                    break;
                }
            }
        }
        
	/**
	 * @param map
	 */
	private void processElementDeclarations(XSNamedMap map) {
            for(int i=0; i<map.getLength(); i++) {
                String name = map.item(i).getName();
                boolean isRoot = name.equals(rootNodeName);
                if (isRoot || rootNodeName == null) {
                    processElementDeclaration((XSElementDeclaration) map.item(i), null, isRoot);
                }
            }
	}

	/**
	 * @param particle
	 */
	private void processParticle(XSParticle particle) {
    	processTerm(particle.getTerm(), getCardinalityString(particle));
	}

	/**
	 * @param term
	 * @param cardinality
	 */
	private void processTerm(XSTerm term, String cardinality) {
		int type = term.getType();
		if (type == XSConstants.MODEL_GROUP) {
			processModelGroup((XSModelGroup) term, cardinality);
		}
		else if (type == XSConstants.ELEMENT_DECLARATION) {
			processElementDeclaration((XSElementDeclaration) term, cardinality, false);
		}
		else if (type == XSConstants.WILDCARD) {
			processElementWildcard((XSWildcard) term, cardinality);
		}
	}

	/**
	 * @param wildcard
	 * @param cardinality
	 */
	private void processElementWildcard(XSWildcard wildcard, String cardinality) {
		SymbolAny symbol = new SymbolAny();
		String ns = getNamespaceString(wildcard);
		if (ns != null && !ns.equals(schemaNamespace)) {
			symbol.setNamespace(ns);
		}
		symbol.setDescription(getDocumentationString(wildcard));
		symbol.setProcessContents(getProcessContents(wildcard));
		symbol.setCardinality(cardinality);
    	builder.appendChild(symbol);
		builder.levelUp();
	}

	/**
	 * @param wildcard
	 */
	private void processAttributeWildcard(XSWildcard wildcard) {
		SymbolAnyAttribute symbol = new SymbolAnyAttribute();
		String ns = getNamespaceString(wildcard);
		if (ns != null && !ns.equals(schemaNamespace)) {
			symbol.setNamespace(ns);
		}
		symbol.setDescription(getDocumentationString(wildcard));
		symbol.setProcessContents(getProcessContents(wildcard));
    	builder.appendChild(symbol);
		builder.levelUp();
	}
	
	/**
	 * @param modelGroup
	 * @param cardinality
	 */
	private void processModelGroup(XSModelGroup modelGroup, String cardinality) {
		AbstractSymbol symbol = null;
		int compositor = modelGroup.getCompositor();
		if (compositor == XSModelGroup.COMPOSITOR_ALL) {
			symbol = new SymbolAll(cardinality);
			symbol.setDescription(getDocumentationString(modelGroup));
		}
		else if (compositor == XSModelGroup.COMPOSITOR_CHOICE) {
			symbol = new SymbolChoice(cardinality);
			symbol.setDescription(getDocumentationString(modelGroup));
		}
		else if (compositor == XSModelGroup.COMPOSITOR_SEQUENCE) {
			symbol = new SymbolSequence(cardinality);
			symbol.setDescription(getDocumentationString(modelGroup));
		}
    	builder.appendChild(symbol);
		processParticles(modelGroup.getParticles());
		builder.levelUp();
	}

	/**
	 * @param particles
	 */
	private void processParticles(XSObjectList particles) {
		for (int i=0; i<particles.getLength(); i++) {
			processParticle((XSParticle) particles.item(i));
		}
	}

	/**
	 * @param attributeUse
	 */
	private void processAttributeUse(XSAttributeUse attributeUse) {
		XSAttributeDeclaration attributeDeclaration = attributeUse.getAttrDeclaration();
		SymbolAttribute symbol = new SymbolAttribute();
		symbol.setName(attributeDeclaration.getName());
                String ns = attributeDeclaration.getNamespace();
                if (ns != null && !ns.equals(schemaNamespace)) {
                    symbol.setNamespace(ns);
                }
		symbol.setType(getTypeString(attributeDeclaration.getTypeDefinition()));
		symbol.setRequired(attributeUse.getRequired());
		symbol.setConstraint(getConstraintString(attributeUse));
		symbol.setDescription(getDocumentationString(attributeUse));
    	builder.appendChild(symbol);
		builder.levelUp();
	}

	/**
	 * @param elementDeclaration
	 * @param cardinality
	 */
	private void processElementDeclaration(XSElementDeclaration elementDeclaration, String cardinality, boolean isRoot) {
		XSTypeDefinition typeDefinition	= elementDeclaration.getTypeDefinition();
		
		SymbolElement symbol = new SymbolElement();
		symbol.setName(elementDeclaration.getName());
                String ns = elementDeclaration.getNamespace();
                if (ns != null && !ns.equals(schemaNamespace)) {
                    symbol.setNamespace(ns);
                }
		symbol.setType(getTypeString(typeDefinition));
		symbol.setCardinality(cardinality);
		symbol.setNillable(elementDeclaration.getNillable());
		symbol.setAbstr(elementDeclaration.getAbstract());
		symbol.setSubstitution(getSubstitutionString(elementDeclaration));
		symbol.setDescription(getDocumentationString(elementDeclaration));
		if (isRoot && oneNodeOnly) { // without Collapse All and Expand All buttons
			symbol.setStartYPosition(20); //default 50
		}
		if (isRoot) {
			builder.setRoot(symbol);
		} else {
			builder.appendChild(symbol);
		}
		//LOOP
		if (processLoop(elementDeclaration)) {
			builder.levelUp();
			return;
		}
		stack.push(elementDeclaration);
                
                if (stack.size() > 1 && oneNodeOnly) {
                    //skip processing
                } else {
                    //COMPLEX TYPE
                    if (typeDefinition.getTypeCategory()==XSTypeDefinition.COMPLEX_TYPE) {
                            processComplexTypeDefinition((XSComplexTypeDefinition) typeDefinition);
                    }
                }
		//IDENTITY CONSTRAINTS
		processIdentityConstraints(elementDeclaration.getIdentityConstraints());
		stack.pop();
		builder.levelUp();
	}

	/**
	 * @param complexTypeDefinition
	 */
	private void processComplexTypeDefinition(XSComplexTypeDefinition complexTypeDefinition) {
		//PARTICLE
		XSParticle particle = complexTypeDefinition.getParticle();
		if (particle!=null) {
			processParticle(particle);
		}
		//ATTRIBUTE USES
		processAttributeUses(complexTypeDefinition.getAttributeUses());
		//ATTRIBUTE WILDCARD
		XSWildcard wildcard = complexTypeDefinition.getAttributeWildcard();
		if (wildcard!=null) {
			processAttributeWildcard(wildcard);
		}
	}

	/**
	 * @param attributeUses
	 */
	private void processAttributeUses(XSObjectList attributeUses) {
		for (int i=0; i<attributeUses.getLength(); i++) {
			processAttributeUse((XSAttributeUse) attributeUses.item(i));
		}
	}

	/**
	 * @param identityConstraints
	 */
	private void processIdentityConstraints(XSNamedMap identityConstraints) {
		for (int i=0; i<identityConstraints.getLength(); i++) {
			processIdentityConstraintDefinition((XSIDCDefinition) identityConstraints.item(i));
		}
	}

	/**
	 * @param elementDeclaration
	 * @return
	 */
	private boolean processLoop(XSElementDeclaration elementDeclaration) {
		if (stack.contains(elementDeclaration)) {
			SymbolLoop symbol = new SymbolLoop();
			builder.appendChild(symbol);
			builder.levelUp();
			return true;
		}
		return false;
	}

	/**
	 * @param identityConstraintDefinition
	 */
	private void processIdentityConstraintDefinition(XSIDCDefinition identityConstraintDefinition) {
		AbstractSymbol symbol = null;
		int category = identityConstraintDefinition.getCategory();
		if (category == XSIDCDefinition.IC_UNIQUE) {
			symbol = new SymbolUnique();
			symbol.setDescription(getDocumentationString(identityConstraintDefinition));
			((SymbolUnique) symbol).setName(identityConstraintDefinition.getName());
                        String ns = identityConstraintDefinition.getNamespace();
                        if (ns != null && !ns.equals(schemaNamespace)) {
                            ((SymbolUnique) symbol).setNamespace(ns);
                        }
		}
		else if (category == XSIDCDefinition.IC_KEY) {
			symbol = new SymbolKey();
			symbol.setDescription(getDocumentationString(identityConstraintDefinition));
			((SymbolKey) symbol).setName(identityConstraintDefinition.getName());
                        String ns = identityConstraintDefinition.getNamespace();
                        if (ns != null && !ns.equals(schemaNamespace)) {
                            ((SymbolKey) symbol).setNamespace(ns);
                        }
		}
		else if (category == XSIDCDefinition.IC_KEYREF) {
			symbol = new SymbolKeyref();
			symbol.setDescription(getDocumentationString(identityConstraintDefinition));
			((SymbolKeyref) symbol).setName(identityConstraintDefinition.getName());
                        String ns = identityConstraintDefinition.getNamespace();
                        if (ns != null && !ns.equals(schemaNamespace)) {
                            ((SymbolKeyref) symbol).setNamespace(ns);
                        }
			((SymbolKeyref) symbol).setRefer(identityConstraintDefinition.getRefKey().getName());
		}
    	builder.appendChild(symbol);
		
		SymbolSelector symbolSelector = new SymbolSelector();
		symbolSelector.setXpath(identityConstraintDefinition.getSelectorStr());
    	builder.appendChild(symbolSelector);
		builder.levelUp();
		
		StringList fieldStrings = identityConstraintDefinition.getFieldStrs();
		for (int i=0; i<fieldStrings.getLength(); i++) {
			SymbolField symbolField = new SymbolField();
			symbolField.setXpath(fieldStrings.item(i));
	    	builder.appendChild(symbolField);
			builder.levelUp();
		}
		
		builder.levelUp();
	}
	
	
	//HELPERS-----------------------------------------------------------------//



	/**
	 * @param attributeUse
	 * @return
	 */
	private String getConstraintString(XSAttributeUse attributeUse) {
		if (attributeUse.getConstraintType()==XSConstants.VC_DEFAULT) {
			return "default: " + attributeUse.getConstraintValue();
		}
		else if (attributeUse.getConstraintType()==XSConstants.VC_FIXED) {
			return "fixed: " + attributeUse.getConstraintValue();
		}
		return null;
	}

	/**
	 * @param typeDefinition
	 * @return
	 */
	private String getTypeString(XSTypeDefinition typeDefinition) {
		if (typeDefinition.getAnonymous()) {
			if (typeDefinition.getTypeCategory()==XSTypeDefinition.SIMPLE_TYPE) {
				return "base: "+typeDefinition.getBaseType().getName();
			}
		}
		else {
			return "type: "+typeDefinition.getName();
		}
		return null;
	}

	/**
	 * @param elementDeclaration
	 * @return
	 */
	private String getSubstitutionString(XSElementDeclaration elementDeclaration) {
		XSElementDeclaration substitution = elementDeclaration.getSubstitutionGroupAffiliation();
		if (substitution!=null) {
			return substitution.getName();
		}
		return null;
	}

	/**
	 * @param wildcard
	 * @return
	 */
	private String getNamespaceString(XSWildcard wildcard) {
		if (wildcard.getConstraintType()==XSWildcard.NSCONSTRAINT_ANY) {
			return "any NS";
		}
		StringBuffer namespace = new StringBuffer();
		if (wildcard.getConstraintType()==XSWildcard.NSCONSTRAINT_NOT) {
			namespace.append("not NS: ");
		}
		else {
			namespace.append("NS: ");
		}
		StringList constraintList = wildcard.getNsConstraintList();
		boolean absent = false;
		for (int i=0; i<constraintList.getLength(); i++) {
			if (constraintList.item(i)==null) {
				if (!absent) {
					namespace.append("[absent] ");
					absent = true;
				}
			}
			else {
				namespace.append(constraintList.item(i));
				namespace.append(' ');
			}
		}
		return namespace.toString();
	}
	
	/**
	 * @param particle
	 * @return
	 */
	private String getCardinalityString(XSParticle particle) {
    	int minOccurs = particle.getMinOccurs();
    	int maxOccurs = particle.getMaxOccurs();
		if (particle.getMaxOccursUnbounded()) {
			return minOccurs+"..\u221E";
    	}
    	else if (minOccurs!=1 || maxOccurs!=1) {
    		return minOccurs+".."+maxOccurs;
    	}
		return null;
	}
	
	/**
	 * @param wildcard
	 * @return
	 */
	private int getProcessContents(XSWildcard wildcard) {
		switch (wildcard.getProcessContents()) {
			case XSWildcard.PC_STRICT:
				return AbstractSymbol.PC_STRICT;
			case XSWildcard.PC_SKIP:
				return AbstractSymbol.PC_SKIP;
			case XSWildcard.PC_LAX:
				return AbstractSymbol.PC_LAX;
			default:
				return AbstractSymbol.PC_STRICT;
		}
	}

	//private List<String> getDocumentationString(XSElementDeclaration elementDeclaration) {
	private List<String> getDocumentationString(org.apache.xerces.xs.XSObject itemDeclaration) {
		//XSAnnotation annotation = elementDeclaration.getAnnotation();
		List<String> annotationsList = new ArrayList<>();
		XSObjectList annotations = null;
		if (itemDeclaration instanceof XSElementDeclaration) {
			annotations = ((XSElementDeclaration) itemDeclaration).getAnnotations();
		} else if (itemDeclaration instanceof XSAttributeUse) {
			annotations = ((XSAttributeUse) itemDeclaration).getAnnotations();
		} else if (itemDeclaration instanceof XSModelGroup) {
			annotations = ((XSModelGroup) itemDeclaration).getAnnotations();
		} else if (itemDeclaration instanceof XSIDCDefinition) {
			annotations = ((XSIDCDefinition) itemDeclaration).getAnnotations();
		} else if (itemDeclaration instanceof XSWildcard) {
			annotations = ((XSWildcard) itemDeclaration).getAnnotations();
		}

		if (annotations != null) {
			for (Object annotationObject: annotations) {
				XSAnnotation annotation = (XSAnnotation) annotationObject;
				String annotationString = annotation.getAnnotationString();
				try {
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder = factory.newDocumentBuilder();
					ByteArrayInputStream input =  new ByteArrayInputStream(annotationString.getBytes("UTF-8"));
					Document doc = dBuilder.parse(input);
					XPath xPath =  XPathFactory.newInstance().newXPath();
					String expression = "//*[local-name() = 'documentation']";
					NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
					for (int i = 0; i < nodeList.getLength(); i++) {
						Node nNode = nodeList.item(i);
						// Todo: split text string by element's width
						annotationsList.add(nNode.getTextContent());
					}
				} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
					logger.severe("Can't retrieve the documentation: " + e.toString());
				}
			}
		}
		return annotationsList;
	}
}