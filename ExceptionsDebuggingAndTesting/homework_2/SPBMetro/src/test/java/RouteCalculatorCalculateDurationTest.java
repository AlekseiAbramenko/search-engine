import core.Line;
import core.Station;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class RouteCalculatorCalculateDurationTest extends TestCase {
    List<Station> route;

    @Override
    protected void setUp() throws Exception {
        route = new ArrayList<>();

        Line line1 = new Line(1, "Красная");
        Line line2 = new Line(2, "Синяя");

        route.add(new Station("Фруктовая", line1));
        route.add(new Station("Яблочная", line1));
        route.add(new Station("Ананасовая", line2));
        route.add(new Station("Морковная", line2));
        route.add(new Station("Огуречная", line2));
        route.add(new Station("Овощная", line2));
    }

    public void testCalculateDuration() {
        double actual = RouteCalculator.calculateDuration(route);
        double expected = 13.5;
        assertEquals(expected, actual);
    }
}
