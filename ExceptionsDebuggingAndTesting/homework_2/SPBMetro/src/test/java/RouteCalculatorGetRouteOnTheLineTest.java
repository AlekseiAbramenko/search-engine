import core.Line;
import core.Station;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class RouteCalculatorGetRouteOnTheLineTest extends TestCase {
    StationIndex stationIndex;

    Line line1 = new Line(1, "Красная");

    Station station1 = new Station("Фруктовая", line1);
    Station station2 = new Station("Яблочная", line1);
    Station station3 = new Station("Ананасовая", line1);
    Station station4 = new Station("Морковная", line1);

    @Override
    protected void setUp() throws Exception {
        stationIndex = new StationIndex();

        stationIndex.addLine(line1);

        line1.addStation(station1);
        line1.addStation(station2);
        line1.addStation(station3);
        line1.addStation(station4);

        stationIndex.addStation(station1);
        stationIndex.addStation(station2);
        stationIndex.addStation(station3);
        stationIndex.addStation(station4);
    }

    public void testGetRouteOnTheLine() {
        RouteCalculator calculator = new RouteCalculator(stationIndex);

        List<Station> actual = calculator.getShortestRoute(station1,station4);
        List<Station> expected = new ArrayList<>();
        expected.add(station1);
        expected.add(station2);
        expected.add(station3);
        expected.add(station4);
        assertEquals(expected, actual);
    }
}
