package edu.cmu.cs.varex;

public class UnimplementedModelClassMethodException extends RuntimeException {
    public UnimplementedModelClassMethodException(String name) {
        super("Unimplemented model class method: " + name);
    }
}
