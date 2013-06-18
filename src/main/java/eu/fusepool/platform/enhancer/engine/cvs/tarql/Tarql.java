/**
 * 
 */
package eu.fusepool.platform.enhancer.engine.cvs.tarql;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.clerezza.rdf.core.MGraph;
import org.apache.commons.io.IOUtils;
import org.deri.tarql.CSVQueryExecutionFactory;
import org.deri.tarql.TarqlParser;
import org.deri.tarql.TarqlQuery;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

//import eu.fusepool.platform.enhancer.engine.cvs.JenaGraphAdaptor;

/**
 * @author giorgio
 *
 */
public class Tarql {

	
	private static String outputFormat = "RDF/XML" ;
	
	public String transfrom(InputStream is) throws Exception {
		Model resultModel = ModelFactory.createDefaultModel();
		
		
		Reader rQuery = readQuery() ;
		TarqlQuery query = new TarqlParser(rQuery).getResult();
		
		Reader rData  = readCsv(is) ;
		
		List<Query> queries = query.getQueries() ;
		if(queries.isEmpty())
			return null ;
		
		Query q = queries.get(0) ;
		Model previousResults = ModelFactory.createDefaultModel();
		previousResults.add(resultModel);
		CSVQueryExecutionFactory.setPreviousResults(previousResults);
		QueryExecution qe = CSVQueryExecutionFactory.create(rData, q);
		resultModel.setNsPrefixes(resultModel);
		qe.execConstruct(resultModel);
		if(resultModel.isEmpty()) 
			return null ;
		resultModel.write(System.out, outputFormat, query.getPrologue().getBaseURI());
		ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
		resultModel.write(bos) ;
 		return bos.toString("UTF-8") ;
		//return new JenaGraphAdaptor(resultModel.getGraph()) ;
	}
	
	
	
	
	
	Reader readQuery() throws UnsupportedEncodingException {
		
		InputStream toRet = this.getClass().getResourceAsStream("/calls.q") ;	
		return new InputStreamReader(toRet, "UTF-8");
	}
	
	Reader readCsv(InputStream is) throws UnsupportedEncodingException {
	
		return new InputStreamReader(is, "UTF-8");
	}
}