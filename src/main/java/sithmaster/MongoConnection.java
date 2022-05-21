package sithmaster;

import com.mongodb.client.MongoCollection;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

@Getter
@Setter
public class MongoConnection {
    private MongoCollection<Document> user;
}
