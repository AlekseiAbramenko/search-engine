import core.Line;
import core.Station;
import junit.framework.TestCase;

import java.util.*;

public class RouteCalculatorGetRouteWithOneConnectionTest extends TestCase {
    StationIndex stationIndex;

    Line line1 = new Line(1, "Красная");
    Line line2 = new Line(2, "Синяя");

    Station station1 = new Station("Фруктовая", line1);
    Station station2 = new Station("Яблочная", line1);
    Station station3 = new Station("Ананасовая", line2);
    Station station4 = new Station("Морковная", line2);
    Station station5 = new Station("Огуречная", line2);
    Station station6 = new Station("Овощная", line2);

    @Override
    protected void setUp() throws Exception {
        stationIndex = new StationIndex();

        stationIndex.addLine(line1);
        stationIndex.addLine(line2);

        line1.addStation(station1);
        line1.addStation(station2);
        line2.addStation(station3);
        line2.addStation(station4);
        line2.addStation(station5);
        line2.addStation(station6);

        stationIndex.addStation(station1);
        stationIndex.addStation(station2);
        stationIndex.addStation(station3);
        stationIndex.addStation(station4);
        stationIndex.addStation(station5);
        stationIndex.addStation(station6);

        List<Station> connectionStations = new ArrayList<>();

        connectionStations.add(station2);
        connectionStations.add(station3);

        stationIndex.addConnection(connectionStations);
    }

    public void testGetShortestRoute() {
        RouteCalculator calculator = new RouteCalculator(stationIndex);

        List<Station> actual = calculator.getShortestRoute(station1,station6);
        List<Station> expected = new ArrayList<>();
        expected.add(station1);
        expected.add(station2);
        expected.add(station3);
        expected.add(station4);
        expected.add(station5);
        expected.add(station6);
        assertEquals(expected, actual);
    }
}
