
object Runner  {

  def main(args:Array[String]) = {

    val protocols = java.lang.System.getProperty("https.protocols")
    println(s"https.protocols? $protocols")
    java.lang.System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
    val protocolsNow = java.lang.System.getProperty("https.protocols")
    println(s"https.protocols? $protocolsNow")

    import com.mongodb.{MongoClient, MongoClientURI}


    sys.env.get("MONGO_URI")  match {
      case Some(uriString) => {
        val uri : MongoClientURI = new MongoClientURI(uriString)
        println(s"uri: $uri")
        val client : MongoClient = new MongoClient(uri)
        println(s"client: $client, db name: ${uri.getDatabase()}")
        val db = client.getDB(uri.getDatabase())
        println(s"db: $db")

        val contentCollection = db.getCollection("content")

        val count = contentCollection.count()
        println(s"contentCollection: $contentCollection, count: $count")
//        println(s" names: ${db.getCollectionNames()}")

      }
      case _=> {
        println("set MONGO_URI and DB_NAME")
      }
    }
    //MongoDatabase database = mongoClient.getDatabase("test");
  }
}
