import core.Line;
import core.Station;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RouteCalculatorGetRouteWithTwoConnectionsTest extends TestCase {
    StationIndex stationIndex;
    RouteCalculator calculator;

    Line line1 = new Line(1, "Красная");
    Line line2 = new Line(2, "Синяя");
    Line line3 = new Line(3, "Зеленая");

    Line[] lines = new Line[] {
            new Line(1, "Красная"),
            new Line(2, "Синяя"),
            new Line(3, "Зеленая")
    };

    Station station1 = new Station("Фруктовая", line1);
    Station station2 = new Station("Яблочная", line1);
    Station station3 = new Station("Ананасовая", line2);
    Station station4 = new Station("Морковная", line2);
    Station station5 = new Station("Огуречная", line3);
    Station station6 = new Station("Овощная", line3);

    Station[] stations = new Station[] {
            new Station("Фруктовая", line1),
            new Station("Яблочная", line1),
            new Station("Ананасовая", line2),
            new Station("Морковная", line2),
            new Station("Огуречная", line3),
            new Station("Овощная", line3)
    };

    @Override
    protected void setUp() throws Exception {
        stationIndex = new StationIndex();

        //добавляю станции на линии
        Arrays.stream(stations).filter(s -> s.getLine().equals(line1))
                 .forEach(s -> line1.addStation(s));

        Arrays.stream(stations).filter(q -> q.getLine().equals(line2))
                .forEach(s -> line2.addStation(s));

        Arrays.stream(stations).filter(q -> q.getLine().equals(line3))
                .forEach(s -> line3.addStation(s));

        //добавляю в карту линии и станции
        Arrays.stream(lines).forEach(l -> stationIndex.addLine(l));
        Arrays.stream(stations).forEach(s -> stationIndex.addStation(s));

        //добавляю в карту переход
        List<Station> connectionStations = new ArrayList<>();
        connectionStations.add(station2);
        connectionStations.add(station3);
        stationIndex.addConnection(connectionStations);

        //добавляю в карту второй переход
        List<Station> connectionStation2 = new ArrayList<>();
        connectionStation2.add(station4);
        connectionStation2.add(station5);
        stationIndex.addConnection(connectionStation2);
    }

    public void testGetShortestRoute() {
        calculator = new RouteCalculator(stationIndex);

        List<Station> actual = calculator.getShortestRoute(station1,station6);
        List<Station> expected = new ArrayList<>(Arrays.asList(stations));
        assertEquals(expected, actual);
    }
}
