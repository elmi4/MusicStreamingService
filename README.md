# MusicStreamingService

Distributed Systems project (AUEB 2020)
    
_GitHub Collaborators_:   
@Eleni-Saxoni  
@Elias-Magg  
@elmi4

### Instructions
- In order to run the program you first have to start:  
**1)Brokers** (BrokerEntry, BrokerEntry1, BrokerEntry2)  
**2)Publishers** (PublisherEntry, PublisherEntry1)  
**3)Consumers** (ConsumerEntry, ConsumerEntry1)    

-For the Publishers: Inside "*files/Tracks*" there are 2 folders corresponding to the 2 Publishers' data sets. Each Publisher serves 
 a set of artists (grouped alphabetically A-M, M-Z). You can add more songs to the application depending on whether the 
 artist name of the song belongs to the first or the second data set.

-For the Consumers: specify the artist name and the song name of the song
 you want to request and specify the RequestType, meaning what will happen with the received
 data (Download each chunk separately, wait for all the chunks and download the whole mp3,
 calculate and show the MD5 hash of the chunk for validation, etc.)
 
-For the Brokers: there is a file "_files/BrokerCredentials.txt_" that contains the 
IP and Port of the expected Brokers in the application. Make sure that the ConnectionInfo
of the created Brokers match the info specified in this file. This file is read by both
the Publishers and the Consumers. You can run the application with as many brokers
as you want, as long as you update "_files/BrokerCredentials.txt_".

 
 There are classes with "main" methods inside each of these files.