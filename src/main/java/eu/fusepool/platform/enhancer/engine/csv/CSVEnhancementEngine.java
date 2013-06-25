package eu.fusepool.platform.enhancer.engine.csv;


import static org.apache.stanbol.enhancer.servicesapi.helper.EnhancementEngineHelper.randomUUID;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.Map;

import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.apache.clerezza.rdf.ontologies.DCTERMS;
import org.apache.clerezza.rdf.ontologies.FOAF;
import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.stanbol.commons.indexedgraph.IndexedMGraph;
import org.apache.stanbol.enhancer.servicesapi.ContentItem;
import org.apache.stanbol.enhancer.servicesapi.ContentSource;
import org.apache.stanbol.enhancer.servicesapi.EngineException;
import org.apache.stanbol.enhancer.servicesapi.EnhancementEngine;
import org.apache.stanbol.enhancer.servicesapi.ServiceProperties;
import org.apache.stanbol.enhancer.servicesapi.impl.AbstractEnhancementEngine;
import org.apache.stanbol.enhancer.servicesapi.impl.ByteArraySource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.fusepool.platform.enhancer.engine.csv.tarql.Tarql;







@Component(immediate = true, metatype = true,
		configurationFactory = true, //allow multiple instances
		 policy = ConfigurationPolicy.OPTIONAL) //create a default instance with the default configuration
		 
