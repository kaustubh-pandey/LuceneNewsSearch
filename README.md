# LuceneNewsSearch - A Search Engine that uses BM25F to search and rank News Articles
## About
This project was a part of Information Retrieval Course
 See IR_Presentation.pdf and IR_Report.pdf for more info
## Ranking Algorithm
BM25F, a variant of BM25, was used for ranking search results which is one of the state-of-the-art method used for ranking structured documents
## Requirements:
1. Lucene 6.2.0
2. Eclipse or netbeans
(Note: Create  a folder "lucene" in C: drive of windows and put your entire lucene-6.2.0 folder inside this to avoid any build path complications)

--------------------------------------------------------------------
## How To Run:
1. Extract the LuceneNewsSearch-Final.zip file in eclipse workspace
2. In eclipse, File->Import->General->Existing Projects... and import the extracted folder
3. Import the Lucene jar files by going to File->Build Path->Configure Build Path-> Add External Jars
4. Select the file NewJFrame1.java and run it in eclipse
5. A window opens, click on write index.(This rewrites the index)
6. After writing index, Enter a query for example: "2016-12-31 01-02-2017 Trump Israel"
(Note: The query can be made without dates also, but format supported for date are:
dd-mm-yyyy,dd/mm/yyyy,yyyy-mm-dd,dd-mm-yyyy)
7. Click on "search" button to see the results in the textarea of GUI.


### Some GUI snapshots
![gui1](https://user-images.githubusercontent.com/28951222/50070230-4488af80-01f3-11e9-88bb-815a54c6810c.JPG)
![gui2](https://user-images.githubusercontent.com/28951222/50070241-4e121780-01f3-11e9-8d6d-913617ad30a5.JPG)
![gui3](https://user-images.githubusercontent.com/28951222/50070247-54a08f00-01f3-11e9-9bca-0a674afb3abf.JPG)
![gui4](https://user-images.githubusercontent.com/28951222/50070254-5a967000-01f3-11e9-9b5b-997b2080ebfe.JPG)
