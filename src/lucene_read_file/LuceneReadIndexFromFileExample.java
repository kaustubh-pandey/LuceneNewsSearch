package lucene_read_file;

import java.awt.List;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

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
    private static final String INDEX_DIR = "E:\\workspace\\lucene_read_file\\indexedFiles";
 
    public static void main(String[] args) throws Exception
    {
        //Create lucene searcher. It search over a single IndexReader.
        IndexSearcher searcher = createSearcher();
         
        //Search indexed contents using search term
        TopDocs foundDocs = searchInDate("2018/11/19", searcher);
        TopDocs foundDocs2= searchInBody("Cyclone",searcher);
        ArrayList<Integer> arr=new ArrayList<Integer>();
        //Merging to be done
        System.out.println("Date Match:");
        for(int i=0;i<foundDocs.scoreDocs.length;i++){
        	System.out.print(foundDocs.scoreDocs[i].doc+" ");
        }
        System.out.println();
        System.out.println("Body Match:");
        for(int i=0;i<foundDocs2.scoreDocs.length;i++){
        	System.out.print(foundDocs2.scoreDocs[i].doc+" ");
        }
        System.out.println();
        for(int i=0;i<foundDocs.scoreDocs.length;i++){
        	for(int j=0;j<foundDocs2.scoreDocs.length;j++){
        		if(foundDocs.scoreDocs[i].doc==foundDocs2.scoreDocs[j].doc){
        			arr.add(foundDocs.scoreDocs[i].doc);
        		}
        	}
        	
        }
        System.out.println("Common Docs :: ");
        for(int k=0;k<arr.size();k++){
        	System.out.println(arr.get(k));
        }
        //Total found documents
        System.out.println("Total Results Date matches :: " + foundDocs.totalHits);
        System.out.println("Total Results Body matches:: " + foundDocs2.totalHits);
         
        //Let's print out the path of files which have searched term
        for (ScoreDoc sd : foundDocs.scoreDocs)
        {
            Document d = searcher.doc(sd.doc);
            System.out.println("Path : "+ d.get("path") +"Date: "+d.get("date")+ ", Score : " + sd.score);
        }
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
    
    private static TopDocs searchInBody(String textToFind, IndexSearcher searcher) throws Exception
    {
        //Create search query
        QueryParser qp = new QueryParser("body", new StandardAnalyzer());
        Query query = qp.parse(textToFind);
         
        //search the index
        TopDocs hits = searcher.search(query, 10);
        System.out.println(hits.scoreDocs[0].doc);
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
