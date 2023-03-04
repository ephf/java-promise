package com.promise;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Vector;

public class Promise<T> {

    public static class Resolver<T> {

        interface ListenerLambda<T> {
            void run(Exception error, T data);
        }

        ListenerLambda<T> listener;

        public Resolver(ListenerLambda<T> listener) {
            this.listener = listener;
        }

        public void resolve(T data) {
            this.listener.run(null, data);
        }

        public T resolve(T data, boolean ignore) {
            this.listener.run(null, data);
            return data;
        }

        public void reject(Exception error) {
            this.listener.run(error, null);
        }
    }

    interface ResolverLambda<T> {
        void run(Resolver<T> resolver) throws IOException;
    }

    interface ThenListenerLambda<T> {
        void run(T data) throws IOException;
    }

    interface StackedThenListenerLambda<T, C> {
        C run(T data) throws IOException;
    }

    interface CatchListenerLambda {
        void run(Exception error);
    }

    private final Vector<ThenListenerLambda<T>> thenListeners = new Vector<>();
    private final Vector<StackedThenListenerLambda<T, ?>> stackedThenListeners = new Vector<>();
    private final Vector<CatchListenerLambda> catchListeners = new Vector<>();

    public Promise(ResolverLambda<T> resolverLambda) {
        new Thread(() -> {
            try {
                resolverLambda.run(new Resolver<>((error, data) -> {
                    if (error == null) {
                        thenListeners.forEach(listener -> {
                            try {
                                listener.run(data);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        stackedThenListeners.forEach(listener -> {
                            try {
                                listener.run(data);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        return;
                    }
                    catchListeners.forEach(listener -> listener.run(error));
                }));
            } catch(Exception error) {
                if(catchListeners.size() == 0) {
                    System.out.println("Error: " + error);
                }
                catchListeners.forEach(listener ->
                    listener.run(error)
                );
            }
        }).start();
    }

    public Promise<T> then(ThenListenerLambda<T> listener) {
        thenListeners.add(listener);
        return this;
    }

    public Promise<T> Catch(CatchListenerLambda listener) {
        catchListeners.add(listener);
        return this;
    }

    public static class ContinualPromise<T, C> {

        Promise<T> promise;

        public ContinualPromise(ResolverLambda<T> resolver) {
            promise = new Promise<>(resolver);
        }

        public Promise<C> then(StackedThenListenerLambda<T, C> listener) {
            return new Promise<>(resolver ->
                    promise.stackedThenListeners.add(data ->
                            resolver.resolve(listener.run(data), true)
                    )
            );
        }

        public Promise<T> Catch(CatchListenerLambda listener) {
            promise.catchListeners.add(listener);
            return promise;
        }

    }

}
