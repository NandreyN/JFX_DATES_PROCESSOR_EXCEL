package classes;

import javafx.util.Pair;

import java.util.*;
import java.util.function.Function;

public class ExpressionExecutor {
    private static Map<ExpressionParser.Operations, Function<Pair<List<Date>, Integer>, Date>> functionMap;
    private static Calendar calendar;

    static {
        calendar = Calendar.getInstance();

        functionMap = new HashMap<>();
        functionMap.put(ExpressionParser.Operations.ADD, (x) -> {
            calendar.setTime(x.getKey().get(0));
            calendar.add(Calendar.DATE, x.getValue());
            return calendar.getTime();
        });

        functionMap.put(ExpressionParser.Operations.SUBTRACT, (x) -> {
            calendar.setTime(x.getKey().get(0));
            calendar.add(Calendar.DATE, -x.getValue());
            return calendar.getTime();
        });

        functionMap.put(ExpressionParser.Operations.MAX, (x) -> {
            return Collections.max(x.getKey(), Date::compareTo);
        });

        functionMap.put(ExpressionParser.Operations.MIN, (x) -> {
            return Collections.min(x.getKey(), Date::compareTo);
        });
    }

    public static Date execute(Date first, ExpressionParser.Operations op, int second) {
        List<Date> l = new ArrayList<>();
        l.add(first);
        return functionMap.get(op).apply(new Pair<>(l, second));
    }

    public static Date execute(List<Date> data, ExpressionParser.Operations op) {
        if (op != ExpressionParser.Operations.MIN && op != ExpressionParser.Operations.MAX)
            throw new IllegalArgumentException("Unresolved operation");
        return functionMap.get(op).apply(new Pair<>(data, 0));
    }
}
