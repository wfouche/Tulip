package io.github.wfouche.tulip.api;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class TulipUserFactory {

    public TulipUser getUser(int userId, String className, int threadId) {
        try {
            Class<?> loadedClass = Class.forName(className);
            Constructor<?> ctor=loadedClass.getConstructor(int.class, int.class);
            return (TulipUser) ctor.newInstance(userId, threadId);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}

