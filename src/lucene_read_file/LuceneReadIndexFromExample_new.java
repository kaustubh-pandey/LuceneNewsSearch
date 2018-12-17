package lucene_read_file;

import java.awt.List;


import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.BlendedTermQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.PerFieldSimilarityWrapper;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException; 
import java.util.Arrays;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;

public class LuceneReadIndexFromExample_new
{
    //directory contains the lucene indexes
    private static final String INDEX_DIR = "indexedFiles";
 
    public static ArrayList<String> pmain(String queryTerm) throws Exception
    {
    	
    	ArrayList<String> res = new ArrayList();
    	FSDirectory dir = FSDirectory.open(Paths.get(INDEX_DIR));
		IndexReader reader = DirectoryReader.open(dir);
//		test(reader);
		IndexSearcher searcher = new IndexSearcher(reader);
		//String queryTerm="Trump Winter 2016-12-31";
                if(queryTerm.compareTo("")==0){
                    System.exit(0);
                }
		
		System.out.println(queryTerm);
//		queryTerm=soundexSentence(queryTerm);
		Map<String,Float> boost = new HashMap<String,Float>();
		boost.put("date",0.5f);
		boost.put("title",0.25f);
		boost.put("body",0.25f);

		
		
		MultiFieldQueryParser qp = new MultiFieldQueryParser(new String[] {"date","title","body"}, new StandardAnalyzer(),boost);
//		MultiFieldQueryParser qp = new MultiFieldQueryParser(new String[] {"description","name","year","location","developer"}, new StandardAnalyzer(),boost);
		MultiFieldQueryParser qp2 = new MultiFieldQueryParser(new String[] {"date","title","body"},new WhitespaceAnalyzer(),boost);

                Query query = qp.parse(queryTerm);
                System.out.println(query);
                ArrayList<String> dates_query= getDates(queryTerm);
//                System.out.println("OK");
//                System.out.println(dates_query.size());
                float[] myarr=new float[101];
                if(!dates_query.isEmpty()){
                            ArrayList<String> processed_dates= processDates(dates_query);
                            String queryTerm2="";
                            for(int i=0;i<processed_dates.size();i++){
                                queryTerm2+=" "+processed_dates.get(i);
                            }
                            System.out.println(queryTerm2);
                            Query query2 = qp2.parse(queryTerm2);
                            System.out.println(query2);
                            TopDocs foundDocs_date = searcher.search(query2,5);

                            for (ScoreDoc sd : foundDocs_date.scoreDocs)
                    {
                        Document d = searcher.doc(sd.doc);
            //            if(d.get("date").compareTo("2016-12-31") == 0){
                             System.out.println(d.get("date"));
                             System.out.println(d.get("title"));
                             myarr[sd.doc]=sd.score;

                             System.out.println("Path : "+ d.get("title") + ", Score : " + sd.score);
                             System.out.println("--------#---------------------------------");
                             res.add(d.get("date"));
                             res.add(d.get("title"));
                             res.add("Score: "+Float.toString(sd.score));
                             res.add("Body : "+ d.get("body"));
                             res.add("\nPath : "+d.get("path"));
                             res.add("--------#---------------------------------");
            //            }

                    }
                }
//		query.createWeight(searcher,true,(float) 0.0);
//		PhraseQuery query=new PhraseQuery("name",queryTerm);
		 
		TopDocs foundDocs = searcher.search(query, 10);
                
                
		System.out.println("Total Results :: " + foundDocs.totalHits);
                res.add("Total Results :: " + foundDocs.totalHits);
        
        //Let's print out the path of files which have searched term
        // For date docs
        
        // For normal docs
        for (ScoreDoc sd : foundDocs.scoreDocs)
        {
            Document d = searcher.doc(sd.doc);
//            if(d.get("date").compareTo("2016-12-31") == 0){
            	 System.out.println(d.get("date"));
                 System.out.println(d.get("title"));
                 
                myarr[sd.doc]=sd.score;
                 System.out.println("Path : "+ d.get("title") + ", Score : " + sd.score);
                 System.out.println("--------#---------------------------------");
                 res.add(d.get("date"));
                 res.add(d.get("title"));
                 res.add("Score: "+Float.toString(sd.score));
                 res.add("Body : "+ d.get("body"));
                 res.add("\nPath : "+d.get("path"));
                 res.add("--------#---------------------------------");
//            }
           
        }
        //Create lucene searcher. It search over a single IndexReader.
        System.out.println(Arrays.toString(myarr));
        System.out.println("-----------------");
        return res;
    }

    
    //To preprocess query
    
