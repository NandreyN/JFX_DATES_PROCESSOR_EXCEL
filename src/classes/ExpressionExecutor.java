package classes;

import javafx.util.Pair;

import java.util.*;
import java.util.function.Function;

public class ExpressionExecutor {
    private static Map<ExpressionParser.Operations, Function<Pair<Date, Integer>, Date>> functionMap;
    private static Calendar calendar;

    static {
        calendar = Calendar.getInstance();

        functionMap = new HashMap<>();
        functionMap.put(ExpressionParser.Operations.ADD, (x) -> {
            calendar.setTime(x.getKey());
            calendar.add(Calendar.DATE, x.getValue());
            return calendar.getTime();
        });

        functionMap.put(ExpressionParser.Operations.SUBTRACT, (x) -> {
            calendar.setTime(x.getKey());
            calendar.add(Calendar.DATE, -x.getValue());
            return calendar.getTime();
        });
    }

    public static Date execute(Date first, ExpressionParser.Operations op, int second) {
        return functionMap.get(op).apply(new Pair<>(first, second));
    }
}
