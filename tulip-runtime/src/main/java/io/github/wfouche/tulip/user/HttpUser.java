package io.github.wfouche.tulip.user;

/** The HttpUser class. */
public class HttpUser extends HttpUser_RestClient {

  /**
   * HttpUser() constructor
   *
   * @param userId - User object id
   * @param threadId - Worker thread id
   */
  public HttpUser(int userId, int threadId) {
    super(userId, threadId);
  }
}
