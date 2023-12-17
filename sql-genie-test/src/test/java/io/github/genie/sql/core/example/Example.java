// package github.alittlehuang.sql4j.example;
//
// import github.alittlehuang.sql4j.dsl.QueryBuilder;
// import github.alittlehuang.sql4j.dsl.builder.Query;
// import github.alittlehuang.sql4j.jpa.JpaQueryBuilder;
// import jakarta.persistence.EntityManager;
// import jakarta.persistence.EntityManagerFactory;
// import jakarta.persistence.Persistence;
//
// public class Example {
//
//     public static void main(String[] args) {
//
//         try (EntityManagerFactory factory = Persistence.createEntityManagerFactory("org.hibernate.jpa")) {
//             EntityManager em = factory.createEntityManager();
//             QueryBuilder builder = new JpaQueryBuilder(em);
//             Query<Employee> query = builder.query(Employee.class);
//             runExample(query);
//         }
//     }
//
//     private static void runExample(Query<Employee> query) {
//
//         // select * from employee where id = 1
//         query.where(Employee::getId).eq(1).getSingle();
//
//
//         // select employee.*, company.* from
//         // employee left join company
//         // on employee.company_id = company.id
//         // where employee.id = 1
//         query.where(Employee::getId).eq(1)
//                 .fetch(Employee::getCompany)
//                 .getSingle();
//
//         // select * from employee where name = 'Luna' and age > 10
//         query.where(Employee::getName).eq("Luna")
//                 .and(Employee::getAge).gt(10)
//                 .getList();
//
//         // select * from employee where name = 'Luna' and age > 10 order by id desc limit 0,100
//         query.where(Employee::getName).eq("Luna")
//                 .and(Employee::getAge).gt(10)
//                 .orderBy(Employee::getId).desc()
//                 .getList(0, 100);
//
//         // select employee.* from
//         // employee left join company
//         // on employee.company_id = company.id
//         // where company.name = 'Microsoft'
//         query.where(Employee::getCompany).to(Company::getName).eq("Microsoft").getList();
//     }
//
// }
