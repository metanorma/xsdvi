package xsdvi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.ParseException;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import org.junit.contrib.java.lang.system.Assertion;

import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TestName;
import xsdvi.XsdVi;
import xsdvi.utils.FileHelper;

public class xsdviTests {

    static String XSDFILE_IN = "UnitsML-v1.0-csd04.xsd";
    
    @Rule
    public final ExpectedSystemExit exitRule = ExpectedSystemExit.none();

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @Rule
    public final EnvironmentVariables envVarRule = new EnvironmentVariables();

    @Rule public TestName name = new TestName();
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        //XSDFILE_IN = System.getProperty("inputXML");        
    }
    
    @Test
    public void notEnoughArguments() throws ParseException {
        System.out.println(name.getMethodName());
        exitRule.expectSystemExitWithStatus(-1);
        String[] args = new String[]{""};
        XsdVi.main(args);
    }

    
    @Test
    public void xsdNotExists() throws ParseException {
        System.out.println(name.getMethodName());
        exitRule.expectSystemExitWithStatus(-1);
        String[] args = new String[]{"nonexist.xsd"};
        XsdVi.main(args);
    }

    @Test
    public void successCreateSVGforXSD() throws ParseException {
        System.out.println(name.getMethodName());
        ClassLoader classLoader = getClass().getClassLoader();
        
        String xsd = classLoader.getResource(XSDFILE_IN).getFile();
        String outputPath = new File(xsd).getParent() + File.separator + "SVG.test1";
        Path fileout = Paths.get(outputPath, File.separator + XSDFILE_IN.substring(0, XSDFILE_IN.indexOf(".xsd")) + ".svg");
        if (Files.exists(fileout)) {
            try {
                Files.delete(fileout);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        String[] args = new String[]{xsd, "-outputPath", outputPath};
        XsdVi.main(args);
        
        assertTrue(Files.exists(fileout));        
    }
    
    @Test
    public void successCreateSVGforXSDwithRoot() throws ParseException {
        System.out.println(name.getMethodName());
        ClassLoader classLoader = getClass().getClassLoader();
        
        String xsd = classLoader.getResource(XSDFILE_IN).getFile();
        String outputPath = new File(xsd).getParent() + File.separator + "SVG.test2";
        Path fileout = Paths.get(outputPath, File.separator + XSDFILE_IN.substring(0, XSDFILE_IN.indexOf(".xsd")) + ".svg");
        if (Files.exists(fileout)) {
            try {
                Files.delete(fileout);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        String[] args = new String[]{xsd, "-rootNodeName", "UnitsML", "-outputPath", outputPath};
        XsdVi.main(args);
        
        assertTrue(Files.exists(fileout));        
    }
    
    @Test
    public void successCreateSVGforOneNode() throws ParseException {
        System.out.println(name.getMethodName());
        ClassLoader classLoader = getClass().getClassLoader();
        
        String xsd = classLoader.getResource(XSDFILE_IN).getFile();
        String outputPath = new File(xsd).getParent() + File.separator + "SVG.test3";
        Path fileout = Paths.get(outputPath, File.separator + "ElectricCurrent.svg");
        if (Files.exists(fileout)) {
            try {
                Files.delete(fileout);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        String[] args = new String[]{xsd, "-rootNodeName", "ElectricCurrent", "-oneNodeOnly", "-outputPath", outputPath};
        XsdVi.main(args);
        
        assertTrue(Files.exists(fileout));        
    }
    
    
    @Test
    public void successCreateSVGforAllNodes() throws ParseException {
        System.out.println(name.getMethodName());
        ClassLoader classLoader = getClass().getClassLoader();
        
        String xsd = classLoader.getResource(XSDFILE_IN).getFile();
        String outputPath = new File(xsd).getParent() + File.separator + "SVG.test4";
        Path fileout = Paths.get(outputPath, File.separator);
        new FileHelper().deleteFolder(fileout);
        
        String[] args = new String[]{xsd, "-rootNodeName", "all", "-outputPath", outputPath};
        XsdVi.main(args);
        long countFiles = 0;
        try {
            countFiles = Files.list(fileout).count();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        assertTrue(countFiles == 54);
    }

    @Test
    public void successProcessXMLEntitiesInDocumentationTag() throws ParseException, IOException {
        System.out.println(name.getMethodName());
        ClassLoader classLoader = getClass().getClassLoader();

        String xsdFileTest5 = "TestXMLEntities.xsd";
        String xsd = classLoader.getResource(xsdFileTest5).getFile();
        String outputPath = new File(xsd).getParent() + File.separator + "SVG.test5";
        Path fileout = Paths.get(outputPath, File.separator + xsdFileTest5.substring(0, xsdFileTest5.indexOf(".xsd")) + ".svg");
        if (Files.exists(fileout)) {
            try {
                Files.delete(fileout);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        String[] args = new String[]{xsd, "-outputPath", outputPath};
        XsdVi.main(args);

        assertTrue(Files.exists(fileout));
        String svgContent = String.join("", Files.readAllLines(fileout));
        assertTrue(svgContent.contains("See &lt;a"));
        assertTrue(svgContent.contains("&gt;KB2105352&lt;/a&gt;"));
        assertTrue(svgContent.contains("&gt;KB 2105352&lt;/a&gt;"));
    }
}
