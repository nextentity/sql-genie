package io.github.genie.sql;

import jakarta.persistence.EntityTransaction;

import java.sql.SQLException;

public class Transaction {

    public static void doInTransaction(boolean jdbc, Runnable action) {
        try {
            executeAction(jdbc, action);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void executeAction(boolean jdbc, Runnable action) throws SQLException {
        EntityTransaction transaction = EntityManagers.getEntityManager().getTransaction();
        boolean commit = true;
        if (jdbc) {
            SingleConnectionProvider.CONNECTION_PROVIDER
                    .execute(connection -> {
                        connection.setAutoCommit(false);
                        return null;
                    });
        } else {
            transaction.begin();
        }
        try {
            action.run();
        } catch (Throwable throwable) {
            commit = false;
            if (jdbc) {
                SingleConnectionProvider.CONNECTION_PROVIDER
                        .execute(connection -> {
                            connection.rollback();
                            return null;
                        });
            } else {
                transaction.rollback();
            }
            throw throwable;
        } finally {
            if (commit) {
                if (jdbc) {
                    SingleConnectionProvider.CONNECTION_PROVIDER
                            .execute(connection -> {
                                connection.commit();
                                return null;
                            });
                } else {
                    transaction.commit();
                }
            }
        }
    }

}
