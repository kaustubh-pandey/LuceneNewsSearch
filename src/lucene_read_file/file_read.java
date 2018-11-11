package lucene_read_file;
import java.io.*; 
import java.nio.file.*;
class MyFields{
	void getFields()throws Exception{
		String file_name =new String("E:\\workspace\\lucene_read_file\\myfiles\\test_file.txt");
		String content=new String(Files.readAllBytes(Paths.get(file_name)));
		String content_arr[]=content.split("\n",0);
		//System.out.println(content);
		for(int i=0;i<content_arr.length;i++){
			System.out.println(content_arr[i]);
		}
		String date=content_arr[0].split(":",0)[1];
		String title=content_arr[1].split(":",0)[1];
		String body=content_arr[2].split(":",0)[1];
		for(int i=3;i<content_arr.length;i++){
			body+=content_arr[i];
		}
		System.out.println(date);
		System.out.println(title);
		System.out.println(body);
	}
}
public class file_read {
	public static void main(String args[])throws Exception{
		MyFields myfield=new MyFields();
		myfield.getFields();
	}
}
