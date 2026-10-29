package com.alojate.identidad.application.ports.output;

import java.util.function.Supplier;

public interface UnitOfWork {

    <T> T ejecutar(Supplier<T> trabajo);

    default void ejecutar(Runnable trabajo) {
        ejecutar(() -> {
            trabajo.run();
            return null;
        });
    }
}
