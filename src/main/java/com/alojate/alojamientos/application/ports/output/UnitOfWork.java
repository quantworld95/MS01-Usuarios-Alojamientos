package com.alojate.alojamientos.application.ports.output;

import java.util.function.Supplier;

public interface UnitOfWork {

    <T> T ejecutar(Supplier<T> trabajo);
}
