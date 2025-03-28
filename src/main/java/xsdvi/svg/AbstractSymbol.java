package xsdvi.svg;

import org.apache.commons.text.WordUtils;
import xsdvi.utils.TreeElement;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Václav Slavìtínský
 *
 */
public abstract class AbstractSymbol extends TreeElement {
    private SvgForXsd svg;

    protected int xPosition;
    protected int yPosition;
    protected int width;
    protected int height;
    protected int startYPosition = 50;

    private static int highestYPosition;

    protected String[] descriptionStringArray = new String[0];
    protected int y_shift = 14;
    private List<String> description = new ArrayList<>();
    protected int additionalHeight;
    protected static int additionalHeightRest;
    protected static int prevXPosition;
    protected static int prevYPosition;

    /**
     * 
     */
    public static final int PC_STRICT	= 1;
    /**
     * 
     */
    public static final int PC_SKIP		= 2;
    /**
     * 
     */
    public static final int PC_LAX		= 3;
    
    /**
     * 
     */
    public static final int X_INDENT	= 45;
    /**
     * 
     */
    public static final int Y_INDENT	= 25;

    /**
     * 
     */
    public static final int MIN_WIDTH	= 60;
    /**
     * 
     */
    public static final int MAX_HEIGHT	= 46;
    /**
     * 
     */
    public static final int MID_HEIGHT	= 31;
    /**
     * 
     */
    public static final int MIN_HEIGHT	= 21;


    /**
     * @return
     */
    public int getXEnd() {
            return xPosition + width;
    }

    /**
     * @return
     */
    public int getYEnd() {
            return yPosition + MAX_HEIGHT;
    }

    /**
     * @return
     */
    public int getXPosition() {
            return xPosition;
    }

    /**
     * @return
     */
    public int getYPosition() {
            return yPosition;
    }

    /**
     * @param xPos
     */
    public void setXPosition(int xPos) {
            this.xPosition = xPos;
    }

    /**
     * @param yPos
     */
    public void setYPosition(int yPos) {
            this.yPosition = yPos;
    }

    /**
     * @param startYPos
     */
    public void setStartYPosition(int startYPos) {
            this.startYPosition = startYPos;
    }
    
    /**
     * @param svgForXsd
     */
    public void setSvg(SvgForXsd svgForXsd) {
            this.svg = svgForXsd;
    }

    /**
     * @return
     */
    public SvgForXsd getSvg() {
            return svg;
    }

    /**
     * @param w
     */
    public void setWidth(int w) {
            this.width = w;
    }

    /**
     * @param h
     */
    public void setHeight(int h) {
            this.height = h;
    }

    /**
     * @param string
     */
    protected void print(String string) {
            svg.print(string);
    }

    /**
     * 
     */
    protected void drawGStart() {
            print("<g id='"+code()+"' class='box' transform='translate("+xPosition+","+yPosition+")' data-desc-height='"+additionalHeight+"' data-desc-height-rest='" + additionalHeightRest + "' data-desc-x='" + prevXPosition +"'>");
    }

    /**
     * 
     */
    protected void drawGEnd() {
            print("</g>\n");
    }

    /**
     * 
     */
    protected void drawConnection() {
            if (isLastChild() && !isFirstChild()) {
                    print("<line class='connection' id='p"+code()+"' x1='"+(10-X_INDENT)+"' y1='"+(((AbstractSymbol) getParent()).yPosition-yPosition+MAX_HEIGHT/2)+"' x2='"+(10-X_INDENT)+"' y2='"+(-15-Y_INDENT)+"'/>");
                    print("<path class='connection' d='M"+(10-X_INDENT)+","+(-15-Y_INDENT)+" Q"+(10-X_INDENT)+",15 0,"+MAX_HEIGHT/2+"'/>");
            }
            else {
                if (hasParent()) {
                    print("<line class='connection' x1='"+(10-X_INDENT)+"' y1='"+MAX_HEIGHT/2+"' x2='0' y2='"+MAX_HEIGHT/2+"'/>");
                }
            }
    }

    /**
     * 
     */
    protected void drawUse() {
            if (hasChildren()) {
                    String code = code();
                    print("<use x='"+(width-1)+"' y='"+(MAX_HEIGHT/2-6)+"' xlink:href='#minus' id='s"+code+"' onclick='show(\""+code+"\")'/>");
            }
    }

    /**
     * 
     */
    protected void drawMouseover() {
            print("onmouseover='makeVisible(\""+code()+"\")' onmouseout='makeHidden(\""+code()+"\")'/>");
    }

    /**
     * 
     */
    public void prepareBox() {
            if (hasParent()) {
                    xPosition = ((AbstractSymbol) getParent()).getXEnd() + X_INDENT;
                    if (isFirstChild()) {
                            yPosition = highestYPosition;
                    }
                    else {
                            yPosition = highestYPosition + MAX_HEIGHT + Y_INDENT;
                    }
            }
            else {
                    xPosition = 20;
                    yPosition = startYPosition;
            }
            width = getWidth();
            height = getHeight();
            highestYPosition = yPosition;
    }

    /**
     * 
     */
    public abstract void draw();

    /**
     * @return
     */
    public abstract int getWidth();

    /**
     * @return
     */
    public abstract int getHeight();

    /**
     *
     * @param description
     */
    public void setDescription(List<String> description) {
        this.description = description;
    }

    protected void processDescription(){
        int wrapLength = (int)Math.round(width / 5.5);
        List<String> stringsWithBreaks =new  ArrayList<>();
        for (String descriptionString: description) {
            // add line breaks into description string
            String descriptionStringWithBreaks = WordUtils.wrap(descriptionString, wrapLength, "\n", true);
            descriptionStringArray = descriptionStringWithBreaks.split("\\R");
            stringsWithBreaks.addAll(Arrays.asList(descriptionStringWithBreaks.split("\\R")));
            additionalHeight+=y_shift * stringsWithBreaks.size(); //descriptionStringArray.length;
        }
        descriptionStringArray = stringsWithBreaks.toArray(new String[0]);
        if (yPosition > prevYPosition && prevYPosition != 0) {
            additionalHeightRest = additionalHeightRest - height;
            if (additionalHeightRest < 0) {
                additionalHeightRest = 0;
            }
            if (additionalHeightRest < additionalHeight) {
                additionalHeightRest = additionalHeight;
            }
        } else { // prevYPosition = yPosition
            if (additionalHeight != 0) {
                additionalHeightRest = additionalHeight;
            }
        }
        if (!description.isEmpty()) {
            prevXPosition = xPosition;
        }
        prevYPosition = yPosition;
    }

    protected void drawDescription(int y_start) {
        for (String descriptionLine: descriptionStringArray) {
            y_start = y_start + y_shift;

            // replace < and > to fix https://github.com/metanorma/xsdvi/issues/9
            print("<text x='5' y='" + y_start + "' class='desc'>"+descriptionLine
                    .replace("<","&lt;")
                    .replace(">","&gt;")+"</text>");
        }
    }

}