package com.alojate.alojamientos.infrastructure.config;

import com.alojate.alojamientos.application.ports.output.UnitOfWork;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

@Component
public class TransaccionDeAlojamientos implements UnitOfWork {

    private final TransactionTemplate transacciones;

    public TransaccionDeAlojamientos(PlatformTransactionManager transactionManager) {
        this.transacciones = new TransactionTemplate(transactionManager);
    }

    @Override
    public <T> T ejecutar(Supplier<T> trabajo) {
        return transacciones.execute(estado -> trabajo.get());
    }
}