@Service
@Properties(value={
		@Property(name=EnhancementEngine.PROPERTY_NAME, value="CSVEngine"),
		@Property(name=Constants.SERVICE_RANKING,intValue=CSVEnhancementEngine.DEFAULT_SERVICE_RANKING),
		@Property(name="Query", value=CSVEnhancementEngine.DEFAULT_QUERY)
})
public class CSVEnhancementEngine 
extends AbstractEnhancementEngine<IOException,RuntimeException> 
implements EnhancementEngine, ServiceProperties {

	public static final String DEFAULT_ENGINE_NAME = "CSVEngine";
	/**
	 * Default value for the {@link Constants#SERVICE_RANKING} used by this engine.
	 * This is a negative value to allow easy replacement by this engine depending
	 * to a remote service with one that does not have this requirement
	 */
	public static final int DEFAULT_SERVICE_RANKING = 101;
	/**
	 * The default value for the Execution of this Engine. Currently set to
	 * {@link ServiceProperties#ORDERING_EXTRACTION_ENHANCEMENT}
	 */
	public static final Integer defaultOrder = ORDERING_EXTRACTION_ENHANCEMENT;

	public static final String  DEFAULT_QUERY = "/calls.q" ;
	
	// MIME TYPE of the PubMed document
	public static final String MIME_TYPE_CSV = "text/csv";
	
	public static final String ENGINE_URI = "urn:fusepool-csv-engine:part-01:" ;
	

	final Logger logger = LoggerFactory.getLogger(this.getClass()) ;

	//	private static final ContentItemFactory ciFactory = InMemoryContentItemFactory.getInstance();

	/**
	 * The literal factory
	 */
	//private final LiteralFactory literalFactory = LiteralFactory.getInstance();

//	public static final Set<String> supportedMediaTypes;
//	static {
//		Set<String> types = new HashSet<String>();
//		//ensure everything is lower case
//		//		types.add(SupportedFormat.N3.toLowerCase());
//		//		types.add(SupportedFormat.N_TRIPLE.toLowerCase());
//		//		types.add(SupportedFormat.RDF_JSON.toLowerCase());
//		types.add(SupportedFormat.RDF_XML.toLowerCase());
//		types.add(SupportedFormat.TURTLE.toLowerCase());
//		//		types.add(SupportedFormat.X_TURTLE.toLowerCase());
//		supportedMediaTypes = Collections.unmodifiableSet(types);
//	}

	protected ComponentContext componentContext ;
	protected BundleContext    bundleContext ;
	Parser parser ;
	String usedQuery = DEFAULT_QUERY ;



	//@SuppressWarnings("unchecked")
	protected void activate(ComponentContext ce) throws ConfigurationException, IOException {
		super.activate(ce);
		this.componentContext = ce ;
		this.bundleContext = ce.getBundleContext() ;

		
		@SuppressWarnings("rawtypes")
		Dictionary dict = ce.getProperties() ;
		Object o = dict.get("Query") ;
		if(o!=null && !"".equals(o.toString()))  {
			usedQuery = (String) o ;
		}

		logger.info("activating "+this.getClass().getName());
		logger.info("using query: "+usedQuery);
	}

	protected void deactivate(ComponentContext ce) {
		super.deactivate(ce);
	}


	public int canEnhance(ContentItem ci) throws EngineException {

		return ENHANCE_SYNCHRONOUS;
	}

	@Override
	public void computeEnhancements(ContentItem ci) throws EngineException {

		UriRef contentItemId = ci.getUri();
		logger.info("UriRef: "+contentItemId.getUnicodeString()) ;

		Tarql tarql = new Tarql(usedQuery) ;

		try {

			ci.getLock().writeLock().lock();

			getParser() ;
			
			InputStream is = ci.getStream() ;

			String csv2rdf = tarql.transfrom(is) ;
			if(csv2rdf!=null) {
				
				InputStream myInputStream = IOUtils.toInputStream(csv2rdf, "UTF-8");
				Graph toAdd = parser.parse(myInputStream, SupportedFormat.RDF_XML) ;
				// Create enhancements to each entity extracted from the CSV
				MGraph enhancements = addEnhancements(ci, toAdd);

				// Add all the RDF triples to the content item metadata
				ci.getMetadata().addAll(toAdd);
				//ci.getMetadata().addAll(enhancements);
			}
			// Add a part to the content item as a text/plain representation of the XML document for indexing. 
			addPartToContentItem(ci);


		} catch (Exception e) {
			logger.error( "Error while computing the enhancements.", e) ;			
		}

		finally {
			ci.getLock().writeLock().unlock();
		}

	}

	/*
	 *  Add a part to the content item as a text/plain representation of the XML document to be
	 *  used by the ECS for indexing. The part text is constructed from triples properties values so 
	 *  this method must be called after the xml to rdf transformation.
	 */
	public void addPartToContentItem(ContentItem ci) {

		logger.debug("Start adding plain text representation");

		try {

			UriRef partUri = new UriRef(ENGINE_URI + randomUUID()); // part uri with index 1 (part with index 0 is reserved to the input data)
			// Add the same content of the document as text/plain. This part can contain some
			// text extracted from the full content for indexing as title and abstract 

			// full document
			//byte [] content = IOUtils.toByteArray(ci.getBlob().getStream());

			// construct the text for the part from triples properties values
			@SuppressWarnings("deprecation")
			byte [] content = IOUtils.toByteArray(constructText(ci.getMetadata()));

			// Add some content to the new part as plain text 
			ContentSource source = new ByteArraySource(content, "text/plain");
			ci.addPart(ci.getUri(), source);

			// Add metadata about the new part of the content item
			ci.getMetadata().add(new TripleImpl(ci.getUri(), DCTERMS.hasPart, partUri));

		}
		catch (IOException e) {

			logger.error("Error adding text/plain part", e) ;

		}


		logger.debug("Finished adding plain text representation");
	}

	/*
	 * Create an entity annotation for each entity found by the transformation of the XML document. 
	 * Each annotation is referred to its entity.
	 */
	public MGraph addEnhancements(ContentItem ci, Graph xml2rdf) {

		MGraph enhancements = new IndexedMGraph();

		if (! xml2rdf.isEmpty()) {

		}

		return enhancements;		
	}	



	/*
	 * Creates a string filled with values from properties:
	 * foaf:name of inventors and applicants
	 * dcterms:title of the publication
	 * dcterms:abstract of the publication
	 * The text is used for indexing. The graph passed as argument must contain the RDF triples created after the transformation.
	 * 
	 */
	public String constructText(MGraph graph) {

		String text = "";

		UriRef documentUri = null;

		// Get the titles. There might be three titles for en, fr, de.
		Iterator<Triple> ititles = graph.filter(documentUri, DCTERMS.title, null);
		String title = "";
		while(ititles.hasNext()) {
			title = ititles.next().getObject().toString() + " ";
			text += title;
		}


		// Get the abstracts. There might be three abstracts for en, fr, de.
		Iterator<Triple> iabstracts = graph.filter(documentUri, DCTERMS.abstract_, null);
		String abstract_ = " ";
		while(iabstracts.hasNext()) {
			title = iabstracts.next().getObject().toString() + " ";
			text += abstract_;
		}

		// Get all the foaf:name of entities of type foaf:Person.
		Iterator<Triple> inames = graph.filter(null, FOAF.name, null);
		String name = "";
		while(inames.hasNext()) {
			title = inames.next().getObject().toString() + " ";
			text += name;
		}

		logger.info("Text to be indexed" + text);

		return text;
	}

	@Override
	public Map<String, Object> getServiceProperties() {
		return Collections.unmodifiableMap(Collections.singletonMap(
				ENHANCEMENT_ENGINE_ORDERING, (Object) defaultOrder));
	}


	//@Activate
	public void registered(ServiceReference ref) {
		logger.error(this.getClass().getName()
				+" registered") ;
	}

	//@Deactivate
	public void unregistered(ServiceReference ref) {
		logger.info(this.getClass().getName()+" unregistered") ;
	}


	private void getParser() throws Exception {
		ServiceReference ref = bundleContext.getServiceReference(Parser.class.getName()) ;
		if(ref!=null) {
			parser = (Parser)bundleContext.getService(ref) ;
		}else {
			parser = null ;
			logger.error("Unable to find parser for the enhancement") ;
			throw new Exception("Cannot get parser!") ;
		}
	}
	

	/**
	 * Converts the type and the subtype of the parsed media type to the
	 * string representation as stored in {@link #supportedMediaTypes} and then
	 * checks if the parsed media type is contained in this list.
	 * @param mediaType the MediaType instance to check
	 * @return <code>true</code> if the parsed media type is not 
	 * <code>null</code> and supported. 
	 */
//	@SuppressWarnings("unused")
//	private boolean isSupported(String mediaType){
//		return mediaType == null ? false : supportedMediaTypes.contains(
//				mediaType.toLowerCase());
//	}


}
