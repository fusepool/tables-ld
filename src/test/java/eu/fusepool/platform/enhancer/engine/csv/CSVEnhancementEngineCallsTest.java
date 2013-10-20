package eu.fusepool.platform.enhancer.engine.csv;


import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.ontologies.DCTERMS;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.stanbol.enhancer.contentitem.inmemory.InMemoryContentItemFactory;
import org.apache.stanbol.enhancer.servicesapi.ContentItem;
import org.apache.stanbol.enhancer.servicesapi.ContentItemFactory;
import org.apache.stanbol.enhancer.servicesapi.EnhancementEngine;
import org.apache.stanbol.enhancer.servicesapi.impl.StringSource;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;
import eu.fusepool.platform.enhancer.engine.csv.testutil.MockComponentContext;

/**
 * 
 * @author Giorgio Costa
 *
 */
public class CSVEnhancementEngineCallsTest {

	static MockComponentContext ctx ;

	private static ContentItemFactory ciFactory = InMemoryContentItemFactory.getInstance();

	private static final Logger log = LoggerFactory.getLogger(CSVEnhancementEngineCallsTest.class);

	private static ContentItem ci01 = null ;

	//private static final String TEST_FOLDER = "test/data/";

	private static final String TEST_FILE_01 = "/calls.csv" ;


	static CSVEnhancementEngine engine ;

	@BeforeClass
	public static void setUp() throws Exception {
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("CLEAN_ON_STARTUP", false) ;
		properties.put(EnhancementEngine.PROPERTY_NAME, "CSVEngine") ;
		properties.put("Query", "/calls.q") ;

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
			Iterator<Triple> metadataIterator = ci01.getMetadata().filter(null, DCTERMS.hasPart, null) ;
			assertTrue("ContentItme must have at least one "+DCTERMS.hasPart+" predicate",metadataIterator.hasNext()) ;
			//			while (metadataIterator.hasNext()) {
			//				Triple triple = (Triple) metadataIterator.next();
			//				System.out.println(triple);
			//			}

			Hashtable<String, String> fromCSV = loadIDS(TEST_FILE_01) ;
			log.info("Number of calls from CSV file: "+fromCSV.size());
			UriRef uri = new UriRef("http://fusepool.info/class/Call") ;
			Iterator<Triple> callsIterator = ci01.getMetadata().filter(null, null, uri) ;
			while (callsIterator.hasNext()) {
				Triple triple = (Triple) callsIterator.next();
				NonLiteral subject = triple.getSubject() ;
				assertTrue("Element missing in enhancements: "+subject.toString(), fromCSV.containsKey(subject.toString())) ;
				String value = fromCSV.get(subject.toString()) ;
				if(value==null) {
					log.warn("Cannot find value for key: "+subject);
				}
				if(fromCSV.containsKey(subject.toString())) {
					fromCSV.remove(subject.toString()) ;
				}
			}

			assertTrue( fromCSV.isEmpty()) ;
			log.info("all calls properly extracted") ;

		} catch (Exception e) {
			log.error("Error in teest; ", e) ;
		}

	}

	private Hashtable<String, String>  loadIDS(String fileName) throws IOException {
		String prefix = "http://fusepool.info/doc/call/" ;

		Hashtable<String, String> fromCSV = new Hashtable<String, String>() ;

		InputStream is = this.getClass().getResourceAsStream(fileName) ;
		InputStreamReader reader = new InputStreamReader(is) ;
		CSVReader csvReader = new CSVReader(reader, ',', '"', 1) ;
		String [] nextLine;
		while ((nextLine = csvReader.readNext()) != null) {
			// nextLine[] is an array of values from the line
			String id = StringUtils.replace(nextLine[0], " ", "-") ;
			UriRef uri = new UriRef(prefix+id) ;
			if(fromCSV.containsKey(uri))
				log.warn("duplicated: "+uri);
			fromCSV.put(uri.toString(), nextLine[0]) ;
		}
		csvReader.close() ;
		return fromCSV ;

		//File file = new F
		//		FileReader reader = new Filer
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
			log.error("Error while creating content item from file " + filePath, e);
			return null ;
		} 

	}



}
