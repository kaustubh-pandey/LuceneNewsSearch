Requirements:
1. Lucene 6.2.0
2. Eclipse or netbeans 

--------------------------------------------------------------------
How To Run:
1. Extract the LuceneNewsSearch-Final.zip file in eclipse workspace
2. In eclipse, File->Import->General->Existing Projects... and import the extracted folder
3. Import the Lucene jar files by going to File->Build Path->Configure Build Path-> Add External Jars
4. Select the file NewJFrame1.java and run it in eclipse
5. A window opens, click on write index.(This rewrites the index)
6. After writing index, Enter a query for example: "2016-12-31 01-02-2017 Trump Israel"
(Note: The query can be made without dates also, but format supported for date are:
dd-mm-yyyy,dd/mm/yyyy,yyyy-mm-dd,dd-mm-yyyy)
7. Click on "search" button to see the results in the textarea of GUI.