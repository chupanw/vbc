package edu.cmu.cs.varex;

@FunctionalInterface
public interface BiConsumerExp<T, U> {
    void accept(T t, U u) throws Throwable;
}
