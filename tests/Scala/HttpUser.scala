import io.github.wfouche.tulip.api._
import java.util.concurrent.ThreadLocalRandom
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import scala.compiletime.uninitialized

class HttpUser(userId: Int, threadId: Int) extends TulipUser(userId, threadId) {

  override def onStart(): Boolean = {
    // Initialize the shared RestClient object only once
    if (getUserId == 0) {
      logger.info("Scala")
      logger.info("Initializing static data")
      val connectTimeout = getUserParamValue("connectTimeoutMillis").toInt
      val readTimeout = getUserParamValue("readTimeoutMillis").toInt
      val factory = new SimpleClientHttpRequestFactory()
      factory.setConnectTimeout(connectTimeout)
      factory.setReadTimeout(readTimeout)
      restClient = RestClient.builder()
        .requestFactory(factory)
        .baseUrl(getUserParamValue("baseURI"))
        .build()
      debug = getUserParamValue("debug").toBoolean
      logger.info(s"debug = $debug")
    }
    true
  }

  // Action 1: GET /posts/{id}
  override def action1(): Boolean = {
    try {
      val id = if (debug) 1 else ThreadLocalRandom.current().nextInt(100) + 1
      val rsp = restClient.get()
        .uri("/posts/{id}", id)
        .retrieve()
        .body(classOf[String])
      rsp != null && rsp.length > 2
    } catch {
      case _: RestClientException => false
    }
  }

  // Action 2: GET /comments/{id}
  override def action2(): Boolean = {
    try {
      val id = if (debug) 1 else ThreadLocalRandom.current().nextInt(500) + 1
      val rsp = restClient.get()
        .uri("/comments/{id}", id)
        .retrieve()
        .body(classOf[String])
      rsp != null && rsp.length > 2
    } catch {
      case _: RestClientException => false
    }
  }

  // Action 3: GET /todos/{id}
  override def action3(): Boolean = {
    try {
      val id = if (debug) 1 else ThreadLocalRandom.current().nextInt(200) + 1
      val rsp = restClient.get()
        .uri("/todos/{id}", id)
        .retrieve()
        .body(classOf[String])
      rsp != null && rsp.length > 2
    } catch {
      case _: RestClientException => false
    }
  }

  // Action 99
  override def onStop(): Boolean = true
}

// RestClient object
var restClient: RestClient = uninitialized

// Debug flag
var debug: Boolean = false

// Logger
val logger: Logger = LoggerFactory.getLogger(classOf[HttpUser])
