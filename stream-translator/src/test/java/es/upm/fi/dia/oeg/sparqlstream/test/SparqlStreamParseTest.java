package es.upm.fi.dia.oeg.sparqlstream.test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.syntax.Element;

import es.upm.fi.dia.oeg.common.ParameterUtils;
import es.upm.fi.dia.oeg.integration.test.QueryTestBase;
import es.upm.fi.dia.oeg.sparqlstream.StreamQuery;
import es.upm.fi.dia.oeg.sparqlstream.StreamQueryFactory;
import es.upm.fi.dia.oeg.sparqlstream.syntax.ElementStream;
import es.upm.fi.dia.oeg.sparqlstream.syntax.ElementTimeWindow;
import es.upm.fi.dia.oeg.sparqlstream.syntax.ElementWindow;
import es.upm.fi.dia.oeg.sparqlstream.syntax.WindowUnit;

public class SparqlStreamParseTest extends QueryTestBase
{
	
	static String queryInvalidWindow="";
	static String constructSimple ="";
	
	private static Logger logger = Logger.getLogger(SparqlStreamParseTest.class.getName());
	static Properties props;

	@BeforeClass
	public static void setUp() throws IOException, URISyntaxException
	{
		init();
		props = ParameterUtils.load(SparqlStreamParseTest.class.getClassLoader().getResourceAsStream("config/config_memoryStore.properties"));

		queryInvalidWindow= ParameterUtils.loadAsString(SparqlStreamParseTest.class.getClassLoader().getResource("queries/common/invalidWindow.sparql"));
		constructSimple = ParameterUtils.loadAsString(SparqlStreamParseTest.class.getClassLoader().getResource("queries/testConstructSimple.sparql"));
	}
	
	@Test(expected=QueryParseException.class)//@Ignore
	public void testInvalidTimeWindow()
	{
		/*
		String service= "http://swiss-experiment.ch/sparql/";
        String querystr= "SELECT DISTINCT ?p WHERE { ?s ?p ?t }";
        QueryExecution qe= QueryExecutionFactory.sparqlService(service, querystr);
        ResultSet rs = qe.execSelect();
        while (rs.hasNext())
        {
        	QuerySolution qs = rs.next();
        	System.out.println(qs.get("film"));
        }
		System.out.println("tiptipitpit"+rs.getRowNumber());*/
		Query query = StreamQueryFactory.create(queryInvalidWindow);
		logger.info("hola");

	}

	@Test
	public void testStreamGraphQuery()
	{
		logger.debug("Query "+testStreamGraphSimple);
		StreamQuery query = (StreamQuery) StreamQueryFactory.create(testStreamGraphSimple);
		assertNotNull(query);
		assertNotNull(query.getStreams());
		assertEquals(1,query.getStreams().size());
		ElementStream s = query.getStream("http://semsorgrid4env.eu/ns#ccometeo.srdf");
		assertNotNull(s);
		assertEquals("http://semsorgrid4env.eu/ns#ccometeo.srdf", s.getUri());
		ElementTimeWindow w = (ElementTimeWindow) s.getWindow();
		assertEquals(10, w.getFrom().getOffset());
		
	}
	
	@Test
	public void testStreamNowQuery()
	{
		String queryString = "PREFIX cd: <http://semsorgrid4env.eu/ns#> "+							
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+ 
			"SELECT ?WaveObs "+
			"FROM NAMED STREAM cd:ccometeo.srdf [NOW] "+
			"WHERE "+
			"{ ?WaveObs a cd:Observation. 	} ";
					
		StreamQuery query = (StreamQuery) StreamQueryFactory.create(queryString);
		assertEquals(1,query.getStreams().size());

		ElementStream s = query.getStream("http://semsorgrid4env.eu/ns#ccometeo.srdf");
		ElementTimeWindow w = (ElementTimeWindow) s.getWindow();
		assertEquals(0, w.getFrom().getOffset());
		assertNull(w.getTo());
		
	}

