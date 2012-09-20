package es.upm.fi.oeg.sparqlstream;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.lang.Parser;
import com.hp.hpl.jena.sparql.lang.ParserFactory;
import com.hp.hpl.jena.sparql.lang.ParserRegistry;

import es.upm.fi.oeg.sparqlstream.parser.ParserSPARQLstr;

public class StreamQueryFactory extends QueryFactory 
{
	static public Query create(String queryString)
	{
		return create(queryString,null,StreamSyntax.syntaxSPARQLstream);
	}
	
	static public Query create(String queryString, String baseURI, Syntax querySyntax)
	{
	       StreamQuery query = new StreamQuery() ;
	       if (!ParserRegistry.containsParserFactory(querySyntax))
	       {
	    	   ParserRegistry.addFactory(StreamSyntax.syntaxSPARQLstream,                      
	    			   new ParserFactory() 
	    	   			{
	    		   			public boolean accept( Syntax syntax ) 
	    		   				{ return StreamSyntax.syntaxSPARQLstream.equals(syntax) ; } 
	    		   			public Parser create( Syntax syntax ) 
	    		   				{ return new ParserSPARQLstr() ; } 
	    		   		}) ;
	       }
	       parse(query, queryString, baseURI, querySyntax) ;
	       return query ;
	       
	   }
	   

}
