package lucene_read_file;


 
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
 
public class LuceneReadIndexFromFileExample
{
    //directory contains the lucene indexes
    private static final String INDEX_DIR = "indexedFiles";
 
    public static void main(String[] args) throws Exception
    {
        //Create lucene searcher. It search over a single IndexReader.
        IndexSearcher searcher = createSearcher();
         
        //Search indexed contents using search term
        String query = "2016-12-31 Trump Israel";
        TopDocs foundDocs = searchInContent(query, searcher);
         
        //Total found documents
        System.out.println("Total Results :: " + foundDocs.totalHits);
         
        //Let's print out the path of files which have searched term
        float[] array = new float[101];
        for (ScoreDoc sd : foundDocs.scoreDocs)
        {
            Document d = searcher.doc(sd.doc);
            String title = d.get("contents").substring(0, 50);
//            System.out.println(sd.doc);
            array[sd.doc] = sd.score;
//            System.out.println("Path : "+ d.get("path") + " title: " + title + ", Score : " + sd.score);
//            System.out.println("Path : "+ d.get("path") + ", Score : " + sd.score);
        }
        System.out.println(Arrays.toString(array));
//        for(int i=0; i<101; i++) {
//        	 System.out.println(array[i]);
//        }
       
    }
     
    private static TopDocs searchInContent(String textToFind, IndexSearcher searcher) throws Exception
    {
        //Create search query
        QueryParser qp = new QueryParser("contents", new StandardAnalyzer());
        Query query = qp.parse(textToFind);
         
        //search the index
        TopDocs hits = searcher.search(query, 70);
        return hits;
    }
 
    private static IndexSearcher createSearcher() throws IOException
    {
        Directory dir = FSDirectory.open(Paths.get(INDEX_DIR));
         
        //It is an interface for accessing a point-in-time view of a lucene index
        IndexReader reader = DirectoryReader.open(dir);
         
        //Index searcher
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new BM25Similarity());
        return searcher;
    }
}