	@Test
	public void testStreamLatest30Query()
	{
		String queryString = "PREFIX cd: <http://semsorgrid4env.eu/ns#> "+							
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+ 
			"SELECT ?WaveObs "+
			"FROM NAMED STREAM cd:ccometeo.srdf [NOW - 30 MINUTE] "+
			"WHERE "+
			"{ ?WaveObs a cd:Observation. 	} ";
					
		StreamQuery query = (StreamQuery) StreamQueryFactory.create(queryString);
		assertEquals(1,query.getStreams().size());

		ElementStream s = query.getStream("http://semsorgrid4env.eu/ns#ccometeo.srdf");
		ElementTimeWindow w = (ElementTimeWindow) s.getWindow();
		assertEquals(30, w.getFrom().getOffset());
		assertEquals(WindowUnit.MINUTE, w.getFrom().getUnit());
		assertNull(w.getTo());
		
	}

	@Test
	public void testStreamLatest10MonthQuery()
	{
		String queryString = "PREFIX cd: <http://semsorgrid4env.eu/ns#> "+							
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+ 
			"SELECT ?WaveObs "+
			"FROM NAMED STREAM cd:ccometeo.srdf [NOW - 10 MONTH] "+
			"WHERE "+
			"{ ?WaveObs a cd:Observation. 	} ";
					
		StreamQuery query = (StreamQuery) StreamQueryFactory.create(queryString);
		assertEquals(1,query.getStreams().size());

		ElementStream s = query.getStream("http://semsorgrid4env.eu/ns#ccometeo.srdf");
		ElementTimeWindow w = (ElementTimeWindow) s.getWindow();
		assertEquals(10, w.getFrom().getOffset());
		assertEquals(WindowUnit.MONTH, w.getFrom().getUnit());
		assertNull(w.getTo());
		
	}

	@Test
	public void testStreamLatest10MonthTo5MinQuery()
	{
		String queryString = "PREFIX cd: <http://semsorgrid4env.eu/ns#> "+							
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+ 
			"SELECT ?WaveObs "+
			"FROM NAMED STREAM cd:ccometeo.srdf [NOW - 10 MONTH TO NOW - 5 MINUTE SLIDE 3 MINUTE]  "+
			"WHERE "+
			"{ ?WaveObs a cd:Observation. 	} ";
					
		StreamQuery query = (StreamQuery) StreamQueryFactory.create(queryString);
		assertEquals(1,query.getStreams().size());

		ElementStream s = query.getStream("http://semsorgrid4env.eu/ns#ccometeo.srdf");
		ElementTimeWindow w = (ElementTimeWindow) s.getWindow();
		assertEquals(10, w.getFrom().getOffset());
		assertEquals(WindowUnit.MONTH, w.getFrom().getUnit());
		assertEquals(WindowUnit.MINUTE, w.getTo().getUnit());
		
	}


	@Test
	public void testQueryFilterValue()
	{
		String queryString = "PREFIX cd: <http://semsorgrid4env.eu/ns#> "+							
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+ 
			"SELECT ?WaveObs "+
			"FROM NAMED STREAM cd:ccometeo.srdf [NOW]  "+
			"WHERE "+
			"{ ?obs a cd:ObservationValue. 	" +
			"  FILTER ( ?obs > 0 ) } ";
					
		StreamQuery query = (StreamQuery) StreamQueryFactory.create(queryString);
		assertEquals(1,query.getStreams().size());

		ElementStream s = query.getStream("http://semsorgrid4env.eu/ns#ccometeo.srdf");
		ElementTimeWindow w = (ElementTimeWindow) s.getWindow();
		assertEquals(0, w.getFrom().getOffset());
		//assertEquals(WindowUnit.MONTH, w.getFrom().getUnit());
		//Element pattern = query.getQueryPattern();
		
	}

	
	@Test
	public void  testConstructQuery()
	{
		Query query = StreamQueryFactory.create(constructSimple);
		query.toString();
	}
}