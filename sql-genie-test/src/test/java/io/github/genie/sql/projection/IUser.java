package io.github.genie.sql.projection;

import io.github.genie.sql.builder.executor.ProjectionUtil;
import io.github.genie.sql.builder.executor.ProjectionUtil.Schema;
import io.github.genie.sql.builder.meta.Projection;
import io.github.genie.sql.builder.meta.ProjectionAttribute;
import io.github.genie.sql.entity.User;
import io.github.genie.sql.meta.JpaMetamodel;

public interface IUser {

    int getId();

    int getRandomNumber();

    String getUsername();

    U parentUser();

    static void main(String[] args) {
        JpaMetamodel metamodel = new JpaMetamodel();
        Projection projection = metamodel.getProjection(User.class, IUser.class);
        for (ProjectionAttribute attribute : projection.attributes()) {
            System.out.println(attribute);
        }

        Schema schema = ProjectionUtil.getSchema(projection.attributes());
        System.out.println(schema);
    }

    record U(int id, int randomNumber, String username) {


    }


}
