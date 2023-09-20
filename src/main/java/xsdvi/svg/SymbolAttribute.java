package xsdvi.svg;

import java.util.ArrayList;
import xsdvi.utils.WidthCalculator;

/**
 * @author Vaclav Slavitinsky
 *
 */
public class SymbolAttribute extends AbstractSymbol {
	private String name = null;
	private String namespace = null;
	private String type = null;
	private boolean required = false;
	private String constraint = null;
	
	/**
	 * @param name
	 * @param namespace
	 * @param type
	 * @param required
	 * @param constraint
	 */
	public SymbolAttribute(String name, String namespace, String type, boolean required, String constraint) {
		this();
		this.name = name;
		this.namespace = namespace;
		this.type = type;
		this.required = required;
		this.constraint = constraint;
	}

	/**
	 * 
	 */
	public SymbolAttribute() {
		super();
	}
	
	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * @param namespace
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	/**
	 * @return
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * @param required
	 */
	public void setRequired(boolean required) {
		this.required = required;
	}

	/**
	 * @return
	 */
	public String getConstraint() {
		return constraint;
	}

	/**
	 * @param constraint
	 */
	public void setConstraint(String constraint) {
		this.constraint = constraint;
	}
	
	/* (non-Javadoc)
	 * @see xsdvi.svg.AbstractSymbol#draw()
	 */
	@Override
	public void draw() {
		processDescription();
		drawGStart();
		print("<rect class='shadow' x='3' y='3' width='"+width+"' height='"+height+"' rx='9'/>");
		if (required) {
			print("<rect class='boxattribute1' x='0' y='0' width='"+width+"' height='"+height+"' rx='9'");
		}
		else {
			print("<rect class='boxattribute2' x='0' y='0' width='"+width+"' height='"+height+"' rx='9'");
		}
		//drawMouseover();
                print("/>");
		if (namespace!=null) {
			print("<text class='visible' x='5' y='13'>"+namespace+"</text>");
		}
		if (constraint!=null) {
			print("<text class='hidden' visibility='hidden' x='5' y='13'>"+constraint+"</text>");
		}
		if (name!=null) {
			print("<text class='strong' x='5' y='27'><tspan class='big'>@</tspan> "+name+"</text>");
		}
		if (type!=null) {
			print("<text class='visible' x='5' y='41'>"+type+"</text>");
		}
                // commented, because redundant, 'use required: ...' identical to border style
		//print("<text class='hidden' visibility='hidden' x='5' y='41'>use: "+(required ? "required" : "optional")+"</text>");
                ArrayList<String> propertiesArray = new ArrayList<>();
		if (constraint!=null) {
                    propertiesArray.add(constraint);
                }
                String properties = String.join(", ", propertiesArray);
                print("<text x='5' y='59'>"+properties+"</text>");

		int y_start = 59;
		for (String descriptionLine: descriptionStringArray) {
			y_start = y_start + y_shift;
			print("<text x='5' y='" + y_start + "' class='desc'>"+descriptionLine+"</text>");
		}

		drawConnection();
		drawGEnd();
	}
	
	/**
	 * 
	 */
	void drawAlternate() {
		drawGStart();
		print("<rect class='shadow' x='3' y='3' width='"+width+"' height='"+height+"' rx='9'/>");
		if (required) {
			print("<rect class='boxattribute1' x='0' y='0' width='"+width+"' height='"+height+"' rx='9'");
		}
		else {
			print("<rect class='boxattribute2' x='0' y='0' width='"+width+"' height='"+height+"' rx='9'");
		}
		drawMouseover();
		if (namespace!=null) {
			print("<text class='hidden' visibility='hidden' x='5' y='13'>"+namespace+"</text>");
		}
		if (constraint!=null) {
			print("<text class='visible' x='5' y='13'>"+constraint+"</text>");
		}
		if (name!=null) {
			print("<text class='strong' x='5' y='27'><tspan class='big'>@</tspan> "+name+"</text>");
		}
		if (type!=null) {
			print("<text class='hidden' visibility='hidden' x='5' y='41'>"+type+"</text>");
		}
		print("<text class='visible' x='5' y='41'>use: "+(required ? "required" : "optional")+"</text>");
		drawConnection();
		drawGEnd();
	}
	
	/* (non-Javadoc)
	 * @see xsdvi.svg.AbstractSymbol#getWidth()
	 */
	@Override
	public int getWidth() {
		WidthCalculator calc = new WidthCalculator(MIN_WIDTH);
		calc.newWidth(15, name, 3);
		calc.newWidth(15, namespace);
		calc.newWidth(15, type);
		calc.newWidth(15, 13);
		calc.newWidth(15, constraint);
		return calc.getWidth();
	}
	
	/* (non-Javadoc)
	 * @see xsdvi.svg.AbstractSymbol#getHeight()
	 */
	@Override
	public int getHeight() {
		return MAX_HEIGHT;
	}
}