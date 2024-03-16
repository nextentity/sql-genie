package io.github.genie.sql.test;

import io.github.genie.sql.test.entity.Gender;
import io.github.genie.sql.test.entity.User;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Users {

    public static List<User> getUsers() {
        String[] names = {"Marjorie Minnie", "Jeremy Keynes", "Richard Noel", "Aurora Benedict", "Burgess Dodd",
                "Karen Samuel", "Stanley Aldington", "Burke Commons", "Polly Anna", "Vivien Fox", "Valentine Newton",
                "Bishop Eisenhower", "Hedy Titus", "Elvis Gus", "Baldwin Ingersoll", "Elton Lynch", "Tracy Dewar",
                "Bess", "Quentin Hemingway", "Betsy Toynbee", "Ulysses Marlowe", "Ivy Yule", "Duke Hobbes",
                "Horace Gray", "Quincy Dolly", "Marlon Wolf", "Bard Shelley", "Flora Dutt", "Meredith Bush",
                "Erica Sapir", "Malcolm Hoyle", "Merry Howell(s)", "Beryl Zangwill", "Isaac Ruth", "Bob Peacock",
                "Warner Flower", "Hobart Adam", "Jessie Cook(e)", "Fitch I.", "Kerr Blake", "Octavia Eden",
                "Denise Barnard", "Erin Smollett", "Nat Gibbon", "Janet Landon", "Rory Crane", "Patricia Fielding",
                "Gale Robinson", "Alvis Adams", "Ingrid Meredith", "Wright Dorothea", "Pearl Judson", "Eudora Holt",
                "Rebecca Emmie", "Basil Stone", "Griselda Judith", "Tyler Edward", "Wendell Beard",
                "Nathaniel Wells", "Jason Whit", "Stan Kingsley", "Boyd Swinburne", "Frances MacArthur",
                "Reg Pepys", "Tiffany Austen", "Aaron Symons", "Guy Sweet", "Rod Morrison", "Catherine Hodgson",
                "Ingram Charles", "Gustave Mat(h)ilda", "Wayne Louise", "Amos Cocker", "Bruce Archibald",
                "Taylor Croft", "Vera Isabel", "Cleveland Lindberg(h)", "Neil Spenser", "Odelette Richardson",
                "Lester Margery", "Josephine Child", "Ralap Zechariah", "Lucien Paul", "Max Leopold", "Jane Lamb",
                "Boris Kell(e)y", "Emma Amelia", "Monroe Carllyle", "Truda Alerander", "Zara Abraham", "Zero Tours",
                "Julie Swift", "Archer London", "Kennedy Arnold", "Abner Lyly", "Carr Bach", " Roy Sawyer  ",
                "Nicholas Carroll", "Booth Longfellow", "Payne Webster", "Tony Darwin", " Trim "};

        List<User> result = new ArrayList<>(names.length);
        Random random = new Random();
        for (int i = 0; i < names.length * 100; i++) {
            String name = names[i % names.length];
            User user = newUser(i, name, random);
            int pid = i / 10;
            user.setPid(pid == 0 ? null : pid);
            user.setValid(i % 2 == 0);
            result.add(user);
        }
        Map<Integer, User> userMap = result.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        for (User user : result) {
            user.setParentUser(userMap.get(user.getPid()));
        }

        return result;

    }

    @NotNull
    public static User newUser(int i, String name, Random random) {
        Gender[] values = Gender.values();
        long l = Duration.ofDays(5).toMillis();
        User user = new User();
        user.setId(i);
        user.setUsername(name);
        user.setTime(new Timestamp(System.currentTimeMillis() - random.nextInt((int) l)));
        user.setTimestamp((double) user.getTime().getTime());
        user.setRandomNumber(random.nextInt(20));
        user.setGender(values[random.nextInt(2)]);
        user.setInstant(new Timestamp(System.currentTimeMillis()));
        user.setTestLong(random.nextLong());
        user.setTestInteger(random.nextInt(100));
        user.setTestLocalDate(LocalDate.now());
        user.setTestLocalDateTime(new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        return user;
    }

}
