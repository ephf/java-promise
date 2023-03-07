package com.promise;

import java.util.Vector;

public class Promise<T> {

    public interface Consumer<T> {
        void accept(T data) throws Exception;
    }

    public interface Supplier<T> {
        T get() throws Exception;
    }

    public interface ContinualThenLambda<T, K> {
        K accept(T data) throws Exception;
    }

    public static class Resolver<T> {

        Promise<T> promise;

        public Resolver(Promise<T> promise) {
            this.promise = promise;
        }

        public void resolve(Promise<?> data) {
            data.then(result -> {
                if(result instanceof Promise<?>) {
                    resolve((Promise<?>) result);
                    return;
                }
                try {
                    resolve((T) result);
                } catch(Exception e) {
                    reject(e);
                }
            });
        }

        public void resolve(T data) {
            promise.value = data;
            promise.listeners.forEach(listener -> handle(listener, data));
        }

        public void reject(Exception error) {
            if(promise.catchListeners.size() == 0) {
                System.err.println("Asynchronous Exception: " + error);
                error.printStackTrace();
                return;
            }
            promise.catchListeners.forEach(listener -> {
                try {
                    listener.accept(error);
                } catch (Exception e) {
                    System.err.println("Asynchronous Catch Exception: " + e);
                }
            });
        }

        public <K> void handle(Consumer<K> lambda, K argument) {
            try {
                lambda.accept(argument);
            } catch(Exception e) {
                reject(e);
            }
        }

        public <K, L> L handle(ContinualThenLambda<K, L> lambda, K argument) {
            try {
                return lambda.accept(argument);
            } catch(Exception e) {
                reject(e);
                return null;
            }
        }

    }

    public T value;
    public Resolver<T> resolver;

    public Promise(Consumer<Resolver<T>> lambda) {
        new Thread(() -> {
            resolver = new Resolver<>(this);
            resolver.handle(lambda, resolver);
        }).start();
    }

    public final Vector<Consumer<T>> listeners = new Vector<>();
    public final Vector<Consumer<Exception>> catchListeners = new Vector<>();

    public Promise<T> then(Consumer<T> listener) {
        if(this.value != null) {
            resolver.handle(listener, this.value);
            return this;
        }
        listeners.add(listener);
        return this;
    }

    public <K, L> Promise<L> thenc(ContinualThenLambda<T, K> listener) {
        Promise<L> promise = new Promise<>(resolver -> {
            Consumer<T> newListener = data -> {
                K result = this.resolver.handle(listener, data);
                if (result instanceof Promise<?>) {
                    resolver.resolve((Promise<?>) result);
                    return;
                }
                try {
                    resolver.resolve((L) result);
                } catch (Exception error) {
                    resolver.reject(error);
                }
            };
            if(this.value != null) {
                this.resolver.handle(newListener, this.value);
                return;
            }
            listeners.add(newListener);
        });
        this.catchListeners.forEach(promise::Catch);
        return promise;

    }

    public Promise<T> Catch(Consumer<Exception> listener) {
        catchListeners.add(listener);
        return this;
    }

    public static <K, L> Promise<K> async(Supplier<L> function) {
        return new Promise<>(resolver -> {
            L result = function.get();
            if(result instanceof Promise<?>) {
                resolver.resolve((Promise<?>) result);
                return;
            }
            try {
                resolver.resolve((K) result);
            } catch (Exception error) {
                resolver.reject(error);
            }
        });
    }

}
