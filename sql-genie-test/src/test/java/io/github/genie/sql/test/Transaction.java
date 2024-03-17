package io.github.genie.sql.test;

import jakarta.persistence.EntityTransaction;

import java.sql.SQLException;

public class Transaction {

    public static void doInTransaction(Runnable action) {
        try {
            executeAction(action);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void executeAction(Runnable action) throws SQLException {
        EntityTransaction transaction = EntityManagers.getEntityManager().getTransaction();
        boolean rollback = false;
        SingleConnectionProvider.CONNECTION_PROVIDER
                .execute(connection -> {
                    connection.setAutoCommit(false);
                    return null;
                });
        transaction.begin();
        try {
            action.run();
        } catch (Throwable throwable) {
            rollback = true;
            SingleConnectionProvider.CONNECTION_PROVIDER
                    .execute(connection -> {
                        connection.rollback();
                        return null;
                    });
            transaction.rollback();
            throw throwable;
        } finally {
            if (!rollback) {
                SingleConnectionProvider.CONNECTION_PROVIDER
                        .execute(connection -> {
                            connection.commit();
                            return null;
                        });
                transaction.commit();
            }
        }
    }

}
