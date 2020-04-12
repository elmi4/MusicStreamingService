# MusicStreamingService

Distributed Systems project (AUEB 2020)
    
_GitHub Collaborators_:   
@Eleni-Saxoni  
@Elias-Magg  
@elmi4

### Instructions
- Inside  "*files/Tracks*" there are 2 folders corresponding to the 2 Publishers' data sets. Each Publisher serves 
a set of artists (grouped alphabetically A-M, M-Z). You can add more songs to the application depending on whether the 
artist name of the song belongs to the first or the second data set.

- In order to run the program you first have to start:  
**1)Brokers** (BrokerEntry, BrokerEntry1, BrokerEntry2)  
**2)Publishers** (PublisherEntry, PublisherEntry1)  
**3)Consumers** (ConsumerEntry, ConsumerEntry1)    
having specified to the Consumer the artist name and the song name of the song
 you want to request. There are classes with "main" methods inside each of these files.