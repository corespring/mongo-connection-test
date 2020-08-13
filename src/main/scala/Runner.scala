import com.mongodb.{MongoClientOptions, MongoCredential, ServerAddress}
import com.mongodb.casbah.{MongoClient, MongoCollection, MongoDB, MongoURI}
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.gridfs.GridFS


case class MongoSource(
                        val hosts: List[ServerAddress],
                        val dbName: String,
                        val writeConcern: com.mongodb.WriteConcern,
                        val user: Option[String] = None,
                        val password: Option[String] = None,
                        val options: Option[MongoClientOptions],
                        private var conn: MongoClient = null
                      ) {

  def connection: MongoClient = {
    if (conn == null) {


      conn = (options, user) match {
        case (Some(o), Some(u)) => {
          val credentials = MongoCredential.createScramSha1Credential(u, dbName, password.get.toCharArray)
          MongoClient(hosts, List(credentials), o)
        }
        case (Some(o), None) => {
          MongoClient(hosts, o)
        }
        case (None, None) => {
          MongoClient(hosts)
        }
      }
    }
    conn
  }

  def reset() {
    conn.close()
    conn = null
  }

  def db: MongoDB = connection(dbName)

  def collection(name: String): MongoCollection = db(name)

  def cappedCollection(name: String, size: Long, max: Option[Long] = None): MongoCollection = {
    val coll = if (db.collectionExists(name)) {
      db(name)
    } else {
      val options = MongoDBObject.newBuilder
      options += "capped" -> true
      options += "size" -> size
      if (max.isDefined)
        options += "max" -> max.get
      new MongoCollection(db.createCollection(name, options.result()))
    }
    coll
  }

  //  def gridFS(bucketName: String = "fs"): GridFS = GridFS(db, bucketName)

  override def toString() = {
    (if (user.isDefined) user.get + "@" else "") +
      hosts.map(h => h.getHost + ":" + h.getPort).mkString(", ") +
      "/" + dbName + options.map(" with Options[" + _ + "]").getOrElse("")
  }
}

object Runner {


  def simulateSalat(uriString: String) = {
    val uri = MongoURI(uriString)
    val hosts = uri.hosts.map { host =>
      if (host.contains(':')) {
        val Array(h, p) = host.split(':')
        new ServerAddress(h, p.toInt)
      } else {
        new ServerAddress(host)
      }
    }.toList
    val db = uri.database.getOrElse(throw new RuntimeException("mongodb." + ".uri db missing for source[]"))
    val writeConcern = uri.options.getWriteConcern
    val user = uri.username
    val password = uri.password.map(_.mkString).filterNot(_.isEmpty)
    val s = MongoSource(hosts, db, writeConcern, user, password, Some(MongoClientOptions.builder().build()))

    val c = s.collection("content")

    println(s"content count: ${c.count()}")
  }

  def main(args: Array[String]) = {


    println(s"ARGS: ${args.mkString(",")}")

    println("java version:")
    println(System.getProperty("java.version"));
    println(System.getProperty("java.specification.version"));
    println("..")
    val protocols = java.lang.System.getProperty("https.protocols")
    println(s"https.protocols? $protocols")
    java.lang.System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
    val protocolsNow = java.lang.System.getProperty("https.protocols")
    println(s"https.protocols? $protocolsNow")


    sys.env.get("MONGO_URI") match {
      case Some(uriString) => {


        if (args.mkString("").contains("--salat")) {
          simulateSalat(uriString)
        } else {
          import com.mongodb.{MongoClient, MongoClientURI}
          val uri: MongoClientURI = new MongoClientURI(uriString)
          println(s"uri: $uri")
          val client: MongoClient = new MongoClient(uri)
          println(s"client: $client, db name: ${uri.getDatabase()}")
          val db = client.getDB(uri.getDatabase())
          println(s"db: $db")
          val contentCollection = db.getCollection("content")
          val count = contentCollection.count()
          println(s"contentCollection: $contentCollection, count: $count")
          println(s" names: ${db.getCollectionNames()}")
        }


      }
      case _ => {
        println("set MONGO_URI and DB_NAME")
      }
    }
  }
}
