package com.alojate.identidad.support;

import com.alojate.identidad.application.ports.output.UnitOfWork;

import java.util.function.Supplier;

public final class UnidadDeTrabajoEnMemoria implements UnitOfWork {

    @Override
    public <T> T ejecutar(Supplier<T> trabajo) {
        return trabajo.get();
    }
}
