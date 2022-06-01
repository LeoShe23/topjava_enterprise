package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.*;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public class UserMealsUtil {
    public static void main(String[] args) {
        List<UserMeal> meals = Arrays.asList(
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410)
        );

        List<UserMealWithExcess> mealsTo = filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo.forEach(System.out::println);

//        System.out.println(filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
    }

    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        List<UserMealWithExcess> list = new ArrayList<>();

        Map<LocalDate, Integer> totalCalories = new HashMap<>();
        for (UserMeal meal : meals) {
            totalCalories.merge(meal.getDateTime().toLocalDate(), meal.getCalories(), Integer::sum);
        }
        for (UserMeal meal : meals) {
            if (TimeUtil.isBetweenHalfOpen(meal.getDateTime().toLocalTime(), startTime, endTime))
                list.add(new UserMealWithExcess(
                        meal.getDateTime(), meal.getDescription(), meal.getCalories(), totalCalories.get(meal.getDateTime().toLocalDate()) > caloriesPerDay
                ));
        }
        // TODO return filtered list with excess. Implement by cycles

        Map<LocalDate, List<UserMeal>> byDay = new HashMap<>();
        for (UserMeal meal : meals)
            byDay.computeIfAbsent(meal.getDateTime().toLocalDate(), key -> new ArrayList<>()).add(meal);

        for (Map.Entry<LocalDate, List<UserMeal>> pair : byDay.entrySet()) {
            int tmpCalories = 0;
            for (UserMeal userMeal : pair.getValue()) {
                    int calories = userMeal.getCalories();
                    tmpCalories += calories;
            }
            if (tmpCalories > caloriesPerDay) {
                for (UserMeal meal : pair.getValue()) {
                    if (TimeUtil.isBetweenHalfOpen(meal.getDateTime().toLocalTime(), startTime, endTime))
                        list.add(new UserMealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), false));
                }
            }
        }
        return list;
    }

    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        List<UserMealWithExcess> list = new ArrayList<>();

        Map<LocalDate, Integer> totalCalories = meals.stream().collect(
                Collectors.groupingBy(
                        userMeal -> userMeal.getDateTime().toLocalDate(),
                        Collectors.summingInt(UserMeal::getCalories)
                ));
//        return meals
//                .stream()
//                .filter(meal -> TimeUtil.isBetweenHalfOpen(meal.getDateTime().toLocalTime(), startTime, endTime))
//                .map(meal -> new UserMealWithExcess(
//                meal.getDateTime(),
//                meal.getDescription(),
//                meal.getCalories(),
//                totalCalories.get(meal.getDateTime().toLocalDate()) > caloriesPerDay
//        ));
        // TODO Implement by streams


        Map<LocalDate, List<UserMeal>> byDay = meals.stream().collect(groupingBy(meal -> meal.getDateTime().toLocalDate()));

        byDay.forEach((key, value) -> {
            int tmpCalories = value.stream().mapToInt(UserMeal::getCalories).sum();
            if (tmpCalories > caloriesPerDay) {
                value.stream().filter(meal -> TimeUtil.isBetweenHalfOpen(meal.getDateTime().toLocalTime(), startTime, endTime))
                        .map(meal -> new UserMealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), false)).forEachOrdered(list::add);
            }
        });
        return list;
    }
}