    private static ArrayList<String> getDates(String queryString){
    	Matcher m = Pattern.compile("(\\d{4}/\\d{2}/\\d{2}|\\d{2}/\\d{2}/\\d{4}|\\d{2}-\\d{2}-\\d{4}|\\d{4}-\\d{2}-\\d{2})",
    			Pattern.CASE_INSENSITIVE).matcher(queryString);
    	ArrayList<String> mydates=new ArrayList<String>();
        while (m.find()) {
            //System.out.println(m.group(1));
            mydates.add(m.group(1));
        }
        return mydates;
    }
    
    private static String convertDate(String date){
    	String returnDate=date;
    	if(date.charAt(2)=='/'){
    		String newDate[]=date.split("/");
    		returnDate=newDate[2]+"-"+newDate[1]+"-"+newDate[0];
    	}
    	else if(date.charAt(2)=='-'){
    		String newDate[]=date.split("-");
    		returnDate=newDate[2]+"-"+newDate[1]+"-"+newDate[0];
    	}
    	else if(date.charAt(4)=='/'){
    		returnDate=date.replace('/', '-');
    	}
    	return returnDate;
    }
    
    private static ArrayList<String> processDates(ArrayList<String> extractedDates){
    	ArrayList<String> processedDates=new ArrayList<String>();
    	for(int i=0;i<extractedDates.size();i++){
    		processedDates.add(convertDate(extractedDates.get(i)));
    	}
    	return processedDates;
    }
    private static TopDocs searchInDate(String textToFind, IndexSearcher searcher) throws Exception
    {
        //Create search query
        //QueryParser qp = new QueryParser("date", new StandardAnalyzer());
        //Query query = qp.parse(textToFind.toString());
        PhraseQuery.Builder builder = new PhraseQuery.Builder();
        builder.add(new Term("date",textToFind));
        PhraseQuery pq = builder.build();
        //search the index
        TopDocs hits = searcher.search(pq, 10);
        //System.out.println(hits.scoreDocs[0].doc);
        return hits;
    }
    
    private static TopDocs searchInTitle(String textToFind, IndexSearcher searcher) throws Exception
    {
        //Create search query
        QueryParser qp = new QueryParser("title", new StandardAnalyzer());
        Query query = qp.parse(textToFind);
         
        //search the index
        TopDocs hits = searcher.search(query, 10);
        System.out.println(hits.scoreDocs[0].doc);
        return hits;
    }
    private static TopDocs searchInBody(String textToFind, IndexSearcher searcher) throws Exception
    {
        //Create search query
        QueryParser qp = new QueryParser("body", new StandardAnalyzer());
        Query query = qp.parse(textToFind);
         
        //search the index
        TopDocs hits = searcher.search(query, 10);
        //System.out.println(hits.scoreDocs[0].doc);
        return hits;
    }
 
    private static IndexSearcher createSearcher() throws IOException
    {
        Directory dir = FSDirectory.open(Paths.get(INDEX_DIR));
         
        //It is an interface for accessing a point-in-time view of a lucene index
        IndexReader reader = DirectoryReader.open(dir);
         
        //Index searcher
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new BM25FSimilarity());
        return searcher;
    }
}
