package xsdvi.svg;

import xsdvi.utils.FileHelper;
import xsdvi.utils.TreeElement;
import xsdvi.utils.WriterHelper;

/**
 * @author Vaclav Slavitinsky
 *
 */
public class SvgForXsd {
	protected WriterHelper writer;
	private String styleUri = null;
	private boolean embodyStyle = true;
        
        private boolean hideMenuButtons = false;
        
	/**
	 * 
	 */
	protected static final String XML_DECLARATION = new FileHelper().readStringFromResourceFile("svg/xml_declaration.xml");
	
	/**
	 * 
	 */
	protected static final String SVG_DOCTYPE = new FileHelper().readStringFromResourceFile("svg/doctype.txt");
	
	/**
	 * 
	 */
	protected static final String SVG_START = new FileHelper().readStringFromResourceFile("svg/svg_start.txt");
	
	/**
	 * 
	 */
	protected static final String TITLE = new FileHelper().readStringFromResourceFile("svg/title.txt");
	
	/**
	 * 
	 */
	protected static final String SCRIPT = new FileHelper().readStringFromResourceFile("svg/script.js");
	
	/**
	 * 
	 */
	protected static final String STYLE = new FileHelper().readStringFromResourceFile("svg/style.css");

	/**
	 * 
	 */
	protected static final String DEFINED_SYMBOLS = new FileHelper().readStringFromResourceFile("svg/defined_symbols.svg");
	
	/**
	 * 
	 */
	protected static final String MENU_BUTTONS = new FileHelper().readStringFromResourceFile("svg/menu_buttons.svg");
	
	/**
	 * 
	 */
	protected static final String SVG_END = new FileHelper().readStringFromResourceFile("svg/svg_end.txt");
	
	/**
	 * @param w
	 */
	public SvgForXsd(WriterHelper w) {
		this.writer = w;
	}
	
	/**
	 * 
	 */
	protected void printStyleRef() {
            String style_template = new FileHelper().readStringFromResourceFile("svg/style.xml");
            print(style_template.replaceAll("%STYLE_URI%", styleUri));
	}

	/**
	 * 
	 */
	protected void printEmbodiedStyle() {
            String style_template = new FileHelper().readStringFromResourceFile("svg/style.html");
            print(style_template.replaceAll("%STYLE%", STYLE));
	}
	
	/**
	 * @param style
	 * @param symbols
	 */
	protected void printDefs(boolean style, boolean symbols) {
		print("<defs>");
		if (style) {
			printEmbodiedStyle();
		}
		if (symbols) {
			print(DEFINED_SYMBOLS);
		}
        print("</defs>");
	}
	
	/**
	 * 
	 */
	public void printExternStyle() {
		writer.newWriter(styleUri);
		print(STYLE);
		writer.close();
	}
	
	/**
	 * 
	 */
	public void begin() {
		print(XML_DECLARATION);
		if (!embodyStyle) {
			printStyleRef();
		}
		print(SVG_DOCTYPE);
		print(SVG_START);
		print(TITLE);
		
        print(SCRIPT
                .replaceAll("%HEIGHT_SUM%", String.valueOf(AbstractSymbol.MAX_HEIGHT+AbstractSymbol.Y_INDENT))
                .replaceAll("%HEIGHT_HALF%", String.valueOf(AbstractSymbol.MAX_HEIGHT/2)));

        printDefs(embodyStyle, true);

        if (!hideMenuButtons) {
            print(MENU_BUTTONS);
        }
	}

	/**
	 * 
	 */
	public void end() {
		print(SVG_END);
	    writer.close();
	}

	/**
	 * @param string
	 */
	protected void print(String string) {
		writer.append(string+"\n");
	}

	/**
	 * @return
	 */
	public WriterHelper getWriter() {
		return writer;
	}

	/**
	 * @param w
	 */
	public void setWriter(WriterHelper w) {
		this.writer = w;
	}
	
        public void setHideMenuButtons(boolean hideMenuButtons) {
            this.hideMenuButtons = hideMenuButtons;
        }
        
	/**
	 * @param embody
	 */
	public void setEmbodyStyle(boolean embody) {
		embodyStyle = embody;
	}

	/**
	 * @param styleUri
	 */
	public void setStyleUri(String styleUri) {
		this.styleUri = styleUri;
	}

	/**
	 * @return
	 */
	public String getStyleUri() {
		return styleUri;
	}

	/**
	 * @return
	 */
	public boolean embodyStyle() {
		return embodyStyle;
	}

	/**
	 * @param rootSymbol
	 */
	public void draw(AbstractSymbol rootSymbol) {
		begin();
		drawSymbol(rootSymbol);
		end();
	}

	/**
	 * @param symbol
	 */
	private void drawSymbol(AbstractSymbol symbol) {
		symbol.setSvg(this);
		symbol.prepareBox();
		symbol.draw();
		for (TreeElement s : symbol.getChildren()) {
			drawSymbol((AbstractSymbol) s);
		}
	}
}