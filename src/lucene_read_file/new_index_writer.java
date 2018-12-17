package lucene_read_file;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
 
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.*;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
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

import java.io.IOException;
 
public class new_index_writer
{
	static Similarity perFieldSimilarities =  new PerFieldSimilarityWrapper() {
        @Override
        public Similarity get(String name) {
//            if (name.equals("title")) {
//                return new BM25FSimilarity(/*k1*/1.2f, /*b*/0.8f);
//            } else if (name.equals("description")) {
//                return new BM25FSimilarity(/*k1*/1.4f, /*b*/0.9f);
//            }
        	if (name.equals("body")) {
                return new BM25FSimilarity(/*k1*/0.6f, /*b*/0.75f);
            }
        	else if (name.equals("title")) {
                return new BM25FSimilarity(/*k1*/0.6f, /*b*/0.75f);
            }
        	else if (name.equals("date")) {
                return new BM25FSimilarity(/*k1*/1.4f, /*b*/0.8f);
            }
            return new BM25FSimilarity();
        }
    };
    
    public static void smain()
    {
        //Input folder
        String docsPath = "myfiles";
         
        //Output folder
        String indexPath = "indexedFiles";
 
        //Input Path Variable
        final Path docDir = Paths.get(docsPath);
 
        try
        {
            //org.apache.lucene.store.Directory instance
            Directory dir = FSDirectory.open( Paths.get(indexPath) );
             
            //analyzer with the default stop words
            Analyzer analyzer = new StandardAnalyzer();
             
            //IndexWriter Configuration
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setSimilarity(perFieldSimilarities);
//            iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
             
            //IndexWriter writes new index files to the directory
            IndexWriter writer = new IndexWriter(dir, iwc);
             
            //Its recursive method to iterate all files and directories
            indexDocs(writer, docDir);
 
            writer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
     
    static void indexDocs(final IndexWriter writer, Path path) throws IOException
    {
        //Directory?
        if (Files.isDirectory(path))
        {
            //Iterate directory
            Files.walkFileTree(path, new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
                {
                    try
                    {
                        //Index this file
                        indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
                    }
                    catch (IOException ioe)
                    {
                        ioe.printStackTrace();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        else
        {
            //Index this file
            indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
        }
    }
 
    static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException
    {
        try (InputStream stream = Files.newInputStream(file))
        {
            //Create lucene Document
            Document doc = new Document();
            
            //String file_name =new String("E:\\workspace\\lucene_read_file\\myfiles\\test_file.txt");
    		//String content=new String(Files.readAllBytes(Paths.get(file)));
            String content=new String(Files.readAllBytes(file));
    		String content_arr[]=content.split("\n",0);
    		//System.out.println(content);
//    		for(int i=0;i<content_arr.length;i++){
//    			System.out.println(content_arr[i]);
//    		}
    		String title=content_arr[0];
    		String publication=content_arr[1];
    		String date=content_arr[2];
    		String body=content_arr[3];
    		for(int i=4;i<content_arr.length;i++){
    			body+=content_arr[i];
    		}
 //   		date=date.substring(0,date.length()-1);
//    		System.out.println("$"+title+"$");
//    		System.out.println("$"+publication+"$");
    		System.out.println("$"+date+"$");
//    		System.out.println("$"+body+"$");
    		
             //System.out.print("as"+date+"as");
            doc.add(new StringField("path", file.toString(), Field.Store.YES));
            doc.add(new LongPoint("modified", lastModified));
//            doc.add(new TextField("contents", new String(Files.readAllBytes(file)), Store.YES));
    
            doc.add(new StringField("date", new String(date), Store.YES)); 
            doc.add(new TextField("title", new String(title), Store.YES));
            doc.add(new TextField("body", new String(body), Store.YES)); 
            //Updates a document by first deleting the document(s)
            //containing <code>term</code> and then adding the new
            //document.  The delete and then add are atomic as seen
            //by a reader on the same index
            writer.updateDocument(new Term("path", file.toString()), doc);
        }
    }
}
