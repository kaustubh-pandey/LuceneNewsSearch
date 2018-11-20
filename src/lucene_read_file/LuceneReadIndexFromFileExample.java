package lucene_read_file;

import java.awt.List;


import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
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
 
public class LuceneReadIndexFromFileExample
{
    //directory contains the lucene indexes
    private static final String INDEX_DIR = "indexedFiles";
 
    public static void main(String[] args) throws Exception
    {
        //Create lucene searcher. It search over a single IndexReader.
        IndexSearcher searcher = createSearcher();
         
        //Search indexed contents using search term
        String queryString= "Fret 31-12-2016 WASHINGTON";
        ArrayList<String> extractedDates=getDates(queryString);
        ArrayList<String> processedDates=processDates(extractedDates);
        ArrayList<TopDocs> foundDateDocs=new ArrayList<TopDocs>();
        for(int i=0;i<processedDates.size();i++){
        	System.out.println("Date:"+processedDates.get(i)+"::");
        	TopDocs foundDocs = searchInDate(processedDates.get(i).toString(), searcher);
        	foundDateDocs.add(foundDocs);
        }
        //System.out.println(foundDateDocs.size());
        for(int i=0;i<foundDateDocs.size();i++){
        	
        	TopDocs foundDocs=foundDateDocs.get(i);
        	//System.out.println(foundDocs.scoreDocs.length);
        	for(int j=0;j<foundDocs.scoreDocs.length;j++){
            	System.out.print(foundDocs.scoreDocs[j].doc+" ");
            }
        }
        System.out.println("-----------------");
        //TopDocs foundDocs = searchInDate("2018/11/19", searcher);
        TopDocs foundDocs2= searchInBody(queryString,searcher);
        TopDocs foundDocs3=searchInTitle(queryString,searcher);
        ArrayList<Integer> arr=new ArrayList<Integer>();
        //Merging to be done
        //System.out.println("Date Match:");
//        for(int i=0;i<foundDocs.scoreDocs.length;i++){
//        	System.out.print(foundDocs.scoreDocs[i].doc+" ");
//        }
        System.out.println();
        System.out.println("Body Match:");
        for(int i=0;i<foundDocs2.scoreDocs.length;i++){
        	System.out.print(foundDocs2.scoreDocs[i].doc+" ");
        }
        System.out.println();
        for(int i=0;i<foundDocs3.scoreDocs.length;i++){
        	System.out.print(foundDocs3.scoreDocs[i].doc+" ");
        }
        System.out.println();
//        for(int i=0;i<foundDocs.scoreDocs.length;i++){
//        	for(int j=0;j<foundDocs2.scoreDocs.length;j++){
//        		if(foundDocs.scoreDocs[i].doc==foundDocs2.scoreDocs[j].doc){
//        			arr.add(foundDocs.scoreDocs[i].doc);
//        		}
//        	}
//        	
//        }
        System.out.println("Common Docs :: ");
        for(int k=0;k<arr.size();k++){
        	System.out.println(arr.get(k));
        }
        //Total found documents
        long r=0;
        for(int i=0;i<foundDateDocs.size();i++){
        		r=foundDateDocs.get(i).totalHits;
        		System.out.println("Total Results Date matches :: " +r);
        }
        
        System.out.println("Total Results Body matches:: " + foundDocs2.totalHits);
        System.out.println("Total Results Title matches:: " + foundDocs3.totalHits);
         
        //Let's print out the path of files which have searched term
//        for (ScoreDoc sd : foundDocs.scoreDocs)
//        {
//            Document d = searcher.doc(sd.doc);
//            System.out.println("Path : "+ d.get("path") +"Date: "+d.get("date")+ ", Score : " + sd.score);
//        }
        for (ScoreDoc sd : foundDocs2.scoreDocs)
        {
            Document d = searcher.doc(sd.doc);
            System.out.println("Path : "+ d.get("path") +"Date: "+d.get("date")+ ", Score : " + sd.score);
        }
    }
     
    
//    public Document getDocument(ScoreDoc scoreDoc) 
//    		   throws IOException {
//    		   return indexSearcher.doc(scoreDoc.doc);	
//    }
//    
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
        return searcher;
    }
}
