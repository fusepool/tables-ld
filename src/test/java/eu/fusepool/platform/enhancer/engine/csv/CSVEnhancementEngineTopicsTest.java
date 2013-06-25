package eu.fusepool.platform.enhancer.engine.csv;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Set;

import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.commons.io.IOUtils;
import org.apache.stanbol.enhancer.contentitem.inmemory.InMemoryContentItemFactory;
import org.apache.stanbol.enhancer.servicesapi.ContentItem;
import org.apache.stanbol.enhancer.servicesapi.ContentItemFactory;
import org.apache.stanbol.enhancer.servicesapi.EngineException;
import org.apache.stanbol.enhancer.servicesapi.EnhancementEngine;
import org.apache.stanbol.enhancer.servicesapi.impl.StringSource;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.fusepool.platform.enhancer.engine.csv.testutil.MockComponentContext;

/**
 * 
 * @author Giorgio Costa
 *
 */
public class CSVEnhancementEngineTopicsTest {

	static MockComponentContext ctx ;
	
	private static ContentItemFactory ciFactory = InMemoryContentItemFactory.getInstance();
	
	private static final Logger log = LoggerFactory.getLogger(CSVEnhancementEngineTopicsTest.class);

	private static ContentItem ci01 = null ;
	
	//private static final String TEST_FOLDER = "test/data/";
	
	private static final String TEST_FILE_01 = "/topics.csv" ;
	
	
	static CSVEnhancementEngine engine ;
	
	@BeforeClass
	public static void setUp() throws Exception {
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("CLEAN_ON_STARTUP", false) ;
		properties.put(EnhancementEngine.PROPERTY_NAME, "CSVEngine") ;
		properties.put("Query", "/topics.q") ;
		
		ctx = new MockComponentContext(properties) ;
		
		engine = new CSVEnhancementEngine() ;
		engine.parser = Parser.getInstance() ;
		
		//Set<String> supportedFormats = engine.parser.getSupportedFormats() ;
		engine.activate(ctx) ;
		
		// creates a content item from the document and compute the enhancements
		ci01 = createContentItemFromFile(TEST_FILE_01);
	}
	
	
	
	@Test
	public void testTransformation()  {
		try {
			engine.computeEnhancements(ci01) ;
			
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MGraph metadata = ci01.getMetadata() ;
	}
	
	
	
	
	private static  ContentItem createContentItemFromFile(String fileName) {
		
		String filePath = fileName;
		try {
			InputStream in = CSVEnhancementEngine.class.getResourceAsStream(filePath) ;
			StringWriter writer = new StringWriter();
			IOUtils.copy(in, writer);
			String content = writer.toString();
			//System.out.println(theString);
			return ciFactory.createContentItem(new UriRef("urn:test:content-item:") + fileName, new StringSource(content)) ;
		}
		catch (IOException e) {
			System.out.println("Error while creating content item from file " + filePath);
			return null ;
		} 
		
	}
	
	
	
}
