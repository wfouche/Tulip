package io.github.wfouche.tulip.api;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * TulipUserFactory create user objects from the class name specified in the benchmark configuration
 * file.
 */
public class TulipUserFactory {

  /** Public constructor */
  public TulipUserFactory() {}

  /**
   * Creates and returns a TulipUser instance based on the provided parameters. This method uses
   * reflection to dynamically instantiate a class that implements TulipUser.
   *
   * @param userId The unique identifier for the user.
   * @param className The fully qualified name of the class to be instantiated. This class must
   *     implement TulipUser.
   * @param threadId The identifier of the thread associated with this user.
   * @return A new instance of TulipUser created with the given userId and threadId.
   * @throws RuntimeException If there's any error during class loading or instantiation, including
   *     ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
   *     InvocationTargetException, or InstantiationException.
   */
  public TulipUser getUser(int userId, String className, int threadId) {
    try {
      Class<?> loadedClass = Class.forName(className);
      Constructor<?> ctor = loadedClass.getConstructor(int.class, int.class);
      return (TulipUser) ctor.newInstance(userId, threadId);
    } catch (ClassNotFoundException
        | NoSuchMethodException
        | IllegalAccessException
        | InvocationTargetException
        | InstantiationException e) {
      throw new RuntimeException(e);
    }
  }
}
